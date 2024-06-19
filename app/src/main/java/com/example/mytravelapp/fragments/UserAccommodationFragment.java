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

public class UserAccommodationFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserPlansAdapter adapter;
    private List<DocumentSnapshot> userSavedPlans;
    private FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;

    public UserAccommodationFragment() {
        // Required empty public constructor
    }

    public static UserAccommodationFragment newInstance(FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        UserAccommodationFragment fragment = new UserAccommodationFragment();
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
        fetchAccommodationsFromFirestore();
    }

    private void fetchAccommodationsFromFirestore() {
        if (destinationId == null || userEmail == null || planName == null) {
            Log.e("UserAccommodation", "Required arguments are null");
            return;
        }

        Log.d("UserAccommodation", "Fetching accommodations for user: " + userEmail + ", plan: " + planName + ", destination: " + destinationId);

        db.collection("user_plans").document(userEmail)
                .collection("plans").document(planName)
                .collection("accommodations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userSavedPlans.clear(); // Clear the list before adding new items
                        for (DocumentSnapshot document : task.getResult()) {
                            userSavedPlans.add(document);
                            Log.d("UserAccommodation", "Fetched accommodation: " + document.getData());
                        }
                        Log.d("UserAccommodation", "Number of accommodations fetched: " + userSavedPlans.size());
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("UserAccommodation", "Error fetching accommodations: ", task.getException());
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_accommodation, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUserAccommodation);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserPlansAdapter(requireActivity(), userSavedPlans, destinationId, "accommodations", userEmail, planName);
        recyclerView.setAdapter(adapter);
        Log.d("UserAccommodation", "RecyclerView adapter set. Number of items in adapter: " + adapter.getItemCount());

        return view;
    }
}



