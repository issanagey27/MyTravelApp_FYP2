package com.example.mytravelapp.models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public String id, name, email, image, token, location;
    public List<String> languages;
}
