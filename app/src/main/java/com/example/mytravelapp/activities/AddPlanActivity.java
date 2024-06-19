package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.databinding.ActivityAddPlanBinding;
import com.example.mytravelapp.models.Plans;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPlanActivity extends AppCompatActivity {

    private ActivityAddPlanBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setListeners();
    }

    private void setListeners() {
        binding.buttonAddPlan.setOnClickListener(v -> addPlan());
    }

    private void addPlan() {
        String planName = binding.inputPlanName.getText().toString().trim();
        if (planName.isEmpty()) {
            showToast("Please enter a plan name");
            return;
        }

        String userEmail = preferenceManager.getString(Constants.KEY_USER_EMAIL);
        if (userEmail == null) {
            showToast("User not logged in");
            return;
        }

        Log.d("AddPlanActivity", "User email: " + userEmail); // Add log statement

        DocumentReference userPlanDocRef = database.collection(Constants.KEY_COLLECTION_USER_PLANS).document(userEmail);

        userPlanDocRef.collection("plans").document(planName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        showToast("Plan with this name already exists. Please try again.");
                    } else {
                        Plans newPlan = new Plans();
                        newPlan.setName(planName);
                        newPlan.setDestination(null);
                        newPlan.setImageUrl(null); // Initialize the image URL as null

                        userPlanDocRef.collection("plans").document(planName)
                                .set(newPlan)
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Plan added successfully");

                                    // Navigate to the search activity
                                    Intent intent = new Intent(getApplicationContext(), Search.class);
                                    intent.putExtra("planName", planName);
                                    intent.putExtra("userEmail", userEmail); // Pass userEmail here
                                    Log.d("AddPlanActivity", "Starting SearchActivity with user email: " + userEmail); // Add log statement
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> showToast("Failed to add plan"));
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to check existing plans"));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}


