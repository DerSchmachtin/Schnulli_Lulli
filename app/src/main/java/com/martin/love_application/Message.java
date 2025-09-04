package com.martin.love_application;

public class Message {
    private int id;
    private String text;
    private String type;
    private String date;
    private String unlockDate;
    private boolean isUnlocked;

    // Constructors
    public Message() {}

    // Constructor for Firebase/new data (uses unlockDate)
    public Message(String text, String type, String unlockDate) {
        this.text = text;
        this.type = type;
        this.unlockDate = unlockDate;
        this.date = unlockDate; // For compatibility
        this.isUnlocked = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public String getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(String unlockDate) {
        this.unlockDate = unlockDate;
    }
}