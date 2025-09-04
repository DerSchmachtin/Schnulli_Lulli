package com.martin.love_application;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FirebaseDataManager {
    
    private static final String TAG = "FirebaseDataManager";
    private Context context;
    private DatabaseHelper dbHelper;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference messagesRef;
    private DatabaseReference timelineRef;
    
    public interface DataUpdateCallback {
        void onSuccess(int newItemsCount);
        void onError(String error);
    }
    
    public FirebaseDataManager(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        
        // Initialize Firebase with explicit database URL
        this.firebaseDatabase = FirebaseDatabase.getInstance("https://love-application-78ecb-default-rtdb.europe-west1.firebasedatabase.app");
        
        // Enable offline persistence (helps with performance)
        try {
            firebaseDatabase.setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.w(TAG, "Firebase persistence already enabled or failed: " + e.getMessage());
        }
        
        this.messagesRef = firebaseDatabase.getReference("messages");
        this.timelineRef = firebaseDatabase.getReference("timeline_events");
        
        // Add connection logging
        firebaseDatabase.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "Firebase connected: " + connected);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Firebase connection listener cancelled: " + error.getMessage());
            }
        });
        
        Log.d(TAG, "FirebaseDataManager initialized with debugging");
    }
    
    public void fetchAndUpdateMessages(DataUpdateCallback callback) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available - skipping messages sync");
            callback.onError("Keine Internetverbindung");
            return;
        }
        
        Log.d(TAG, "Syncing messages from Firebase...");
        long startTime = System.currentTimeMillis();
        
        // Get current local messages to compare
        List<Message> localMessages = dbHelper.getAllMessages();
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<Message> firebaseMessages = new ArrayList<>();
                    
                    // Get all messages from Firebase
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String text = messageSnapshot.child("text").getValue(String.class);
                        String type = messageSnapshot.child("type").getValue(String.class);
                        String unlockDate = messageSnapshot.child("unlock_date").getValue(String.class);
                        
                        if (text != null && type != null && unlockDate != null) {
                            Message message = new Message(text, type, unlockDate);
                            firebaseMessages.add(message);
                        }
                    }
                    
                    // Compare with local data
                    boolean dataChanged = !messagesAreEqual(localMessages, firebaseMessages);
                    
                    if (dataChanged || firebaseMessages.size() != localMessages.size()) {
                        // Clear all local messages and replace with Firebase data
                        dbHelper.clearAllMessages();
                        
                        // Add all Firebase messages to local database
                        for (Message message : firebaseMessages) {
                            dbHelper.addMessage(message);
                        }
                        
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "Messages synchronized - " + firebaseMessages.size() + " total messages in " + (endTime - startTime) + "ms");
                        callback.onSuccess(firebaseMessages.size());
                    } else {
                        // No changes
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "Messages are already up to date - " + firebaseMessages.size() + " messages in " + (endTime - startTime) + "ms");
                        callback.onSuccess(0); // 0 indicates no changes
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing messages: " + e.getMessage(), e);
                    callback.onError("Fehler beim Synchronisieren der Nachrichten: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase messages sync cancelled: " + databaseError.getMessage());
                callback.onError("Firebase Fehler: " + databaseError.getMessage());
            }
        });
    }
    
    public void fetchAndUpdateTimeline(DataUpdateCallback callback) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available - skipping timeline sync");
            callback.onError("Keine Internetverbindung");
            return;
        }
        
        Log.d(TAG, "Syncing timeline events from Firebase...");
        long startTime = System.currentTimeMillis();
        
        // Get current local timeline events to compare
        List<TimelineEvent> localEvents = dbHelper.getAllTimelineEvents();
        
        timelineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<TimelineEvent> firebaseEvents = new ArrayList<>();
                    
                    // Get all events from Firebase
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String title = eventSnapshot.child("title").getValue(String.class);
                        String date = eventSnapshot.child("date").getValue(String.class);
                        String type = eventSnapshot.child("type").getValue(String.class);
                        String description = eventSnapshot.child("description").getValue(String.class);
                        
                        if (title != null && date != null && type != null) {
                            TimelineEvent event = new TimelineEvent(title, date, type);
                            if (description != null) {
                                event.setDescription(description);
                            }
                            firebaseEvents.add(event);
                        }
                    }
                    
                    // Compare with local data
                    boolean dataChanged = !timelineEventsAreEqual(localEvents, firebaseEvents);
                    
                    if (dataChanged || firebaseEvents.size() != localEvents.size()) {
                        // Clear all local timeline events and replace with Firebase data
                        dbHelper.clearAllTimelineEvents();
                        
                        // Add all Firebase events to local database
                        for (TimelineEvent event : firebaseEvents) {
                            dbHelper.addTimelineEvent(event);
                        }
                        
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "Timeline events synchronized - " + firebaseEvents.size() + " total events in " + (endTime - startTime) + "ms");
                        callback.onSuccess(firebaseEvents.size());
                    } else {
                        // No changes
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "Timeline events are already up to date - " + firebaseEvents.size() + " events in " + (endTime - startTime) + "ms");
                        callback.onSuccess(0); // 0 indicates no changes
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing timeline events: " + e.getMessage(), e);
                    callback.onError("Fehler beim Synchronisieren der Timeline-Events: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase timeline sync cancelled: " + databaseError.getMessage());
                callback.onError("Firebase Fehler: " + databaseError.getMessage());
            }
        });
    }
    
    // Method to add a new message to Firebase (for testing/admin use)
    public void addMessageToFirebase(String text, String type, String unlockDate) {
        DatabaseReference newMessageRef = messagesRef.push();
        
        newMessageRef.child("text").setValue(text);
        newMessageRef.child("type").setValue(type);
        newMessageRef.child("unlock_date").setValue(unlockDate);
        
        Log.d(TAG, "Added message to Firebase: " + text);
    }
    
    // Method to add a new timeline event to Firebase (for testing/admin use)
    public void addTimelineEventToFirebase(String title, String date, String type, String description) {
        DatabaseReference newEventRef = timelineRef.push();
        
        newEventRef.child("title").setValue(title);
        newEventRef.child("date").setValue(date);
        newEventRef.child("type").setValue(type);
        if (description != null) {
            newEventRef.child("description").setValue(description);
        }
        
        Log.d(TAG, "Added timeline event to Firebase: " + title);
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    private boolean messagesAreEqual(List<Message> local, List<Message> firebase) {
        if (local.size() != firebase.size()) {
            return false;
        }
        
        for (Message localMsg : local) {
            boolean found = false;
            for (Message firebaseMsg : firebase) {
                if (localMsg.getText().equals(firebaseMsg.getText()) &&
                    localMsg.getType().equals(firebaseMsg.getType()) &&
                    localMsg.getUnlockDate().equals(firebaseMsg.getUnlockDate())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    private boolean timelineEventsAreEqual(List<TimelineEvent> local, List<TimelineEvent> firebase) {
        if (local.size() != firebase.size()) {
            return false;
        }
        
        for (TimelineEvent localEvent : local) {
            boolean found = false;
            for (TimelineEvent firebaseEvent : firebase) {
                if (localEvent.getTitle().equals(firebaseEvent.getTitle()) &&
                    localEvent.getDate().equals(firebaseEvent.getDate()) &&
                    localEvent.getType().equals(firebaseEvent.getType()) &&
                    ((localEvent.getDescription() == null && firebaseEvent.getDescription() == null) ||
                     (localEvent.getDescription() != null && localEvent.getDescription().equals(firebaseEvent.getDescription())))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    public void shutdown() {
        Log.d(TAG, "FirebaseDataManager shutdown");
        // Firebase connections are automatically managed
    }
}