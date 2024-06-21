package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout locationListLayout;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        database = FirebaseFirestore.getInstance();
        locationListLayout = findViewById(R.id.destinationListLayout);

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            // Replace with appropriate destination after editing (e.g., AdminMainActivity)
        });

        loadLocations();
    }

    private void loadLocations() {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    locationListLayout.removeAllViews(); // Clear existing views

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        View locationView = LayoutInflater.from(EditLocationActivity.this)
                                .inflate(R.layout.item_destination, locationListLayout, false);

                        TextView textViewLocationName = locationView.findViewById(R.id.textViewDestinationName);
                        ImageView imageViewLocation = locationView.findViewById(R.id.imageViewDestination);
                        com.google.android.material.button.MaterialButton buttonEdit = locationView.findViewById(R.id.buttonEdit);

                        String name = document.getString(Constants.KEY_NAME);
                        String imageUrl = document.getString(Constants.KEY_IMAGE_URL);

                        textViewLocationName.setText(name);
                        Glide.with(EditLocationActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .into(imageViewLocation);

                        buttonEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EditLocationActivity.this, EditLocationItemActivity.class);
                                intent.putExtra(Constants.KEY_DESTINATION_NAME, name);
                                intent.putExtra(Constants.KEY_IMAGE_URL, imageUrl);
                                startActivity(intent);
                            }
                        });

                        locationListLayout.addView(locationView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditLocationActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                });
    }
}
