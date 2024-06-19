package com.example.mytravelapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mytravelapp.fragments.UserAccommodationFragment;
import com.example.mytravelapp.fragments.UserRestaurantFragment;
import com.example.mytravelapp.fragments.UserThingsToDoFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserSectionsPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles;
    private final FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;

    public UserSectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> tabTitles, FirebaseFirestore db, String destinationId, String userEmail, String planName) {
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
                return UserAccommodationFragment.newInstance(db, destinationId, userEmail, planName);
            case 1:
                return UserRestaurantFragment.newInstance(db, destinationId, userEmail, planName);
            case 2:
                return UserThingsToDoFragment.newInstance(db, destinationId, userEmail, planName);
            default:
                throw new IllegalArgumentException("Invalid tab position");
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }
}

