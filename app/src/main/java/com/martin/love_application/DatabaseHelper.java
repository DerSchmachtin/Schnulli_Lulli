package com.martin.love_application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LoveApp.db";
    private static final int DATABASE_VERSION = 1;

    // Daily Messages Table
    private static final String TABLE_MESSAGES = "daily_messages";
    private static final String COLUMN_MESSAGE_ID = "id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_MESSAGE_TYPE = "message_type";
    private static final String COLUMN_MESSAGE_DATE = "message_date";
    private static final String COLUMN_IS_UNLOCKED = "is_unlocked";

    // Timeline Events Table
    private static final String TABLE_TIMELINE = "timeline_events";
    private static final String COLUMN_TIMELINE_ID = "id";
    private static final String COLUMN_EVENT_DATE = "event_date";
    private static final String COLUMN_EVENT_TITLE = "event_title";
    private static final String COLUMN_EVENT_DESCRIPTION = "event_description";
    private static final String COLUMN_EVENT_PHOTOS = "event_photos";
    private static final String COLUMN_EVENT_TYPE = "event_type";

    private final Context context;

    private static DatabaseHelper instance; // 👈 Declare the static instance

    // Make the constructor private
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Gets the single instance of the DatabaseHelper.
     * @param context The application context.
     * @return The single instance of DatabaseHelper.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Use application context to prevent memory leaks
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }


//    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.context = context;
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create daily messages table
        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MESSAGE_TEXT + " TEXT NOT NULL,"
                + COLUMN_MESSAGE_TYPE + " TEXT,"
                + COLUMN_MESSAGE_DATE + " TEXT,"
                + COLUMN_IS_UNLOCKED + " INTEGER DEFAULT 0"
                + ")";

        // Create timeline events table
        String createTimelineTable = "CREATE TABLE " + TABLE_TIMELINE + "("
                + COLUMN_TIMELINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_DATE + " TEXT NOT NULL,"
                + COLUMN_EVENT_TITLE + " TEXT NOT NULL,"
                + COLUMN_EVENT_DESCRIPTION + " TEXT,"
                + COLUMN_EVENT_PHOTOS + " TEXT,"
                + COLUMN_EVENT_TYPE + " TEXT"
                + ")";

        db.execSQL(createMessagesTable);
        db.execSQL(createTimelineTable);

        // Call the single method to populate all initial data
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMELINE);
        onCreate(db);
    }

    // New master method to handle all initial data insertion
    private void insertInitialData(SQLiteDatabase db) {
        // Skip loading initial data - app will start empty and use GitHub data only
        Log.d("DatabaseHelper", "Skipping initial data insertion - using GitHub data only");
    }

    private void loadMessagesFromFile(SQLiteDatabase db) {
        try {
            InputStream is = context.getAssets().open("messages.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            int dayOffset = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String messageType = parts[0];
                    String messageText = parts[1];
                    long timeMillis = System.currentTimeMillis() + (dayOffset * 24 * 60 * 60 * 1000L);
                    String date = sdf.format(new Date(timeMillis));

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_MESSAGE_TEXT, messageText);
                    values.put(COLUMN_MESSAGE_TYPE, messageType);
                    values.put(COLUMN_MESSAGE_DATE, date);
                    values.put(COLUMN_IS_UNLOCKED, 0);
                    db.insert(TABLE_MESSAGES, null, values);

                    dayOffset++;
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error reading messages.txt file", e);
        }
    }

    private void loadTimelineEventsFromFile(SQLiteDatabase db) {
        try {
            InputStream is = context.getAssets().open("timeline_events.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");

            JSONArray timelineArray = new JSONArray(jsonString);

            for (int i = 0; i < timelineArray.length(); i++) {
                JSONObject eventObject = timelineArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(COLUMN_EVENT_DATE, eventObject.getString("date"));
                values.put(COLUMN_EVENT_TITLE, eventObject.getString("title"));
                values.put(COLUMN_EVENT_DESCRIPTION, eventObject.getString("description"));
                values.put(COLUMN_EVENT_TYPE, eventObject.getString("type"));
                if (eventObject.has("photos")) {
                    values.put(COLUMN_EVENT_PHOTOS, eventObject.getString("photos"));
                } else {
                    values.put(COLUMN_EVENT_PHOTOS, "");
                }
                db.insert(TABLE_TIMELINE, null, values);
            }
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error reading timeline JSON file", e);
        } catch (JSONException e) {
            Log.e("DatabaseHelper", "Error parsing timeline JSON", e);
        }
    }
    // Insert daily message
    public long insertMessage(String messageText, String messageType, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_TEXT, messageText);
        values.put(COLUMN_MESSAGE_TYPE, messageType);
        values.put(COLUMN_MESSAGE_DATE, date);
        values.put(COLUMN_IS_UNLOCKED, 0);

        long result = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return result;
    }

    // Insert timeline event
    public long insertTimelineEvent(String date, String title, String description, String photos, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_DATE, date);
        values.put(COLUMN_EVENT_TITLE, title);
        values.put(COLUMN_EVENT_DESCRIPTION, description);
        values.put(COLUMN_EVENT_PHOTOS, photos);
        values.put(COLUMN_EVENT_TYPE, type);

        long result = db.insert(TABLE_TIMELINE, null, values);
        db.close();
        return result;
    }

    // Get today's message
    public Message getTodaysMessage() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("DatabaseHelper", "Looking for today's message for date: " + today);

        Cursor cursor = db.query(TABLE_MESSAGES,
                null,
                COLUMN_MESSAGE_DATE + "=?",
                new String[]{today},
                null, null, null);

        Message message = null;
        if (cursor.moveToFirst()) {
            message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
            message.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TEXT)));
            message.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TYPE)));
            message.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_DATE)));
            message.setUnlocked(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_UNLOCKED)) == 1);
            Log.d("DatabaseHelper", "Found today's message: " + message.getText() + " (type: " + message.getType() + ")");
        } else {
            Log.d("DatabaseHelper", "No message found for today (" + today + ")");
        }

        cursor.close();
        db.close();
        return message;
    }

    // Get all timeline events
    public List<TimelineEvent> getAllTimelineEvents() {
        List<TimelineEvent> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TIMELINE,
                null, null, null, null, null,
                COLUMN_EVENT_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                TimelineEvent event = new TimelineEvent();
                event.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIMELINE_ID)));
                event.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE)));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)));
                event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)));
                event.setPhotos(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_PHOTOS)));
                event.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)));
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return events;
    }

    // Unlock today's message
    public void unlockTodaysMessage() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_UNLOCKED, 1);

        db.update(TABLE_MESSAGES, values, COLUMN_MESSAGE_DATE + "=?", new String[]{today});
        db.close();
    }

    // Sample data insertion - YOU'LL CUSTOMIZE THIS!
    public void insertSampleData2() {
        // Check if data already exists
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MESSAGES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();

        if (count > 0) return; // Data already exists

        // Sample messages - REPLACE WITH YOUR OWN!
        String[] sampleMessages = {
                "I love the way you laugh at my terrible jokes ❤️",
                "Remember our first date? I was so nervous but you made everything perfect 🥰",
                "Your smile brightens even my darkest days ☀️",
                "I love how passionate you get about the things you care about 💕",
                "Thank you for always believing in me, even when I don't believe in myself 🌟"
        };

        String[] messageTypes = {"love_note", "memory", "appreciation", "admiration", "gratitude"};

        // Insert messages for the next 30 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < sampleMessages.length; i++) {
            long timeMillis = System.currentTimeMillis() + (i * 24 * 60 * 60 * 1000L);
            String date = sdf.format(new Date(timeMillis));
            insertMessage(sampleMessages[i], messageTypes[i], date);
        }

        // Sample timeline events - CUSTOMIZE WITH YOUR RELATIONSHIP!
        insertTimelineEvent("2024-01-01", "Our First Date ❤️",
                "The day that started everything. I was so nervous but you made me feel comfortable instantly.",
                "", "milestone");
        insertTimelineEvent("2024-02-14", "First Valentine's Day 💕",
                "Our first Valentine's together - you made it so special!",
                "", "holiday");
        insertTimelineEvent("2024-03-15", "First 'I Love You' 🥰",
                "The moment I knew for sure - you're the one.",
                "", "milestone");
    }

    public void insertSampleData() {
        // Check if data already exists
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TIMELINE, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();

        if (count > 0) return; // Data already exists

        try {
            // Load JSON data from the assets folder
            InputStream is = this.context.getAssets().open("timeline_events.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");

            // Parse JSON array
            JSONArray timelineArray = new JSONArray(jsonString);

            // Insert timeline events from JSON
            for (int i = 0; i < timelineArray.length(); i++) {
                JSONObject eventObject = timelineArray.getJSONObject(i);
                String date = eventObject.getString("date");
                String title = eventObject.getString("title");
                String description = eventObject.getString("description");
                String type = eventObject.getString("type");
                insertTimelineEvent(date, title, description, "", type);
            }

        } catch (IOException e) {
            Log.e("DBHelper", "Error reading timeline JSON file", e);
        } catch (JSONException e) {
            Log.e("DBHelper", "Error parsing timeline JSON", e);
        }
    }

    private void loadMessagesFromFile() {
        try {
            InputStream is = context.getAssets().open("messages.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            int dayOffset = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String messageType = parts[0];
                    String messageText = parts[1];

                    // Calculate date (today + dayOffset)
                    long timeMillis = System.currentTimeMillis() + (dayOffset * 24 * 60 * 60 * 1000L);
                    String date = sdf.format(new Date(timeMillis));

                    insertMessage(messageText, messageType, date);
                    dayOffset++;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
            * Check if a message exists for the given date
 */
    public boolean hasMessageForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{COLUMN_MESSAGE_ID},
                COLUMN_MESSAGE_DATE + "=?",
                new String[]{date},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Get all messages (for debugging or display)
     */

    /**
     * Check if a timeline event exists for the given date and title
     */
    public boolean hasTimelineEventForDateAndTitle(String date, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TIMELINE,
                new String[]{COLUMN_TIMELINE_ID},
                COLUMN_EVENT_DATE + "=? AND " + COLUMN_EVENT_TITLE + "=?",
                new String[]{date, title},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Get timeline events count (for debugging)
     */
    public int getTimelineEventsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TIMELINE, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MESSAGES,
                null, null, null, null, null,
                COLUMN_MESSAGE_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TEXT)));
                message.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TYPE)));
                message.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_DATE)));
                message.setUnlocked(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_UNLOCKED)) == 1);
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messages;
    }
    
    // Firebase compatibility methods
    public boolean messageExists(String text, String unlockDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{COLUMN_MESSAGE_ID},
                COLUMN_MESSAGE_TEXT + "=? AND " + COLUMN_MESSAGE_DATE + "=?",
                new String[]{text, unlockDate},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    
    public boolean addMessage(Message message) {
        long result = insertMessage(message.getText(), message.getType(), message.getUnlockDate());
        return result != -1;
    }
    
    public boolean timelineEventExists(String title, String date) {
        return hasTimelineEventForDateAndTitle(date, title);
    }
    
    public boolean addTimelineEvent(TimelineEvent event) {
        long result = insertTimelineEvent(event.getDate(), event.getTitle(), 
            event.getDescription(), event.getPhotos(), event.getType());
        return result != -1;
    }
    
    // Firebase complete sync methods
    public void clearAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.close();
        Log.d("DatabaseHelper", "All messages cleared for Firebase sync");
    }
    
    public void clearAllTimelineEvents() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMELINE, null, null);
        db.close();
        Log.d("DatabaseHelper", "All timeline events cleared for Firebase sync");
    }
}