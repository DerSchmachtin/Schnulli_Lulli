package com.martin.love_application;

public class TimelineEvent {
    private int id;
    private String date;
    private String title;
    private String description;
    private String photos; // Comma-separated photo paths
    private String type;

    // Constructors
    public TimelineEvent() {}

    public TimelineEvent(String date, String title, String description, String photos, String type) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.photos = photos;
        this.type = type;
    }
    
    // Constructor for Firebase compatibility
    public TimelineEvent(String title, String date, String type) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.description = "";
        this.photos = "";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Helper method to get photo paths as array
    public String[] getPhotoArray() {
        if (photos == null || photos.isEmpty()) {
            return new String[0];
        }
        return photos.split(",");
    }
}