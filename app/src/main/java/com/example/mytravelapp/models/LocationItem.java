package com.example.mytravelapp.models;

public class LocationItem {

    private String documentId;
    private String name;
    private String imageUrl;
    private String subcollection;

    public LocationItem(String documentId, String name, String imageUrl, String subcollection) {
        this.documentId = documentId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.subcollection = subcollection;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSubcollection() {
        return subcollection;
    }
}
