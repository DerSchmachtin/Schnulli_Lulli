package com.martin.love_application;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import java.util.Calendar;

public class NotificationHelper extends BroadcastReceiver {

    private Context context;
    private static final String NOTIFICATION_CHANNEL_ID = "LOVE_MESSAGES";
    private static final int NOTIFICATION_ID = 1001;
    private static final String PREFS_NAME = "LoveAppPrefs";
    private static final String NOTIFICATION_TIME_HOUR = "notification_hour";
    private static final String NOTIFICATION_TIME_MINUTE = "notification_minute";

    public NotificationHelper() {
        // Default constructor for BroadcastReceiver
    }

    public NotificationHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the scheduled alarm triggers
        showDailyNotification(context);
    }

    public void scheduleDailyNotification() {
        // Get user's preferred notification time (default to 10:00 PM)
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 22); // 10 PM default
        int minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0); // 0 minutes default
        Log.d("NotificationHelper", "scheduleDailyNotification called for: " + hour + ":" + minute);
        scheduleDailyNotification(hour, minute);
    }

    public void scheduleDailyNotification(int hour, int minute) {
        // Save the notification time
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(NOTIFICATION_TIME_HOUR, hour);
        editor.putInt(NOTIFICATION_TIME_MINUTE, minute);
        editor.apply();

        // Create intent for the notification
        Intent intent = new Intent(context, NotificationHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set up the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Calculate the time for today's notification
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Log.d("NotificationHelper", "Scheduled for: " + calendar.getTime());


        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule repeating alarm
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void showDailyNotification(Context context) {
        // Check if message has been viewed today
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        String lastViewedDate = prefs.getString("last_viewed_date", "");
        
        // Only send notification if message hasn't been viewed today
        if (today.equals(lastViewedDate)) {
            Log.d("NotificationHelper", "Message already viewed today, skipping notification");
            return;
        }

        // Get today's message from database to check if one exists
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        Message todaysMessage = dbHelper.getTodaysMessage();

        String notificationTitle = "💕 Erinnerung an deine tägliche Nachricht";
        String notificationText = "Du hast heute noch nicht deine Liebesnachricht angeschaut! ❤️";

        // Don't spoil the message - just remind them to check
        if (todaysMessage == null) {
            notificationText = "Keine Nachricht für heute verfügbar. Bitte synchronisiere die App! 💕";
        }

        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create intent to directly open main activity (where daily message is now shown)
        Intent directIntent = new Intent(context, MainActivity.class);
        PendingIntent directPendingIntent = PendingIntent.getActivity(
                context,
                1,
                directIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_heart) // You'll need to create this
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .addAction(R.drawable.ic_heart, "Nachricht lesen", directPendingIntent);

        // Add some personality to the notification
        String[] expandedTexts = {
                "Jemand Besonderes denkt an dich! 💕",
                "Eine kleine Liebesnotiz nur für dich ❤️",
                "Deine tägliche Erinnerung daran, wie toll du bist! ✨",
                "Liebe liegt in der Luft... und in deinem Handy! 🥰"
        };

        int randomIndex = (int) (Math.random() * expandedTexts.length);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(notificationText)
                .setSummaryText(expandedTexts[randomIndex]));

        // Show the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void cancelDailyNotification() {
        Intent intent = new Intent(context, NotificationHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // Method to update notification time
    public void updateNotificationTime(int hour, int minute) {
        cancelDailyNotification();
        scheduleDailyNotification(hour, minute);
    }

    // Method to check if notifications are enabled
    public boolean areNotificationsEnabled() {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.areNotificationsEnabled();
        }
        return true; // Assume enabled for older versions
    }
    
    // Mark message as viewed today
    public void markMessageViewed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        prefs.edit().putString("last_viewed_date", today).apply();
        Log.d("NotificationHelper", "Message marked as viewed for: " + today);
    }
}