package com.martin.love_application;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkDataManager {
    private static final String TAG = "NetworkDataManager";
    private static final String GITHUB_PAGES_BASE_URL = "https://derschmachtin.github.io/";
    private static final String MESSAGES_ENDPOINT = "messages.json";
    private static final String TIMELINE_ENDPOINT = "timeline.json";

    private Context context;
    private DatabaseHelper dbHelper;
    private ExecutorService executor;

    public interface DataUpdateCallback {
        void onSuccess(int newItemsCount);
        void onError(String error);
        default void onStarted() {} // Optional callback for when fetch starts
    }

    public NetworkDataManager(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Fetch new messages from GitHub Pages and update local database
     */

    public void fetchAndUpdateMessages(DataUpdateCallback callback) {
        // Notify that fetch has started
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(callback::onStarted);
        }
        
        executor.execute(() -> {
            try {
                String jsonResponse = fetchDataFromUrl(GITHUB_PAGES_BASE_URL + MESSAGES_ENDPOINT);
                int newMessages = processMessagesJson(jsonResponse);

                // Run callback on main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onSuccess(newMessages));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching messages", e);
                String userFriendlyError = getUserFriendlyError(e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError(userFriendlyError));
                }
            }
        });
    }

    public void fetchAndUpdateTimeline(DataUpdateCallback callback) {
        // Notify that fetch has started
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(callback::onStarted);
        }
        
        executor.execute(() -> {
            try {
                String jsonResponse = fetchDataFromUrl(GITHUB_PAGES_BASE_URL + TIMELINE_ENDPOINT);
                int newEvents = processTimelineJson(jsonResponse);

                // Run callback on main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onSuccess(newEvents));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching timeline events", e);
                String userFriendlyError = getUserFriendlyError(e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError(userFriendlyError));
                }
            }
        });
    }

    /**
     * Fetch both messages and timeline events from GitHub Pages
     */
    public void fetchAndUpdateAll(DataUpdateCallback callback) {
        executor.execute(() -> {
            try {
                int totalNewItems = 0;
                
                // Fetch messages
                try {
                    String messagesResponse = fetchDataFromUrl(GITHUB_PAGES_BASE_URL + MESSAGES_ENDPOINT);
                    totalNewItems += processMessagesJson(messagesResponse);
                } catch (Exception e) {
                    Log.w(TAG, "Could not fetch messages: " + e.getMessage());
                }
                
                // Fetch timeline events
                try {
                    String timelineResponse = fetchDataFromUrl(GITHUB_PAGES_BASE_URL + TIMELINE_ENDPOINT);
                    totalNewItems += processTimelineJson(timelineResponse);
                } catch (Exception e) {
                    Log.w(TAG, "Could not fetch timeline events: " + e.getMessage());
                }

                // Run callback on main thread
                final int newItemsCount = totalNewItems;
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onSuccess(newItemsCount));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching data", e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError(e.getMessage()));
                }
            }
        });
    }

    private String fetchDataFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                throw new IOException("HTTP Error: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    private int processMessagesJson(String jsonString) throws JSONException {
        JSONArray messagesArray;
        
        // Try to parse as direct array first, then as object with "messages" field
        try {
            messagesArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            JSONObject rootObject = new JSONObject(jsonString);
            messagesArray = rootObject.getJSONArray("messages");
        }

        int newMessagesCount = 0;

        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObj = messagesArray.getJSONObject(i);

            String date = messageObj.getString("date");
            String messageType = messageObj.optString("messageType", messageObj.optString("type", "love_note"));
            String message = messageObj.getString("message");

            // Convert ISO date to your format (yyyy-MM-dd)
            String formattedDate = convertIsoDateToLocal(date);

            // Check if message already exists for this date
            if (!messageExistsForDate(formattedDate)) {
                long result = dbHelper.insertMessage(message, messageType, formattedDate);
                if (result > 0) {
                    newMessagesCount++;
                    Log.d(TAG, "Inserted new message for date: " + formattedDate);
                }
            }
        }

        return newMessagesCount;
    }

    private int processTimelineJson(String jsonString) throws JSONException {
        Log.d(TAG, "Processing timeline JSON: " + jsonString.substring(0, Math.min(200, jsonString.length())));
        
        JSONArray timelineArray;
        
        // Try to parse as direct array first, then as object with "timeline_events" field
        try {
            timelineArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            JSONObject rootObject = new JSONObject(jsonString);
            timelineArray = rootObject.getJSONArray("timeline_events");
        }
        
        int newEventsCount = 0;
        
        Log.d(TAG, "Found " + timelineArray.length() + " timeline events in JSON");

        for (int i = 0; i < timelineArray.length(); i++) {
            JSONObject eventObj = timelineArray.getJSONObject(i);

            String date = eventObj.getString("date");
            String eventType = eventObj.getString("type");
            String title = eventObj.getString("title");
            String description = eventObj.getString("description");
            String photos = eventObj.optString("photos", "");
            
            // Convert ISO date to your format (yyyy-MM-dd)
            String formattedDate = convertIsoDateToLocal(date);

            // Check if timeline event already exists for this date and title
            if (!dbHelper.hasTimelineEventForDateAndTitle(formattedDate, title)) {
                long result = dbHelper.insertTimelineEvent(formattedDate, title, description, photos, eventType);
                if (result > 0) {
                    newEventsCount++;
                    Log.d(TAG, "Inserted new timeline event: " + title + " for date: " + formattedDate);
                } else {
                    Log.w(TAG, "Failed to insert timeline event: " + title);
                }
            } else {
                Log.d(TAG, "Timeline event already exists: " + title + " for date: " + formattedDate);
            }
        }
        
        return newEventsCount;
    }

    private String convertIsoDateToLocal(String isoDate) {
        try {
            // Simple conversion from "2025-09-03T10:30:00Z" to "2025-09-03"
            return isoDate.substring(0, 10);
        } catch (Exception e) {
            Log.e(TAG, "Error converting date: " + isoDate, e);
            return isoDate; // Return original if conversion fails
        }
    }

    private boolean messageExistsForDate(String date) {
        // You'll need to add this method to DatabaseHelper
        return dbHelper.hasMessageForDate(date);
    }

    private String getUserFriendlyError(Exception e) {
        if (e instanceof java.net.UnknownHostException || 
            e instanceof java.net.ConnectException) {
            return "Keine Internetverbindung. Bitte versuche es später noch einmal.";
        } else if (e instanceof java.net.SocketTimeoutException) {
            return "Die Verbindung ist zu langsam. Bitte versuche es später noch einmal.";
        } else if (e instanceof java.io.IOException && e.getMessage().contains("HTTP Error: 404")) {
            return "Die Daten konnten nicht gefunden werden.";
        } else if (e instanceof org.json.JSONException) {
            return "Fehler beim Laden der Daten. Bitte versuche es später noch einmal.";
        } else {
            return "Ein unbekannter Fehler ist aufgetreten. Bitte versuche es später noch einmal.";
        }
    }

    /**
     * Test method to verify the timeline URL is accessible
     */
    public void testTimelineConnection(DataUpdateCallback callback) {
        executor.execute(() -> {
            try {
                String response = fetchDataFromUrl(GITHUB_PAGES_BASE_URL + TIMELINE_ENDPOINT);
                Log.d(TAG, "Timeline connection test successful. Response length: " + response.length());
                
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onSuccess(0)); // 0 as we're just testing connection
                }
            } catch (Exception e) {
                Log.e(TAG, "Timeline connection test failed", e);
                String error = getUserFriendlyError(e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError(error));
                }
            }
        });
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}