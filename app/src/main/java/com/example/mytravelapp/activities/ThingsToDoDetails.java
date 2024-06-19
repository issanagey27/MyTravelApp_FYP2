package com.example.mytravelapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ThingsToDoDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private String destinationId;
    private String thingsToDoId;
    private String userEmail;
    private String planName;

    private ImageView imageView;
    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewAddress;
    private TextView textViewType;
    private TextView textViewPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thingstodo_details);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        imageView = findViewById(R.id.imageView);
        textViewName = findViewById(R.id.textViewName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewType = findViewById(R.id.textViewType);
        textViewPrice = findViewById(R.id.textViewPrice);
        ImageButton saveToFavoritesButton = findViewById(R.id.saveToFavoritesButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destinationId = extras.getString("destinationId");
            thingsToDoId = extras.getString("thingsToDoId");
            userEmail = extras.getString("userEmail");
            planName = extras.getString("planName");

            // Log the received extras
            Log.d("ThingsToDoDetails", "Received extras:");
            Log.d("ThingsToDoDetails", "Destination ID: " + destinationId);
            Log.d("ThingsToDoDetails", "Things To Do ID: " + thingsToDoId);
            Log.d("ThingsToDoDetails", "User Email: " + userEmail);
            Log.d("ThingsToDoDetails", "Plan Name: " + planName);

            // Validate extras
            if (destinationId != null && thingsToDoId != null && userEmail != null && planName != null) {
                // Fetch things to do details from Firestore
                fetchThingsToDoDetails();

                // Set click listener for save button
                saveToFavoritesButton.setOnClickListener(v -> {
                    Log.d("ThingsToDoDetails", "Save to Favorites button clicked");
                    saveThingsToDoToFavorites();
                });
            } else {
                // Error: Required intent extras are missing
                handleMissingExtras();
            }
        } else {
            // Error: No intent extras provided
            Log.e("ThingsToDoDetails", "Error: No intent extras provided.");
            Toast.makeText(this, "Error: No intent extras provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }
    }

    private void fetchThingsToDoDetails() {
        if (destinationId == null || thingsToDoId == null) {
            Toast.makeText(this, "Error: Document path must not be null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firestore for the things to do document
        DocumentReference thingsToDoRef = db.collection("destinations")
                .document(destinationId)
                .collection("thingstodo")
                .document(thingsToDoId);

        thingsToDoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d("ThingsToDoDetails", "Image URL: " + imageUrl);
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(imageView);
                } else {
                    Log.e("ThingsToDoDetails", "Image URL is null or empty");
                }

                // Populate views with details
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");
                String address = documentSnapshot.getString("address");
                String type = documentSnapshot.getString("type");
                String price = documentSnapshot.getString("price");
                textViewName.setText(name);
                textViewDescription.setText(description);
                textViewAddress.setText(address);
                textViewType.setText(type);
                textViewPrice.setText(price);
            } else {
                Toast.makeText(this, "Things to do not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveThingsToDoToFavorites() {
        if (userEmail == null || planName == null || destinationId == null || thingsToDoId == null) {
            String errorMessage = "Error: ";
            if (userEmail == null) errorMessage += "User email is null. ";
            if (planName == null) errorMessage += "Plan name is null. ";
            if (destinationId == null) errorMessage += "Destination ID is null. ";
            if (thingsToDoId == null) errorMessage += "Things To Do ID is null. ";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference planDocumentRef = db.collection("user_plans")
                .document(userEmail)
                .collection("plans")
                .document(planName);

        DocumentReference favoriteThingsToDoRef = planDocumentRef.collection("thingstodo").document(thingsToDoId);

        favoriteThingsToDoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(this, "Things to do already added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    db.collection("destinations").document(destinationId)
                            .collection("thingstodo").document(thingsToDoId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    favoriteThingsToDoRef.set(documentSnapshot.getData())
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Error adding to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, "Things to do not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error fetching details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(this, "Error checking favorites: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleMissingExtras() {
        // Error: Required intent extras are missing
        StringBuilder errorMessage = new StringBuilder("Error: Required information is missing. ");
        if (destinationId == null) errorMessage.append("Destination ID is null. ");
        if (thingsToDoId == null) errorMessage.append("Things To Do ID is null. ");
        if (userEmail == null) errorMessage.append("User Email is null. ");
        if (planName == null) errorMessage.append("Plan Name is null. ");
        Log.e("ThingsToDoDetails", errorMessage.toString());
        Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show();
        finish(); // Close the activity
    }
}






