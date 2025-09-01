package com.martin.love_application;

public class Message {
    private int id;
    private String text;
    private String type;
    private String date;
    private boolean isUnlocked;

    // Constructors
    public Message() {}

    public Message(String text, String type, String date) {
        this.text = text;
        this.type = type;
        this.date = date;
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
}