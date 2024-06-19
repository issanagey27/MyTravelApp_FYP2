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

public class RestaurantDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private String destinationId;
    private String restaurantId;
    private String userEmail;
    private String planName;

    private ImageView imageView;
    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewAddress;
    private TextView textViewCuisine;
    private TextView textViewPrice;
    private TextView textViewReview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details); // Make sure this matches your layout file name

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        imageView = findViewById(R.id.imageView);
        textViewName = findViewById(R.id.textViewName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewCuisine = findViewById(R.id.textViewCuisine);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewReview = findViewById(R.id.textViewReview);
        ImageButton saveToFavoritesButton = findViewById(R.id.saveToFavoritesButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destinationId = extras.getString("destinationId");
            restaurantId = extras.getString("restaurantId");
            userEmail = extras.getString("userEmail");
            planName = extras.getString("planName");

            Log.d("RestaurantDetails", "Destination ID: " + destinationId);
            Log.d("RestaurantDetails", "Restaurant ID: " + restaurantId);

            // Fetch restaurant details from Firestore
            fetchRestaurantDetails();

            saveToFavoritesButton.setOnClickListener(v -> saveRestaurantToFavorites());
        } else {
            // Error: No destinationId or restaurantId provided
            Toast.makeText(this, "Error: No destinationId or restaurantId provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }
    }

    private void fetchRestaurantDetails() {
        // Query Firestore for the restaurant document
        DocumentReference restaurantRef = db.collection("destinations")
                .document(destinationId)
                .collection("restaurants")
                .document(restaurantId);

        restaurantRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d("RestaurantDetails", "Image URL: " + imageUrl);
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(imageView);
                } else {
                    Log.e("RestaurantDetails", "Image URL is null or empty");
                }

                // Populate views with restaurant details
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");
                String address = documentSnapshot.getString("address");
                String cuisine = documentSnapshot.getString("cuisine");
                String price = documentSnapshot.getString("price");
                String review = documentSnapshot.getString("review");
                textViewName.setText(name);
                textViewDescription.setText(description);
                textViewAddress.setText(address);
                textViewCuisine.setText(cuisine);
                textViewPrice.setText(price);
                textViewReview.setText(review);
            } else {
                Toast.makeText(this, "Restaurant not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching restaurant details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveRestaurantToFavorites() {
        if (userEmail == null || planName == null || destinationId == null || restaurantId == null) {
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
            if (restaurantId == null) {
                errorMessage += "Restaurant ID is null. ";
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference planDocumentRef = db.collection("user_plans")
                .document(userEmail)
                .collection("plans")
                .document(planName);

        DocumentReference favoriteRestaurantRef = planDocumentRef.collection("restaurants").document(restaurantId);

        favoriteRestaurantRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(this, "Restaurant already added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    db.collection("destinations").document(destinationId)
                            .collection("restaurants").document(restaurantId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    favoriteRestaurantRef.set(documentSnapshot.getData())
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Restaurant added to favorites", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Error adding restaurant to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, "Restaurant not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error fetching restaurant details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(this, "Error checking restaurant: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


