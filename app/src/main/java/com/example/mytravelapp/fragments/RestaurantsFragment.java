package com.example.mytravelapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.RecommendationAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RestaurantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;
    private List<DocumentSnapshot> recommendations;
    private FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;
    private Spinner filterSpinner;

    public RestaurantsFragment() {
        // Required empty public constructor
    }

    public static RestaurantsFragment newInstance(FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        RestaurantsFragment fragment = new RestaurantsFragment();
        Bundle args = new Bundle();
        args.putString("destinationId", destinationId);
        args.putString("userEmail", userEmail);
        args.putString("planName", planName);
        fragment.setArguments(args);
        fragment.db = db;
        fragment.destinationId = destinationId;
        fragment.userEmail = userEmail;
        fragment.planName = planName;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recommendations = new ArrayList<>();
        if (getArguments() != null) {
            destinationId = getArguments().getString("destinationId");
            userEmail = getArguments().getString("userEmail");
            planName = getArguments().getString("planName");
        }
        fetchRestaurantsFromFirestore();
    }

    private void fetchRestaurantsFromFirestore() {
        if (destinationId == null) {
            // Handle the error, log it, or show a message to the user
            return;
        }

        db.collection("destinations").document(destinationId).collection("restaurants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            recommendations.add(document);
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle errors
                        Log.e("RestaurantsFragment", "Error fetching restaurants", task.getException());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurants, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewRestaurants);
        filterSpinner = view.findViewById(R.id.filterSpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RecommendationAdapter(requireActivity(), recommendations, destinationId, "restaurant", userEmail, planName);
        recyclerView.setAdapter(adapter);

        // Set up filter spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.filter_cuisine, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Set default selection to "All"
        filterSpinner.setSelection(spinnerAdapter.getPosition("All"));

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilterType = (String) parent.getItemAtPosition(position);
                loadRecommendations(selectedFilterType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return view;
    }

    private void loadRecommendations(String filterType) {
        if ("All".equals(filterType)) {
            fetchRestaurantsFromFirestore();
        } else {
            db.collection("destinations").document(destinationId)
                    .collection("restaurants")
                    .whereEqualTo("cuisine", filterType)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> newRecommendations = queryDocumentSnapshots.getDocuments();
                        adapter.updateRecommendations(newRecommendations);
                    })
                    .addOnFailureListener(e -> Log.e("RestaurantsFragment", "Error getting recommendations", e));
        }
    }
}


