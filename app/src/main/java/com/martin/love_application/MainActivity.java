package com.martin.love_application;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView dayCounter;
    private MaterialCardView cardFront;
    private MaterialCardView cardBack;
    private CardView timelineCard;
    private MaterialButton viewTimelineButton;
    private MaterialButton syncButtonMain;
    
    
    // Daily message views (back card)
    private TextView messageText;
    private TextView messageDate;
    private TextView messageType;
    private ImageView messageIcon;
    private LinearLayout mainLayout;
    
    // Flip animation state
    private boolean isShowingFront = true;

    private DatabaseHelper dbHelper;
    private NotificationHelper notificationHelper;
    private FirebaseDataManager firebaseManager;

    // Your relationship start date - UPDATE THIS!
    private static final String RELATIONSHIP_START = "2024-11-01"; // Format: YYYY-MM-DD
    
    // Permission request code
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase explicitly
        FirebaseApp.initializeApp(this);
        
        setContentView(R.layout.activity_main);

        initializeViews();
        setupDatabase();
        checkNotificationPermission();
        setupNotifications();
        updateUI();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcome_text);
        dayCounter = findViewById(R.id.day_counter);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        timelineCard = findViewById(R.id.timeline_card);
        viewTimelineButton = findViewById(R.id.view_timeline_button);
        syncButtonMain = findViewById(R.id.sync_button_main);
        
        // Daily message views (from back card)
        messageText = findViewById(R.id.message_text_back);
        messageDate = findViewById(R.id.message_date_back);
        messageType = findViewById(R.id.message_type_back);
        messageIcon = findViewById(R.id.message_icon_back);
        mainLayout = findViewById(R.id.main_layout);

        // Set click listeners
        viewTimelineButton.setOnClickListener(v -> openTimeline());
        cardFront.setOnClickListener(v -> flipCard());
        cardBack.setOnClickListener(v -> flipCard());
        syncButtonMain.setOnClickListener(v -> manualSync());
        
        // Set camera distance for 3D rotation (farther camera = less perspective distortion)
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
    }

    private void setupDatabase() {
        dbHelper = DatabaseHelper.getInstance(this);
        firebaseManager = new FirebaseDataManager(this);

        // Show sync status to user
        Toast.makeText(this, "🔥 Synchronisiere mit Firebase...", Toast.LENGTH_SHORT).show();
        
        // Always fetch data from Firebase on startup
        fetchNewDataFromFirebase();
    }

    // 👈 Updated method to fetch both messages and timeline events from Firebase
    private void fetchNewDataFromFirebase() {
        Log.d("MainActivity", "Attempting to fetch new data from Firebase...");

        // Add timeout for Firebase operations
        android.os.Handler timeoutHandler = new android.os.Handler();
        final boolean[] syncCompleted = {false};
        
        // Set timeout of 10 seconds
        timeoutHandler.postDelayed(() -> {
            if (!syncCompleted[0]) {
                Log.w("MainActivity", "Firebase sync timeout - using local data");
                Toast.makeText(MainActivity.this, "⚠️ Sync timeout - lokale Daten werden verwendet", Toast.LENGTH_SHORT).show();
                loadTodaysMessage();
                syncCompleted[0] = true;
            }
        }, 10000);

        // Fetch messages
        firebaseManager.fetchAndUpdateMessages(new FirebaseDataManager.DataUpdateCallback() {
            @Override
            public void onSuccess(int newMessagesCount) {
                if (!syncCompleted[0]) {
                    syncCompleted[0] = true;
                    timeoutHandler.removeCallbacksAndMessages(null);
                    
                    // Reset sync button
                    if (syncButtonMain != null) {
                        syncButtonMain.setEnabled(true);
                        syncButtonMain.setText("🔄 Aktualisieren");
                    }
                    
                    if (newMessagesCount > 0) {
                        Log.d("MainActivity", "Successfully added " + newMessagesCount + " new messages");
                        Toast.makeText(MainActivity.this,
                                "❤️ " + newMessagesCount + " neue Nachrichten hinzugefügt!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "✅ Nachrichten sind aktuell", Toast.LENGTH_SHORT).show();
                    }
                    // Reload today's message after successful sync
                    loadTodaysMessage();
                }
            }

            @Override
            public void onError(String error) {
                if (!syncCompleted[0]) {
                    syncCompleted[0] = true;
                    timeoutHandler.removeCallbacksAndMessages(null);
                    
                    // Reset sync button
                    if (syncButtonMain != null) {
                        syncButtonMain.setEnabled(true);
                        syncButtonMain.setText("🔄 Aktualisieren");
                    }
                    
                    Log.e("MainActivity", "Failed to fetch messages: " + error);
                    Toast.makeText(MainActivity.this, "❌ Firebase Sync fehlgeschlagen: " + error, Toast.LENGTH_SHORT).show();
                    // Still try to load today's message from local data
                    loadTodaysMessage();
                }
            }
        });

        // Fetch timeline events
        firebaseManager.fetchAndUpdateTimeline(new FirebaseDataManager.DataUpdateCallback() {
            @Override
            public void onSuccess(int newEventsCount) {
                if (newEventsCount > 0) {
                    Log.d("MainActivity", "Successfully added " + newEventsCount + " new timeline events");
                    Toast.makeText(MainActivity.this,
                            "❤️ " + newEventsCount + " neue Timeline-Events hinzugefügt!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("MainActivity", "Failed to fetch timeline events: " + error);
            }
        });
    }

    // 👈 Add this optional method if you want to refresh UI after new messages
    private void refreshMessageDisplay() {
        // This would update any UI elements that show message counts or status
        // For now, your app probably doesn't need this since messages are unlocked daily
        Log.d("MainActivity", "UI refreshed after new messages added");
    }

    private void setupNotifications() {
        notificationHelper = new NotificationHelper(this);
        createNotificationChannel();
        // Schedule daily notifications (user can customize time later)
        notificationHelper.cancelDailyNotification();
        notificationHelper.scheduleDailyNotification();
    }

    private void updateUI() {
        // Calculate days together
        int daysTogether = calculateDaysTogether();
        dayCounter.setText(daysTogether + " tolle Tage mit dir ❤️");

        // Personalize welcome message
        String[] welcomeMessages = {
                "Guten Morgen, Hübschlie! ☀️",
                "Na Süße! 💕",
                "Ich vermisse dich jetzt schon 🥰",
                "Ich wünsche dir einen schönen Tag! ✨"
        };

        int randomIndex = (int) (Math.random() * welcomeMessages.length);
        welcomeText.setText(welcomeMessages[randomIndex]);
    }

    private int calculateDaysTogether() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(RELATIONSHIP_START);
            Date currentDate = new Date();

            long timeDiff = currentDate.getTime() - startDate.getTime();
            return (int) (timeDiff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            return 0;
        }
    }

    private void loadTodaysMessage() {
        Message todaysMessage = dbHelper.getTodaysMessage();

        if (todaysMessage != null) {
            // Display the message
            messageText.setText(todaysMessage.getText());

            // Set hidden fields for data
            String today = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(new Date());
            messageDate.setText(today);
            messageType.setText(todaysMessage.getType());

            // Set appropriate icon based on message type
            setMessageIcon(todaysMessage.getType());

            // Apply color theme based on message type
            applyColorTheme(todaysMessage.getType());

            // Mark message as unlocked if not already
            if (!todaysMessage.isUnlocked()) {
                dbHelper.unlockTodaysMessage();
            }

        } else {
            // No message for today - suggest syncing
            messageText.setText("Keine Nachricht für heute gefunden. Bitte synchronisiere die App um neue Nachrichten zu erhalten ❤️");
            messageDate.setText("Heute");
            messageType.setText("sync_needed");
            messageIcon.setImageResource(R.drawable.ic_heart);
            applyColorTheme("love_note"); // Default theme
        }
    }

    private void setMessageIcon(String type) {
        int iconResource;
        switch (type) {
            case "love_note":
                iconResource = R.drawable.ic_heart;
                break;
            case "memory":
                iconResource = R.drawable.ic_cloud;
                break;
            case "appreciation":
                iconResource = R.drawable.ic_star;
                break;
            case "inside_joke":
                iconResource = R.drawable.ic_smile;
                break;
            case "future_dream":
                iconResource = R.drawable.ic_rocket;
                break;
            case "gratitude":
                iconResource = R.drawable.ic_thumbs_up;
                break;
            case "encouragement":
                iconResource = R.drawable.ic_trophy;
                break;
            default:
                iconResource = R.drawable.ic_heart;
                break;
        }

        try {
            messageIcon.setImageResource(iconResource);
        } catch (Exception e) {
            // Fallback to heart if icon doesn't exist
            messageIcon.setImageResource(android.R.drawable.ic_dialog_info);
        }
    }

    private void applyColorTheme(String messageType) {
        int backgroundColor, textColor, strokeColor;
        
        switch (messageType) {
            case "love_note":
                backgroundColor = ContextCompat.getColor(this, R.color.love_note_bg);
                textColor = ContextCompat.getColor(this, R.color.love_note_text);
                strokeColor = ContextCompat.getColor(this, R.color.love_note_text);
                break;
            case "memory":
                backgroundColor = ContextCompat.getColor(this, R.color.memory_bg);
                textColor = ContextCompat.getColor(this, R.color.memory_text);
                strokeColor = ContextCompat.getColor(this, R.color.memory_text);
                break;
            case "appreciation":
                backgroundColor = ContextCompat.getColor(this, R.color.appreciation_bg);
                textColor = ContextCompat.getColor(this, R.color.appreciation_text);
                strokeColor = ContextCompat.getColor(this, R.color.appreciation_text);
                break;
            case "inside_joke":
                backgroundColor = ContextCompat.getColor(this, R.color.inside_joke_bg);
                textColor = ContextCompat.getColor(this, R.color.inside_joke_text);
                strokeColor = ContextCompat.getColor(this, R.color.inside_joke_text);
                break;
            case "future_dream":
                backgroundColor = ContextCompat.getColor(this, R.color.future_dream_bg);
                textColor = ContextCompat.getColor(this, R.color.future_dream_text);
                strokeColor = ContextCompat.getColor(this, R.color.future_dream_text);
                break;
            case "gratitude":
                backgroundColor = ContextCompat.getColor(this, R.color.gratitude_bg);
                textColor = ContextCompat.getColor(this, R.color.gratitude_text);
                strokeColor = ContextCompat.getColor(this, R.color.gratitude_text);
                break;
            case "encouragement":
                backgroundColor = ContextCompat.getColor(this, R.color.encouragement_bg);
                textColor = ContextCompat.getColor(this, R.color.encouragement_text);
                strokeColor = ContextCompat.getColor(this, R.color.encouragement_text);
                break;
            case "seasonal":
                backgroundColor = ContextCompat.getColor(this, R.color.seasonal_bg);
                textColor = ContextCompat.getColor(this, R.color.seasonal_text);
                strokeColor = ContextCompat.getColor(this, R.color.seasonal_text);
                break;
            case "sweet":
                backgroundColor = ContextCompat.getColor(this, R.color.sweet_bg);
                textColor = ContextCompat.getColor(this, R.color.sweet_text);
                strokeColor = ContextCompat.getColor(this, R.color.sweet_text);
                break;
            default:
                // Default to love_note theme
                backgroundColor = ContextCompat.getColor(this, R.color.love_note_bg);
                textColor = ContextCompat.getColor(this, R.color.love_note_text);
                strokeColor = ContextCompat.getColor(this, R.color.love_note_text);
                break;
        }
        
        // Apply colors to views
        if (messageText != null) {
            messageText.setTextColor(textColor);
        }
        
        if (cardBack != null) {
            try {
                cardBack.setCardBackgroundColor(backgroundColor);
                cardBack.setStrokeColor(strokeColor);
            } catch (Exception e) {
                Log.e("MainActivity", "Error setting card colors: " + e.getMessage());
            }
        }
    }


    private void flipCard() {
        if (isShowingFront) {
            // Flip to back (reveal message)
            AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
            AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_in);
            
            flipOut.setTarget(cardFront);
            flipIn.setTarget(cardBack);
            
            // Show back card and start flip in animation after half rotation
            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cardFront.setVisibility(View.GONE);
                    cardBack.setVisibility(View.VISIBLE);
                    flipIn.start();
                }
            });
            
            flipOut.start();
            isShowingFront = false;
            
            // Mark message as viewed when revealing it
            if (notificationHelper != null) {
                notificationHelper.markMessageViewed(this);
            }
        } else {
            // Flip to front (hide message)
            AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out);
            AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
            
            flipOut.setTarget(cardBack);
            flipIn.setTarget(cardFront);
            
            // Show front card and start flip in animation after half rotation
            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cardBack.setVisibility(View.GONE);
                    cardFront.setVisibility(View.VISIBLE);
                    flipIn.start();
                }
            });
            
            flipOut.start();
            isShowingFront = true;
        }
    }

    private void manualSync() {
        syncButtonMain.setEnabled(false);
        syncButtonMain.setText("🔄 Synchronisiere...");
        
        Toast.makeText(this, "🔄 Aktualisiere Daten...", Toast.LENGTH_SHORT).show();
        
        // Manual sync - same logic as automatic sync but with user feedback
        fetchNewDataFromFirebase();
        
        // Re-enable button after delay (Firebase callbacks will also handle this)
        new android.os.Handler().postDelayed(() -> {
            if (syncButtonMain != null) {
                syncButtonMain.setEnabled(true);
                syncButtonMain.setText("🔄 Aktualisieren");
            }
        }, 3000);
    }

    private void openTimeline() {
        Intent intent = new Intent(this, TimelineActivity.class);
        startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Love Messages";
            String description = "Daily reminders of how much you're loved";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("LOVE_MESSAGES", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                
                // Show rationale if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, 
                    android.Manifest.permission.POST_NOTIFICATIONS)) {
                    
                    // Show explanation to user
                    Toast.makeText(this, 
                        "Benachrichtigungen sind nötig für deine täglichen Liebesnachrichten ❤️", 
                        Toast.LENGTH_LONG).show();
                }
                
                // Request permission
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Perfekt! Du bekommst jetzt deine täglichen Liebesnachrichten 💕", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Schade! Du verpasst deine täglichen Liebesnachrichten. Du kannst die Berechtigung in den Einstellungen aktivieren ❤️", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    // 👈 Add cleanup when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseManager != null) {
            firebaseManager.shutdown();
        }
    }
}