package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddDestinationActivity extends AppCompatActivity {

    private EditText editTextDestinationName;
    private EditText editTextImageUrl;
    private Button buttonAddDestination;

    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_destination);

        editTextDestinationName = findViewById(R.id.editTextDestinationName);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        buttonAddDestination = findViewById(R.id.buttonAddDestination);

        database = FirebaseFirestore.getInstance();

        // Set up back button functionality
        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(AddDestinationActivity.this, AdminMainActivity.class));
        });

        // Set up Add Destination button click listener
        buttonAddDestination.setOnClickListener(v -> addDestination());
    }

    private void addDestination() {
        String destinationName = editTextDestinationName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(destinationName)) {
            editTextDestinationName.setError("Destination name is required");
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            editTextImageUrl.setError("Image URL is required");
            return;
        }

        if (!isValidImageUrl(imageUrl)) {
            editTextImageUrl.setError("Invalid image URL");
            return;
        }

        // Check if destination name already exists
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Destination already exists
                        Toast.makeText(AddDestinationActivity.this, "Destination already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Destination does not exist, add it to Firestore
                        Map<String, Object> destination = new HashMap<>();
                        destination.put(Constants.KEY_NAME, destinationName);
                        destination.put(Constants.KEY_IMAGE_URL, imageUrl);

                        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                                .document(destinationName)
                                .set(destination)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddDestinationActivity.this, "Destination added successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(AddDestinationActivity.this, AdminMainActivity.class));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddDestinationActivity.this, "Failed to add destination", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddDestinationActivity.this, "Failed to check destination existence", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidImageUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
}

