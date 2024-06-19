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

public class AccommodationDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private String destinationId;
    private String accommodationId;
    private String userEmail;
    private String planName;

    private ImageView imageView;
    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewAmenities;
    private TextView textViewType;
    private TextView textViewAddress;
    private TextView textViewPrice;
    private TextView textViewReview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accommodation_details);

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        imageView = findViewById(R.id.imageView);
        textViewName = findViewById(R.id.textViewName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewAmenities = findViewById(R.id.textViewAmenities);
        textViewType = findViewById(R.id.textViewType);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewReview = findViewById(R.id.textViewReview);
        ImageButton saveToFavoritesButton = findViewById(R.id.saveToFavoritesButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destinationId = extras.getString("destinationId");
            accommodationId = extras.getString("accommodationId");
            userEmail = extras.getString("userEmail");
            planName = extras.getString("planName");

            Log.d("AccommodationDetails", "Destination ID: " + destinationId);
            Log.d("AccommodationDetails", "Accommodation ID: " + accommodationId);

            // Fetch accommodation details from Firestore
            fetchAccommodationDetails();

            saveToFavoritesButton.setOnClickListener(v -> saveAccommodationToFavorites());
        } else {
            // Error: No destinationId or accommodationId provided
            Toast.makeText(this, "Error: No destinationId or accommodationId provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }
    }

    private void fetchAccommodationDetails() {
        // Query Firestore for the accommodation document
        DocumentReference accommodationRef = db.collection("destinations")
                .document(destinationId)
                .collection("accommodations")
                .document(accommodationId);

        accommodationRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d("AccommodationDetails", "Image URL: " + imageUrl);
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(imageView);
                } else {
                    // Log an error message or handle the absence of image URL
                    Log.e("AccommodationDetails", "Image URL is null or empty");
                }

                // Populate views with accommodation details
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");
                String amenities = documentSnapshot.getString("amenities");
                String type = documentSnapshot.getString("type");
                String address = documentSnapshot.getString("address");
                String price = documentSnapshot.getString("price");
                String review = documentSnapshot.getString("review");
                textViewName.setText(name);
                textViewDescription.setText(description);
                textViewAmenities.setText(amenities);
                textViewType.setText(type);
                textViewAddress.setText(address);
                textViewPrice.setText(price);
                textViewReview.setText(review);
            } else {
                Toast.makeText(this, "Accommodation not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching accommodation details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveAccommodationToFavorites() {
        if (userEmail == null || planName == null || destinationId == null || accommodationId == null) {
            String errorMessage = "Error: ";
            if (userEmail == null) {
                errorMessage += "User email is null. ";
            }
            if (planName == null) {
                errorMessage += "Plan name is null. ";
            }
            if (destinationId == null) {
                errorMessage += "Destination ID is null. ";
            }
            if (accommodationId == null) {
                errorMessage += "Accommodation ID is null. ";
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the document path in the userplans collection
        DocumentReference planDocumentRef = db.collection("user_plans")
                .document(userEmail)
                .collection("plans")
                .document(planName);

        // Check if the specific accommodation document already exists
        DocumentReference favoriteAccommodationRef = planDocumentRef.collection("accommodations").document(accommodationId);

        favoriteAccommodationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Accommodation already exists in favorites
                    Toast.makeText(this, "Accommodation already added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    // Accommodation does not exist, proceed to add it
                    db.collection("destinations").document(destinationId)
                            .collection("accommodations").document(accommodationId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Copy accommodation details to the user's plan
                                    favoriteAccommodationRef.set(documentSnapshot.getData())
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Accommodation added to favorites", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Error adding accommodation to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, "Accommodation not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error fetching accommodation details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(this, "Error checking accommodation: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



