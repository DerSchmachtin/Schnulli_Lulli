package com.martin.love_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyMessageActivity extends AppCompatActivity {

    private TextView messageText;
    private TextView messageDate;
    private TextView messageType;
    private MaterialButton backButton;
    private MaterialButton heartButton;
    private ImageView messageIcon;
    private MaterialCardView messageCard;
    private LinearLayout mainLayout;

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
        messageText = findViewById(R.id.message_text);
        messageDate = findViewById(R.id.message_date);
        messageType = findViewById(R.id.message_type);
        backButton = findViewById(R.id.back_button);
        heartButton = findViewById(R.id.heart_button);
        messageIcon = findViewById(R.id.message_icon);
        
        // Try to find the views - they should exist
        try {
            messageCard = findViewById(R.id.message_card);
            mainLayout = findViewById(R.id.main_layout);
        } catch (Exception e) {
            android.util.Log.e("DailyMessage", "Views not found: " + e.getMessage());
        }

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        heartButton.setOnClickListener(v -> toggleHeart());
    }

    private void setupDatabase() {
        dbHelper = DatabaseHelper.getInstance(this);
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

    private void applyColorTheme(String messageType) {
        int backgroundColor, textColor, strokeColor;
        
        switch (messageType) {
            case "love_note":
                backgroundColor = getResources().getColor(R.color.love_note_bg);
                textColor = getResources().getColor(R.color.love_note_text);
                strokeColor = getResources().getColor(R.color.love_note_text);
                break;
            case "memory":
                backgroundColor = getResources().getColor(R.color.memory_bg);
                textColor = getResources().getColor(R.color.memory_text);
                strokeColor = getResources().getColor(R.color.memory_text);
                break;
            case "appreciation":
                backgroundColor = getResources().getColor(R.color.appreciation_bg);
                textColor = getResources().getColor(R.color.appreciation_text);
                strokeColor = getResources().getColor(R.color.appreciation_text);
                break;
            case "inside_joke":
                backgroundColor = getResources().getColor(R.color.inside_joke_bg);
                textColor = getResources().getColor(R.color.inside_joke_text);
                strokeColor = getResources().getColor(R.color.inside_joke_text);
                break;
            case "future_dream":
                backgroundColor = getResources().getColor(R.color.future_dream_bg);
                textColor = getResources().getColor(R.color.future_dream_text);
                strokeColor = getResources().getColor(R.color.future_dream_text);
                break;
            case "gratitude":
                backgroundColor = getResources().getColor(R.color.gratitude_bg);
                textColor = getResources().getColor(R.color.gratitude_text);
                strokeColor = getResources().getColor(R.color.gratitude_text);
                break;
            case "encouragement":
                backgroundColor = getResources().getColor(R.color.encouragement_bg);
                textColor = getResources().getColor(R.color.encouragement_text);
                strokeColor = getResources().getColor(R.color.encouragement_text);
                break;
            case "seasonal":
                backgroundColor = getResources().getColor(R.color.seasonal_bg);
                textColor = getResources().getColor(R.color.seasonal_text);
                strokeColor = getResources().getColor(R.color.seasonal_text);
                break;
            case "sweet":
                backgroundColor = getResources().getColor(R.color.sweet_bg);
                textColor = getResources().getColor(R.color.sweet_text);
                strokeColor = getResources().getColor(R.color.sweet_text);
                break;
            default:
                // Default to love_note theme
                backgroundColor = getResources().getColor(R.color.love_note_bg);
                textColor = getResources().getColor(R.color.love_note_text);
                strokeColor = getResources().getColor(R.color.love_note_text);
                break;
        }
        
        // Apply colors to views - focus on text color only for now
        if (messageText != null) {
            messageText.setTextColor(textColor);
        }
        
        // Only apply card colors if we found the card
        if (messageCard != null) {
            try {
                messageCard.setCardBackgroundColor(backgroundColor);
                messageCard.setStrokeColor(strokeColor);
            } catch (Exception e) {
                android.util.Log.e("DailyMessage", "Error setting card colors: " + e.getMessage());
            }
        }
        
        // Only apply background if we found the main layout
        if (mainLayout != null) {
            try {
                mainLayout.setBackgroundColor(backgroundColor);
            } catch (Exception e) {
                android.util.Log.e("DailyMessage", "Error setting background: " + e.getMessage());
            }
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