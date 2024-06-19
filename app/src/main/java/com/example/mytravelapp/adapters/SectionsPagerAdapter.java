package com.example.mytravelapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mytravelapp.fragments.AccommodationFragment;
import com.example.mytravelapp.fragments.RestaurantsFragment;
import com.example.mytravelapp.fragments.ThingsToDoFragment;
import com.example.mytravelapp.fragments.UserAccommodationFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SectionsPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles;
    private final FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;

    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> tabTitles, FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        super(fragmentActivity);
        this.tabTitles = tabTitles;
        this.db = db;
        this.destinationId = destinationId;
        this.userEmail = userEmail;
        this.planName = planName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return AccommodationFragment.newInstance(db, destinationId, userEmail, planName); // Use newInstance method
            case 1:
                return RestaurantsFragment.newInstance(db, destinationId, userEmail, planName); // Similarly update RestaurantsFragment
            case 2:
                return ThingsToDoFragment.newInstance(db, destinationId, userEmail, planName); // Similarly update ThingsToDoFragment
            case 3:
                return UserAccommodationFragment.newInstance(db, destinationId, userEmail, planName);
            default:
                throw new IllegalArgumentException("Invalid tab position");
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }
}






