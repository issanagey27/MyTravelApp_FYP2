package com.example.mytravelapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.UserPlansAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRestaurantFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserPlansAdapter adapter;
    private List<DocumentSnapshot> userSavedPlans;
    private FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;

    public UserRestaurantFragment() {
        // Required empty public constructor
    }

    public static UserRestaurantFragment newInstance(FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        UserRestaurantFragment fragment = new UserRestaurantFragment();
        Bundle args = new Bundle();
        args.putString("destinationId", destinationId);
        args.putString("userEmail", userEmail);
        args.putString("planName", planName);
        fragment.setArguments(args);
        fragment.db = db;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSavedPlans = new ArrayList<>();
        if (getArguments() != null) {
            destinationId = getArguments().getString("destinationId");
            userEmail = getArguments().getString("userEmail");
            planName = getArguments().getString("planName");
        }
        fetchRestaurantsFromFirestore();
    }

    private void fetchRestaurantsFromFirestore() {
        if (destinationId == null || userEmail == null || planName == null) {
            Log.e("UserRestaurant", "Required arguments are null");
            return;
        }

        Log.d("UserRestaurant", "Fetching restaurants for user: " + userEmail + ", plan: " + planName + ", destination: " + destinationId);

        db.collection("user_plans").document(userEmail)
                .collection("plans").document(planName)
                .collection("restaurants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userSavedPlans.clear(); // Clear the list before adding new items
                        for (DocumentSnapshot document : task.getResult()) {
                            userSavedPlans.add(document);
                            Log.d("UserRestaurant", "Fetched restaurant: " + document.getData());
                        }
                        Log.d("UserRestaurant", "Number of restaurants fetched: " + userSavedPlans.size());
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("UserRestaurant", "Error fetching restaurants: ", task.getException());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_restaurant, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUserRestaurant);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserPlansAdapter(requireActivity(), userSavedPlans, destinationId, "restaurants", userEmail, planName);
        recyclerView.setAdapter(adapter);
        Log.d("UserRestaurant", "RecyclerView adapter set. Number of items in adapter: " + adapter.getItemCount());

        return view;
    }
}

