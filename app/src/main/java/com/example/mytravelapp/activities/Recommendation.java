package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class Recommendation extends AppCompatActivity {

    private FirebaseFirestore db;
    private String destinationId;
    private String planName;
    private String userEmail;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private final List<String> tabTitles = Arrays.asList("Accommodation", "Restaurants", "Things To Do");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        ImageButton backButton = findViewById(R.id.backButton);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the destinationId, userEmail, and planName from the intent extras
        destinationId = getIntent().getStringExtra("destinationId");
        userEmail = getIntent().getStringExtra("userEmail");
        planName = getIntent().getStringExtra("planName");

        if (destinationId == null || userEmail == null || planName == null) {
            Log.e("Recommendation", "Destination ID, userEmail, or planName is null");
            finish(); // Close the activity if any of these values is null
            return;
        }

        // Set up ViewPager2 and adapter
        viewPager.setAdapter(new SectionsPagerAdapter(this, tabTitles, db, destinationId, userEmail, planName));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

        // Set up back button to navigate to PlansActivity when clicked
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Recommendation.this, PlansActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
