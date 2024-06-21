package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytravelapp.adapters.PlansAdapter;
import com.example.mytravelapp.databinding.ActivityPlansBinding;
import com.example.mytravelapp.models.Plans;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlansActivity extends AppCompatActivity implements PlansAdapter.OnItemClickListener, PlansAdapter.OnDeleteClickListener {

    private ActivityPlansBinding binding;
    private FirebaseFirestore database;
    private List<Plans> plansList;
    private PlansAdapter plansAdapter;
    private PreferenceManager preferenceManager;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        // Get userEmail from PreferenceManager
        userEmail = preferenceManager.getString(Constants.KEY_USER_EMAIL);

        init();
        loadPlans();
        setListeners();

        // Back button
        binding.backButton.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(PlansActivity.this, TouristMainActivity.class));
        });
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        plansList = new ArrayList<>();
        plansAdapter = new PlansAdapter(plansList, this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(plansAdapter);
    }

    private void loadPlans() {
        String userEmail = preferenceManager.getString(Constants.KEY_USER_EMAIL);
        database.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        plansList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Plans plan = document.toObject(Plans.class);
                            plansList.add(plan);
                        }
                        plansAdapter.notifyDataSetChanged();
                    } else {
                        showToast("Failed to load plans");
                    }
                });
    }

    private void setListeners() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddPlanActivity.class);
            startActivity(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Plans plan) {
        Intent intent = new Intent(PlansActivity.this, UserPlansActivity.class);
        intent.putExtra("destinationId", plan.getDestination());
        intent.putExtra("planName", plan.getName());
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Plans plan) {
        String planName = plan.getName(); // Get the name of the plan

        // Delete the plan document and its subcollections
        database.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .whereEqualTo("name", planName) // Assuming "name" is the field in the document that stores the plan name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String planId = document.getId(); // Get the document ID (plan ID)
                            deletePlanAndSubcollections(planId);
                        }
                    } else {
                        showToast("Failed to find plan document: " + task.getException().getMessage());
                    }
                });
    }

    private void deletePlanAndSubcollections(String planId) {
        // Delete the plan document
        database.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .document(planId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Document successfully deleted, now delete subcollections
                    deleteSubcollections(planId);
                    // Show toast indicating deletion success
                    showToast("Item deleted successfully.");
                    // Finish current activity and reopen it
                    finish();
                    startActivity(getIntent());
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to delete plan: " + e.getMessage());
                });
    }

    private void deleteSubcollections(String planId) {
        // Delete all documents in the subcollection
        database.collection(Constants.KEY_COLLECTION_USER_PLANS)
                .document(userEmail)
                .collection("plans")
                .document(planId)
                .collection("subcollectionName") // Replace with your subcollection name if any
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    } else {
                        showToast("Failed to delete subcollections: " + task.getException().getMessage());
                    }
                });
    }
}
