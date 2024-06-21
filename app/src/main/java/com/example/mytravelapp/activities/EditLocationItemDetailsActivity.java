package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EditLocationItemDetailsActivity extends AppCompatActivity {

    private EditText editTextName, editTextDescription, editTextAddress, editTextImageUrl, editTextAmenities, editTextReview, editTextPrice, editTextCuisine;
    private Spinner spinnerType;
    private String documentId, destinationName, subcollection;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location_item_details);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextAmenities = findViewById(R.id.editTextAmenities);
        editTextReview = findViewById(R.id.editTextReview);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextCuisine = findViewById(R.id.editTextCuisine);
        spinnerType = findViewById(R.id.spinnerType);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> finish());

        findViewById(R.id.buttonUpdate).setOnClickListener(this::updateLocationItem);

        database = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        documentId = intent.getStringExtra(Constants.KEY_DOCUMENT_ID);
        destinationName = intent.getStringExtra(Constants.KEY_DESTINATION_NAME);
        subcollection = intent.getStringExtra(Constants.KEY_SUBCOLLECTION_NAME);

        setupSpinner();
        loadItemDetails();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter;
        switch (subcollection.toLowerCase()) {
            case "accommodations":
                adapter = ArrayAdapter.createFromResource(this, R.array.accommodation_type_array, android.R.layout.simple_spinner_item);
                break;
            case "restaurants":
                adapter = ArrayAdapter.createFromResource(this, R.array.cuisine_array, android.R.layout.simple_spinner_item);
                break;
            case "thingstodo":
                adapter = ArrayAdapter.createFromResource(this, R.array.things_to_do_type_array, android.R.layout.simple_spinner_item);
                break;
            default:
                adapter = ArrayAdapter.createFromResource(this, R.array.accommodation_type_array, android.R.layout.simple_spinner_item);
                break;
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void loadItemDetails() {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(subcollection.toLowerCase())
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editTextName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                        editTextDescription.setText(documentSnapshot.getString(Constants.KEY_DESCRIPTION));
                        editTextAddress.setText(documentSnapshot.getString(Constants.KEY_ADDRESS));
                        editTextImageUrl.setText(documentSnapshot.getString(Constants.KEY_IMAGE_URL));
                        editTextAmenities.setText(documentSnapshot.getString(Constants.KEY_AMENITIES));
                        editTextReview.setText(documentSnapshot.getString(Constants.KEY_REVIEW));
                        editTextPrice.setText(documentSnapshot.getString(Constants.KEY_PRICE));
                        editTextCuisine.setText(documentSnapshot.getString(Constants.KEY_CUISINE));
                        String type = documentSnapshot.getString(Constants.KEY_TYPE);
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerType.getAdapter();
                        if (type != null) {
                            int spinnerPosition = adapter.getPosition(type);
                            spinnerType.setSelection(spinnerPosition);
                        }
                    } else {
                        Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load item details", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLocationItem(View view) {
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String amenities = editTextAmenities.getText().toString().trim();
        String review = editTextReview.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();
        String cuisine = editTextCuisine.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            return;
        }

        if (!isValidImageUrl(imageUrl)) {
            editTextImageUrl.setError("Invalid image URL");
            return;
        }

        checkForDuplicateName(name, isDuplicate -> {
            if (isDuplicate) {
                editTextName.setError("Name already exists");
            } else {
                Map<String, Object> updates = new HashMap<>();
                updates.put(Constants.KEY_NAME, name);
                updates.put(Constants.KEY_DESCRIPTION, description);
                updates.put(Constants.KEY_ADDRESS, address);
                updates.put(Constants.KEY_IMAGE_URL, imageUrl);
                updates.put(Constants.KEY_AMENITIES, amenities);
                updates.put(Constants.KEY_REVIEW, review);
                updates.put(Constants.KEY_PRICE, price);
                updates.put(Constants.KEY_CUISINE, cuisine);
                updates.put(Constants.KEY_TYPE, type);

                database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                        .document(destinationName)
                        .collection(subcollection.toLowerCase())
                        .document(documentId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private boolean isValidImageUrl(@NonNull String url) {
        String regex = "^(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|gif|png)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }

    private void checkForDuplicateName(String name, OnDuplicateCheckListener listener) {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(subcollection.toLowerCase())
                .whereEqualTo(Constants.KEY_NAME, name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = !queryDocumentSnapshots.isEmpty();
                    listener.onCheckCompleted(isDuplicate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking for duplicate name", Toast.LENGTH_SHORT).show();
                });
    }

    private interface OnDuplicateCheckListener {
        void onCheckCompleted(boolean isDuplicate);
    }
}
