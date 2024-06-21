package com.example.mytravelapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddLocationItemActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private Spinner spinnerAccommodationType;
    private Spinner spinnerCuisine;
    private Spinner spinnerThingsToDoType;
    private TextView textViewAccommodationType;
    private TextView textViewCuisine;
    private TextView textViewThingsToDoType;
    private FirebaseFirestore database;
    private String destinationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_item);

        database = FirebaseFirestore.getInstance();
        destinationName = getIntent().getStringExtra(Constants.KEY_DESTINATION_NAME);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerAccommodationType = findViewById(R.id.spinnerAccommodationType);
        spinnerCuisine = findViewById(R.id.spinnerCuisine);
        spinnerThingsToDoType = findViewById(R.id.spinnerThingsToDoType);
        textViewAccommodationType = findViewById(R.id.textViewAccommodationType);
        textViewCuisine = findViewById(R.id.textViewCuisine);
        textViewThingsToDoType = findViewById(R.id.textViewThingsToDoType);

        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);

        ArrayAdapter<CharSequence> adapterAccommodationType = ArrayAdapter.createFromResource(this, R.array.accommodation_type_array, android.R.layout.simple_spinner_item);
        adapterAccommodationType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccommodationType.setAdapter(adapterAccommodationType);

        ArrayAdapter<CharSequence> adapterCuisine = ArrayAdapter.createFromResource(this, R.array.cuisine_array, android.R.layout.simple_spinner_item);
        adapterCuisine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuisine.setAdapter(adapterCuisine);

        ArrayAdapter<CharSequence> adapterThingsToDoType = ArrayAdapter.createFromResource(this, R.array.things_to_do_type_array, android.R.layout.simple_spinner_item);
        adapterThingsToDoType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThingsToDoType.setAdapter(adapterThingsToDoType);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFieldVisibility(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        findViewById(R.id.buttonSave).setOnClickListener(this::saveLocationDetails);

        ImageView buttonBack = findViewById(R.id.imageBack);
        buttonBack.setOnClickListener(v -> finish());
    }

    private void updateFieldVisibility(int position) {
        // Hide all specific fields first
        findViewById(R.id.editTextAmenities).setVisibility(View.GONE);
        findViewById(R.id.editTextReview).setVisibility(View.GONE);
        findViewById(R.id.spinnerAccommodationType).setVisibility(View.GONE);
        findViewById(R.id.textViewAccommodationType).setVisibility(View.GONE);
        findViewById(R.id.spinnerCuisine).setVisibility(View.GONE);
        findViewById(R.id.textViewCuisine).setVisibility(View.GONE);
        findViewById(R.id.editTextRestaurantReview).setVisibility(View.GONE);
        findViewById(R.id.spinnerThingsToDoType).setVisibility(View.GONE);
        findViewById(R.id.textViewThingsToDoType).setVisibility(View.GONE);

        // Show specific fields based on the selected category
        switch (position) {
            case 0: // Accommodation
                findViewById(R.id.editTextAmenities).setVisibility(View.VISIBLE);
                findViewById(R.id.editTextReview).setVisibility(View.VISIBLE);
                findViewById(R.id.spinnerAccommodationType).setVisibility(View.VISIBLE);
                findViewById(R.id.textViewAccommodationType).setVisibility(View.VISIBLE);
                break;
            case 1: // Restaurant
                findViewById(R.id.spinnerCuisine).setVisibility(View.VISIBLE);
                findViewById(R.id.textViewCuisine).setVisibility(View.VISIBLE);
                findViewById(R.id.editTextRestaurantReview).setVisibility(View.VISIBLE);
                break;
            case 2: // ThingsToDo
                findViewById(R.id.spinnerThingsToDoType).setVisibility(View.VISIBLE);
                findViewById(R.id.textViewThingsToDoType).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveLocationDetails(View view) {
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        String name = ((TextInputEditText) findViewById(R.id.editTextName)).getText().toString().trim();
        String description = ((TextInputEditText) findViewById(R.id.editTextDescription)).getText().toString().trim();
        String address = ((TextInputEditText) findViewById(R.id.editTextAddress)).getText().toString().trim();
        String imageUrl = ((TextInputEditText) findViewById(R.id.editTextImageUrl)).getText().toString().trim();
        String price = ((TextInputEditText) findViewById(R.id.editTextPrice)).getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || address.isEmpty() || imageUrl.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidImageUrl(imageUrl)) {
            Toast.makeText(this, "Please enter a valid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(selectedCategory.toLowerCase())
                .whereEqualTo(Constants.KEY_NAME, name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "This name already exists in " + selectedCategory, Toast.LENGTH_SHORT).show();
                    } else {
                        saveNewLocation(selectedCategory, name, description, address, imageUrl, price);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking for duplicates", Toast.LENGTH_SHORT).show());
    }

    private void saveNewLocation(String category, String name, String description, String address, String imageUrl, String price) {
        Map<String, Object> locationDetails = new HashMap<>();
        locationDetails.put(Constants.KEY_NAME, name);
        locationDetails.put(Constants.KEY_DESCRIPTION, description);
        locationDetails.put(Constants.KEY_ADDRESS, address);
        locationDetails.put(Constants.KEY_IMAGE_URL, imageUrl);
        locationDetails.put(Constants.KEY_PRICE, price);

        switch (category) {
            case "Accommodations":
                String amenities = ((TextInputEditText) findViewById(R.id.editTextAmenities)).getText().toString().trim();
                String review = ((TextInputEditText) findViewById(R.id.editTextReview)).getText().toString().trim();
                String type = spinnerAccommodationType.getSelectedItem().toString();

                if (amenities.isEmpty() || review.isEmpty() || type.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                locationDetails.put(Constants.KEY_AMENITIES, amenities);
                locationDetails.put(Constants.KEY_REVIEW, review);
                locationDetails.put(Constants.KEY_TYPE, type);
                break;

            case "Restaurants":
                String cuisine = spinnerCuisine.getSelectedItem().toString();
                String restaurantReview = ((TextInputEditText) findViewById(R.id.editTextRestaurantReview)).getText().toString().trim();

                if (cuisine.isEmpty() || restaurantReview.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                locationDetails.put(Constants.KEY_CUISINE, cuisine);
                locationDetails.put(Constants.KEY_REVIEW, restaurantReview);
                break;

            case "ThingsToDo":
                String thingsToDoType = spinnerThingsToDoType.getSelectedItem().toString();

                locationDetails.put(Constants.KEY_TYPE, thingsToDoType);
                break;
        }

        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(category.toLowerCase())
                .add(locationDetails)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Location added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add location", Toast.LENGTH_SHORT).show());
    }

    private boolean isValidImageUrl(String imageUrl) {
        // Regex pattern to match a valid URL for image
        String pattern = "(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|gif|png)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(imageUrl);
        return m.matches();
    }
}
