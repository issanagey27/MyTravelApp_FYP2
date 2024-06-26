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

public class AddLocationActivity extends AppCompatActivity {

    private LinearLayout destinationListLayout;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        database = FirebaseFirestore.getInstance();
        destinationListLayout = findViewById(R.id.destinationListLayout);

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(AddLocationActivity.this, AdminMainActivity.class));
        });

        loadDestinations();
    }

    private void loadDestinations() {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    destinationListLayout.removeAllViews(); // Clear existing views

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        View destinationView = LayoutInflater.from(AddLocationActivity.this)
                                .inflate(R.layout.item_destination_no_delete, destinationListLayout, false);

                        TextView textViewDestinationName = destinationView.findViewById(R.id.textViewDestinationName);
                        ImageView imageViewDestination = destinationView.findViewById(R.id.imageViewDestination);
                        com.google.android.material.button.MaterialButton buttonEdit = destinationView.findViewById(R.id.buttonEdit);

                        String name = document.getString(Constants.KEY_NAME);
                        String imageUrl = document.getString(Constants.KEY_IMAGE_URL);

                        textViewDestinationName.setText(name);
                        Glide.with(AddLocationActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .into(imageViewDestination);

                        buttonEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(AddLocationActivity.this, AddLocationItemActivity.class);
                                intent.putExtra(Constants.KEY_DESTINATION_NAME, name);
                                startActivity(intent);
                            }
                        });

                        destinationListLayout.addView(destinationView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddLocationActivity.this, "Failed to load destinations", Toast.LENGTH_SHORT).show();
                });
    }
}
