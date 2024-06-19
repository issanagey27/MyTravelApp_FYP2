package com.example.mytravelapp.models;

public class Plans {
    private String name;
    private String imageUrl; // Add imageUrl field
    private String destination;

    public Plans() {
        // Default constructor required for Firestore
    }

    // Getter and setter methods for name field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add getter and setter method for imageUrl field
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}



