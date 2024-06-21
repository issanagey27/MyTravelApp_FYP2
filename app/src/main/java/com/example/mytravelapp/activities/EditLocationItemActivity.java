package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.LocationAdapter;
import com.example.mytravelapp.models.LocationItem;
import com.example.mytravelapp.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditLocationItemActivity extends AppCompatActivity {

    private Spinner spinnerSubcollection;
    private RecyclerView recyclerViewLocations;
    private FirebaseFirestore database;
    private LocationAdapter locationAdapter;
    private List<LocationItem> locationItemList;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location_item);

        database = FirebaseFirestore.getInstance();
        destinationName = getIntent().getStringExtra(Constants.KEY_DESTINATION_NAME);

        spinnerSubcollection = findViewById(R.id.spinnerSubcollection);
        recyclerViewLocations = findViewById(R.id.recyclerViewLocations);
        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(this));
        locationItemList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationItemList, this::onItemClick, this::onEditClick, this::onDeleteClick);
        recyclerViewLocations.setAdapter(locationAdapter);

        ArrayAdapter<CharSequence> adapterSubcollection = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapterSubcollection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubcollection.setAdapter(adapterSubcollection);

        spinnerSubcollection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadLocations(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(EditLocationItemActivity.this, EditLocationActivity.class));
        });
    }

    private void loadLocations(int subcollectionIndex) {
        String[] subcollections = getResources().getStringArray(R.array.category_array);
        String subcollection = subcollections[subcollectionIndex];

        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(subcollection.toLowerCase())
                .orderBy(Constants.KEY_NAME, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    locationItemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString(Constants.KEY_NAME);
                        String imageUrl = document.getString(Constants.KEY_IMAGE_URL);
                        locationItemList.add(new LocationItem(document.getId(), name, imageUrl, subcollection));
                    }
                    locationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditLocationItemActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                });
    }

    private void onItemClick(int position) {
        LocationItem locationItem = locationItemList.get(position);
        // Handle click event if needed, for example, opening details activity
    }

    private void onEditClick(int position) {
        LocationItem locationItem = locationItemList.get(position);
        Intent intent = new Intent(EditLocationItemActivity.this, EditLocationItemDetailsActivity.class);
        intent.putExtra(Constants.KEY_DOCUMENT_ID, locationItem.getDocumentId());
        intent.putExtra(Constants.KEY_DESTINATION_NAME, destinationName);
        intent.putExtra(Constants.KEY_SUBCOLLECTION_NAME, locationItem.getSubcollection());
        startActivity(intent);
    }

    private void onDeleteClick(int position) {
        LocationItem locationItem = locationItemList.get(position);
        String subcollection = locationItem.getSubcollection();
        String documentId = locationItem.getDocumentId();

        database.collection(Constants.KEY_COLLECTION_DESTINATIONS)
                .document(destinationName)
                .collection(subcollection.toLowerCase())
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditLocationItemActivity.this, "Location deleted successfully", Toast.LENGTH_SHORT).show();
                    // Remove item from list and notify adapter
                    locationItemList.remove(position);
                    locationAdapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditLocationItemActivity.this, "Failed to delete location", Toast.LENGTH_SHORT).show();
                });
    }
}
