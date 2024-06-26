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

public class EditDestinationItemActivity extends AppCompatActivity {

    private EditText editTextDestinationName;
    private EditText editTextImageUrl;
    private Button buttonUpdateDestination;

    private FirebaseFirestore database;
    private String currentDestinationName;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destination_item);

        editTextDestinationName = findViewById(R.id.editTextDestinationName);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        buttonUpdateDestination = findViewById(R.id.buttonUpdateDestination);

        database = FirebaseFirestore.getInstance();

        currentDestinationName = getIntent().getStringExtra(Constants.KEY_DESTINATION_NAME);
        currentImageUrl = getIntent().getStringExtra(Constants.KEY_IMAGE_URL);

        editTextDestinationName.setText(currentDestinationName);
        editTextImageUrl.setText(currentImageUrl);

        buttonUpdateDestination.setOnClickListener(v -> updateDestination());

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(EditDestinationItemActivity.this, AdminMainActivity.class));
        });
    }

    private void updateDestination() {
        String newDestinationName = editTextDestinationName.getText().toString().trim();
        String newImageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(newDestinationName)) {
            editTextDestinationName.setError("Destination name is required");
            return;
        }

        if (TextUtils.isEmpty(newImageUrl)) {
            editTextImageUrl.setError("Image URL is required");
            return;
        }

        if (!isValidImageUrl(newImageUrl)) {
            editTextImageUrl.setError("Invalid image URL");
            return;
        }

        // Check if destination name has been changed
        if (!newDestinationName.equals(currentDestinationName)) {
            // Check if the new destination name already exists
            database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                    .document(newDestinationName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Destination name already exists
                            Toast.makeText(EditDestinationItemActivity.this, "Destination name already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed with update
                            performUpdate(newDestinationName, newImageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditDestinationItemActivity.this, "Failed to check destination existence", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Proceed with update
            performUpdate(newDestinationName, newImageUrl);
        }
    }

    private void performUpdate(String destinationName, String imageUrl) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(Constants.KEY_NAME, destinationName);
        updateData.put(Constants.KEY_IMAGE_URL, imageUrl);

        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(currentDestinationName)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditDestinationItemActivity.this, "Destination updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditDestinationItemActivity.this, "Failed to update destination", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidImageUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
}
