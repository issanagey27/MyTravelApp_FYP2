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

public class UserThingsToDoFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserPlansAdapter adapter;
    private List<DocumentSnapshot> userSavedPlans;
    private FirebaseFirestore db;
    private String destinationId;
    private String userEmail;
    private String planName;

    public UserThingsToDoFragment() {
        // Required empty public constructor
    }

    public static UserThingsToDoFragment newInstance(FirebaseFirestore db, String destinationId, String userEmail, String planName) {
        UserThingsToDoFragment fragment = new UserThingsToDoFragment();
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
        fetchThingsToDoFromFirestore();
    }

    private void fetchThingsToDoFromFirestore() {
        if (destinationId == null || userEmail == null || planName == null) {
            Log.e("UserThingsToDo", "Required arguments are null");
            return;
        }

        Log.d("UserThingsToDo", "Fetching ThingsToDo for user: " + userEmail + ", plan: " + planName + ", destination: " + destinationId);

        db.collection("user_plans").document(userEmail)
                .collection("plans").document(planName)
                .collection("thingstodo")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userSavedPlans.clear(); // Clear the list before adding new items
                        for (DocumentSnapshot document : task.getResult()) {
                            userSavedPlans.add(document);
                            Log.d("UserThingsToDo", "Fetched ThingsToDo: " + document.getData());
                        }
                        Log.d("UserThingsToDo", "Number of ThingsToDo fetched: " + userSavedPlans.size());
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("UserThingsToDo", "Error fetching ThingsToDo: ", task.getException());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_things_to_do, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUserThingsToDo);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserPlansAdapter(requireActivity(), userSavedPlans, destinationId, "thingstodo", userEmail, planName);
        recyclerView.setAdapter(adapter);
        Log.d("UserThingsToDo", "RecyclerView adapter set. Number of items in adapter: " + adapter.getItemCount());

        return view;
    }
}
