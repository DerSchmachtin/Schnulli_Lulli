package com.martin.love_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyMessageActivity extends AppCompatActivity {

    private TextView messageText;
    private TextView messageDate;
    private TextView messageType;
    private Button backButton;
    private Button heartButton;
    private ImageView messageIcon;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_message);

        initializeViews();
        setupDatabase();
        loadTodaysMessage();
    }

    private void initializeViews() {
        messageText = findViewById(R.id.view_message_button);
        messageDate = findViewById(R.id.message_date);
        messageType = findViewById(R.id.message_type);
        backButton = findViewById(R.id.back_button);
        heartButton = findViewById(R.id.heart_button);
        messageIcon = findViewById(R.id.message_icon);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        heartButton.setOnClickListener(v -> toggleHeart());
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void loadTodaysMessage() {
        Message todaysMessage = dbHelper.getTodaysMessage();

        if (todaysMessage != null) {
            // Display the message
            messageText.setText(todaysMessage.getText());

            // Format and display date
            String today = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(new Date());
            messageDate.setText(today);

            // Display message type with nice formatting
            String typeDisplay = formatMessageType(todaysMessage.getType());
            messageType.setText(typeDisplay);

            // Set appropriate icon based on message type
            setMessageIcon(todaysMessage.getType());

            // Mark message as unlocked if not already
            if (!todaysMessage.isUnlocked()) {
                dbHelper.unlockTodaysMessage();
            }

        } else {
            // No message for today
            messageText.setText("No message for today yet! Check back tomorrow ❤️");
            messageDate.setText("Today");
            messageType.setText("Coming Soon");
            messageIcon.setImageResource(R.drawable.ic_heart);
        }
    }

    private String formatMessageType(String type) {
        switch (type) {
            case "love_note": return "💕 Love Note";
            case "memory": return "📸 Sweet Memory";
            case "appreciation": return "🙏 Appreciation";
            case "inside_joke": return "😂 Inside Joke";
            case "future_dream": return "✨ Future Dream";
            case "gratitude": return "🌟 Gratitude";
            case "encouragement": return "💪 Encouragement";
            case "seasonal": return "🌈 Seasonal";
            case "sweet": return "💖 Sweet Thought";
            default: return "❤️ Love Message";
        }
    }

    private void setMessageIcon(String type) {
        int iconResource;
        switch (type) {
            case "love_note":
                iconResource = R.drawable.ic_heart;
                break;
            case "memory":
                iconResource = R.drawable.ic_camera;
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

        // Set icon (you'll need to add these drawable resources)
        try {
            messageIcon.setImageResource(iconResource);
        } catch (Exception e) {
            // Fallback to heart if icon doesn't exist
            messageIcon.setImageResource(android.R.drawable.ic_dialog_info);
        }
    }

    private void toggleHeart() {
        // Add heart animation or save as favorite
        heartButton.setText(heartButton.getText().equals("♡") ? "♥" : "♡");

        // You could save this as a favorite in the database
        // For now, just a visual toggle
    }

    // Method to get a random encouraging message if no specific message exists
    private String getRandomEncouragingMessage() {
        String[] encouragingMessages = {
                "You're absolutely amazing ❤️",
                "Hope you have the most wonderful day!",
                "Just thinking about you makes me smile 😊",
                "You're my favorite person in the whole world",
                "Sending you all my love today! 💕"
        };

        int randomIndex = (int) (Math.random() * encouragingMessages.length);
        return encouragingMessages[randomIndex];
    }
}