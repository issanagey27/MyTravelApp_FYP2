package com.example.mytravelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.R;
import com.example.mytravelapp.adapters.UsersAdapters;
import com.example.mytravelapp.databinding.ActivityUsersBinding;
import com.example.mytravelapp.listeners.UserListener;
import com.example.mytravelapp.models.User;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> allUsers;
    private UsersAdapters usersAdapter;
    private Spinner spinnerLocation, spinnerLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        allUsers = new ArrayList<>();
        usersAdapter = new UsersAdapters(allUsers, this);
        binding.usersRecyclerView.setAdapter(usersAdapter);

        spinnerLocation = binding.spinnerLocation;
        spinnerLanguage = binding.spinnerLanguage;

        setupSpinners();
        setListeners();
        getUsers();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, R.layout.spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, R.layout.spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerLocation.setOnItemSelectedListener(filterListener);
        spinnerLanguage.setOnItemSelectedListener(filterListener);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo("accountType", "Local/TouristGuide")
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        allUsers.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            user.location = queryDocumentSnapshot.getString(Constants.KEY_LOCATION);
                            user.languages = (List<String>) queryDocumentSnapshot.get(Constants.KEY_LANGUAGES);
                            allUsers.add(user);
                        }
                        filterUsers();
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void filterUsers() {
        String selectedLocation = spinnerLocation.getSelectedItem().toString();
        String selectedLanguage = spinnerLanguage.getSelectedItem().toString();

        List<User> filteredUsers = new ArrayList<>();
        for (User user : allUsers) {
            boolean matchesLocation = selectedLocation.equals("All") || (user.location != null && user.location.equals(selectedLocation));
            boolean matchesLanguage = selectedLanguage.equals("All") || (user.languages != null && user.languages.contains(selectedLanguage));
            if (matchesLocation && matchesLanguage) {
                filteredUsers.add(user);
            }
        }

        if (!filteredUsers.isEmpty()) {
            usersAdapter.updateUsers(filteredUsers);
            binding.usersRecyclerView.setVisibility(View.VISIBLE);
            binding.textErrorMessage.setVisibility(View.GONE);
        } else {
            showErrorMessage();
        }
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        binding.usersRecyclerView.setVisibility(View.GONE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}
