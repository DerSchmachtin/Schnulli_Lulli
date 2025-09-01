package com.martin.love_application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMELINE);
        onCreate(db);
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

        Cursor cursor = db.query(TABLE_MESSAGES,
                null,
                COLUMN_MESSAGE_DATE + "=?",
                new String[]{today},
                null, null, null);

        Message message = null;
        if (cursor.moveToFirst()) {
            message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_MESSAGE_ID)));
            message.setText(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TEXT)));
            message.setType(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TYPE)));
            message.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_DATE)));
            message.setUnlocked(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_UNLOCKED)) == 1);
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
                event.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_TIMELINE_ID)));
                event.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DATE)));
                event.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE)));
                event.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DESCRIPTION)));
                event.setPhotos(cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHOTOS)));
                event.setType(cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TYPE)));
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
    public void insertSampleData() {
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
}