package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Search extends AppCompatActivity {

    private EditText destinationEditText;
    private Button generateButton;
    private TextView resultTextView;
    private FirebaseFirestore db;
    private String planName;
    private PreferenceManager preferenceManager;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        destinationEditText = findViewById(R.id.destinationEditText);
        generateButton = findViewById(R.id.generateButton);
        resultTextView = findViewById(R.id.resultTextView);
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Get the plan name from the intent extras
        planName = getIntent().getStringExtra("planName");
        userEmail = getIntent().getStringExtra("userEmail");

        Log.d("SearchActivity", "User email received from AddPlanActivity: " + userEmail); // Add log statement

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = destinationEditText.getText().toString().trim();
                if (!destination.isEmpty()) {
                    searchDestination(destination);
                } else {
                    Toast.makeText(Search.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Search.this, PlansActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void searchDestination(final String destination) {
        db.collection("destinations")
                .document(destination)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("image_url");
                            updatePlanWithImage(imageUrl);
                            String destination = documentSnapshot.getId();
                            updatePlanWithDestination(destination);

                            try {
                                // Data found, start the Recommendation activity
                                String destinationId = documentSnapshot.getId();
                                Intent intent = new Intent(Search.this, Recommendation.class);
                                intent.putExtra("userEmail", userEmail);
                                intent.putExtra("planName", planName);
                                intent.putExtra("destinationId", destinationId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e("SearchActivity", "Error starting Recommendation activity: " + e.getMessage());
                            }
                        } else {
                            // Data not found, show a message
                            resultTextView.setText("No details found for " + destination);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Search.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePlanWithImage(String imageUrl) {
        String userEmail = preferenceManager.getString(Constants.KEY_USER_EMAIL);
        if (userEmail == null) {
            Toast.makeText(Search.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .document(planName)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the image URL
                    Toast.makeText(Search.this, "Plan updated with image", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to update the image URL
                    Toast.makeText(Search.this, "Failed to update plan with image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePlanWithDestination(String destination) {
        String userEmail = preferenceManager.getString(Constants.KEY_USER_EMAIL);
        if (userEmail == null) {
            Toast.makeText(Search.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .document(planName)
                .update("destination", destination)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the image URL
                    Toast.makeText(Search.this, "Plan updated with destination", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to update the image URL
                    Toast.makeText(Search.this, "Failed to update plan with destination", Toast.LENGTH_SHORT).show();
                });
    }
}



