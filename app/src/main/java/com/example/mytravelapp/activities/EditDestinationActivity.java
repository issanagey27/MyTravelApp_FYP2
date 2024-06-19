package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditDestinationActivity extends AppCompatActivity {

    private LinearLayout destinationListLayout;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destination);

        database = FirebaseFirestore.getInstance();
        destinationListLayout = findViewById(R.id.destinationListLayout);

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(EditDestinationActivity.this, AdminMainActivity.class));
        });

        loadDestinations();
    }

    private void loadDestinations() {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    destinationListLayout.removeAllViews(); // Clear existing views

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        View destinationView = LayoutInflater.from(EditDestinationActivity.this)
                                .inflate(R.layout.item_destination, destinationListLayout, false);

                        TextView textViewDestinationName = destinationView.findViewById(R.id.textViewDestinationName);
                        ImageView imageViewDestination = destinationView.findViewById(R.id.imageViewDestination);
                        com.google.android.material.button.MaterialButton buttonEdit = destinationView.findViewById(R.id.buttonEdit);
                        com.google.android.material.button.MaterialButton buttonDelete = destinationView.findViewById(R.id.buttonDelete);

                        String name = document.getString(Constants.KEY_NAME);
                        String imageUrl = document.getString(Constants.KEY_IMAGE_URL);

                        textViewDestinationName.setText(name);
                        Glide.with(EditDestinationActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .into(imageViewDestination);

                        buttonEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EditDestinationActivity.this, EditDestinationItemActivity.class);
                                intent.putExtra(Constants.KEY_DESTINATION_NAME, name);
                                intent.putExtra(Constants.KEY_IMAGE_URL, imageUrl);
                                startActivity(intent);
                            }
                        });

                        buttonDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDeleteDialog(document.getId(), name);
                            }
                        });

                        destinationListLayout.addView(destinationView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditDestinationActivity.this, "Failed to load destinations", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteDialog(String documentId, String destinationName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Destination")
                .setMessage("Are you sure you want to delete " + destinationName + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDestination(documentId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDestination(String documentId) {
        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditDestinationActivity.this, "Destination deleted successfully", Toast.LENGTH_SHORT).show();
                    loadDestinations(); // Refresh list after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditDestinationActivity.this, "Failed to delete destination", Toast.LENGTH_SHORT).show();
                });
    }
}
