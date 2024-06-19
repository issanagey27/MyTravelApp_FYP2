package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.PlansAdapter;
import com.example.mytravelapp.databinding.ActivityPlansBinding;
import com.example.mytravelapp.models.Plans;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlansActivity extends AppCompatActivity {

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

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        plansList = new ArrayList<>();
        plansAdapter = new PlansAdapter(plansList, plan -> {
            Intent intent = new Intent(PlansActivity.this, UserPlansActivity.class);
            // Pass intent extras here
            intent.putExtra("destinationId", plan.getDestination()); // Assuming you have a method to get destinationId from Plans class
            intent.putExtra("planName", plan.getName()); // Assuming you have a method to get planName from Plans class
            intent.putExtra("userEmail", userEmail); // Assuming userEmail is already defined in PlansActivity
            startActivity(intent);
        });
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
}





