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

public class AccommodationFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;
    private List<DocumentSnapshot> recommendations;
    private FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;
    private Spinner filterSpinner;

    public AccommodationFragment() {
        // Required empty public constructor
    }

    public static AccommodationFragment newInstance(FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        AccommodationFragment fragment = new AccommodationFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accommodation, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAccommodation);
        filterSpinner = view.findViewById(R.id.filterSpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RecommendationAdapter(requireActivity(), recommendations, destinationId, "accommodation", userEmail, planName);
        recyclerView.setAdapter(adapter);

        // Set up filter spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.filter_acmTypes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Set default selection to "All"
        filterSpinner.setSelection(spinnerAdapter.getPosition("All"));

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilterType = (String) parent.getItemAtPosition(position);
                adapter.setSelectedFilterType(selectedFilterType);
                if (selectedFilterType.equals("All")) {
                    fetchAllAccommodationsFromFirestore();
                } else {
                    fetchFilteredAccommodationsFromFirestore(selectedFilterType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial data load with "All" filter
        fetchAllAccommodationsFromFirestore();

        return view;
    }

    private void fetchAllAccommodationsFromFirestore() {
        db.collection("destinations").document(destinationId)
                .collection("accommodations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recommendations.clear();
                        recommendations.addAll(task.getResult().getDocuments());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("AccommodationFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchFilteredAccommodationsFromFirestore(String filterType) {
        db.collection("destinations").document(destinationId)
                .collection("accommodations")
                .whereEqualTo("type", filterType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recommendations.clear();
                        recommendations.addAll(task.getResult().getDocuments());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("AccommodationFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
}








