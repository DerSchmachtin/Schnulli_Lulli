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
        Log.d(TAG, "Firebase database URL: " + firebaseDatabase.getReference().toString());
        Log.d(TAG, "Messages reference path: " + messagesRef.toString());
        long startTime = System.currentTimeMillis();
        
        // Get current local messages to compare
        List<Message> localMessages = dbHelper.getAllMessages();
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<Message> firebaseMessages = new ArrayList<>();
                    
                    Log.d(TAG, "Firebase messages node has " + dataSnapshot.getChildrenCount() + " children");
                    
                    // Get all messages from Firebase
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String text = messageSnapshot.child("text").getValue(String.class);
                        String type = messageSnapshot.child("type").getValue(String.class);
                        String unlockDate = messageSnapshot.child("unlock_date").getValue(String.class);
                        
                        if (text != null && type != null && unlockDate != null) {
                            Message message = new Message(text, type, unlockDate);
                            firebaseMessages.add(message);
                            Log.d(TAG, "Firebase message loaded: " + text + " (type: " + type + ", date: " + unlockDate + ")");
                        } else {
                            Log.w(TAG, "Incomplete message data - text: " + text + ", type: " + type + ", unlockDate: " + unlockDate);
                        }
                    }
                    
                    if (firebaseMessages.size() > 0) {
                        // Firebase hat Messages gefunden → Alle alten löschen, neue einfügen
                        Log.d(TAG, "Firebase Messages gefunden → Lösche alle alten Messages und füge " + firebaseMessages.size() + " neue hinzu");
                        dbHelper.clearAllMessages();
                        
                        int addedCount = 0;
                        for (Message message : firebaseMessages) {
                            boolean added = dbHelper.addMessage(message);
                            if (added) {
                                addedCount++;
                            }
                        }
                        
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "✅ Messages sync erfolgreich - " + addedCount + " Messages von Firebase geladen in " + (endTime - startTime) + "ms");
                        callback.onSuccess(addedCount);
                    } else {
                        // Keine Messages in Firebase
                        Log.d(TAG, "Keine Messages in Firebase gefunden - lokale Daten bleiben unverändert");
                        long endTime = System.currentTimeMillis();
                        callback.onSuccess(0);
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing messages: " + e.getMessage(), e);
                    callback.onError("Fehler beim Synchronisieren der Nachrichten: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase messages sync cancelled: " + databaseError.getMessage());
                Log.e(TAG, "Firebase error code: " + databaseError.getCode());
                Log.e(TAG, "Firebase error details: " + databaseError.getDetails());
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
        Log.d(TAG, "Firebase database URL: " + firebaseDatabase.getReference().toString());
        Log.d(TAG, "Timeline reference path: " + timelineRef.toString());
        long startTime = System.currentTimeMillis();
        
        // Get current local timeline events to compare
        List<TimelineEvent> localEvents = dbHelper.getAllTimelineEvents();
        
        timelineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<TimelineEvent> firebaseEvents = new ArrayList<>();
                    
                    Log.d(TAG, "Firebase timeline node has " + dataSnapshot.getChildrenCount() + " children");
                    
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
                            Log.d(TAG, "Firebase timeline event loaded: " + title + " (date: " + date + ", type: " + type + ")");
                        } else {
                            Log.w(TAG, "Incomplete timeline event data - title: " + title + ", date: " + date + ", type: " + type);
                        }
                    }
                    
                    if (firebaseEvents.size() > 0) {
                        // Firebase hat Events gefunden → Alle alten löschen, neue einfügen
                        Log.d(TAG, "Firebase Events gefunden → Lösche alle alten Events und füge " + firebaseEvents.size() + " neue hinzu");
                        dbHelper.clearAllTimelineEvents();
                        
                        int addedCount = 0;
                        for (TimelineEvent event : firebaseEvents) {
                            boolean added = dbHelper.addTimelineEvent(event);
                            if (added) {
                                addedCount++;
                            }
                        }
                        
                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "✅ Timeline sync erfolgreich - " + addedCount + " Events von Firebase geladen in " + (endTime - startTime) + "ms");
                        callback.onSuccess(addedCount);
                    } else {
                        // Keine Events in Firebase
                        Log.d(TAG, "Keine Events in Firebase gefunden - lokale Daten bleiben unverändert");
                        long endTime = System.currentTimeMillis();
                        callback.onSuccess(0);
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
    
    
    // Test Firebase connection and data structure
    public void testFirebaseConnection(DataUpdateCallback callback) {
        Log.d(TAG, "Testing Firebase connection and data structure...");
        
        // Test connection to root
        firebaseDatabase.getReference(".info/connected").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "Firebase connection test: " + connected);
                
                if (connected) {
                    // Test data structure - check if timeline_events node exists
                    timelineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Timeline node exists: " + dataSnapshot.exists());
                            Log.d(TAG, "Timeline children count: " + dataSnapshot.getChildrenCount());
                            
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Log.d(TAG, "Sample timeline data:");
                                int count = 0;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (count < 3) { // Show first 3 entries
                                        Log.d(TAG, "Event " + count + ": " + child.getValue());
                                        count++;
                                    }
                                }
                                callback.onSuccess((int) dataSnapshot.getChildrenCount());
                            } else {
                                Log.w(TAG, "Timeline node is empty or doesn't exist");
                                callback.onError("Timeline node ist leer oder existiert nicht");
                            }
                        }
                        
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Timeline test cancelled: " + databaseError.getMessage());
                            callback.onError("Timeline test fehlgeschlagen: " + databaseError.getMessage());
                        }
                    });
                } else {
                    callback.onError("Firebase Verbindung fehlgeschlagen");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Connection test cancelled: " + databaseError.getMessage());
                callback.onError("Verbindungstest fehlgeschlagen: " + databaseError.getMessage());
            }
        });
    }
    
    public void shutdown() {
        Log.d(TAG, "FirebaseDataManager shutdown");
        // Firebase connections are automatically managed
    }
}