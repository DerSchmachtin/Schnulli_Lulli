package com.martin.love_application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView dayCounter;
    private CardView dailyMessageCard;
    private CardView timelineCard;
    private Button viewMessageButton;
    private Button viewTimelineButton;

    private DatabaseHelper dbHelper;
    private NotificationHelper notificationHelper;

    // Your relationship start date - UPDATE THIS!
    private static final String RELATIONSHIP_START = "2024-11-01"; // Format: YYYY-MM-DD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupDatabase();
        setupNotifications();
        updateUI();
        triggerTestNotification();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcome_text);
        dayCounter = findViewById(R.id.day_counter);
        dailyMessageCard = findViewById(R.id.daily_message_card);
        timelineCard = findViewById(R.id.timeline_card);
        viewMessageButton = findViewById(R.id.view_message_button);
        viewTimelineButton = findViewById(R.id.view_timeline_button);

        // Set click listeners
        viewMessageButton.setOnClickListener(v -> openDailyMessage());
        viewTimelineButton.setOnClickListener(v -> openTimeline());
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        // Pre-populate with your messages and timeline events
        dbHelper.insertSampleData();
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

    private void openDailyMessage() {
//        triggerTestNotification();

        Intent intent = new Intent(this, DailyMessageActivity.class);
        startActivity(intent);
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

    private void triggerTestNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "LOVE_MESSAGES")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // sicheres Test-Icon
                .setContentTitle("Test ❤️")
                .setContentText("Deine erste Notification ist da!")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // hoch setzen, damit sichtbar
                .setAutoCancel(false); // verschwindet nach Klick

        notificationManager.notify(1, builder.build());
    }
}