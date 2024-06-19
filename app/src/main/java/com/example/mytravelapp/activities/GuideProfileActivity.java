package com.example.mytravelapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.databinding.ActivityGuideProfileBinding;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuideProfileActivity extends AppCompatActivity {

    private ActivityGuideProfileBinding binding;
    private FirebaseFirestore db;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private Spinner spinnerLanguages;
    private ArrayAdapter<String> spinnerAdapter;
    private Set<String> selectedLanguages = new HashSet<>(); // Store selected languages
    private boolean isImageChanged = false; // Track if the image is changed

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spinnerLanguages = binding.spinnerLanguages;
        setupLanguageDropdown();

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();

        setListeners();
        loadUserData();
    }

    private void setListeners() {
        binding.buttonSave.setOnClickListener(v -> saveChanges());
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GuideProfileActivity.this, GuideMainActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnAddLanguage.setOnClickListener(v -> {
            String selectedLanguage = spinnerLanguages.getSelectedItem().toString();
            if (!selectedLanguages.contains(selectedLanguage)) {
                addLanguageChip(selectedLanguage);
                selectedLanguages.add(selectedLanguage);
            } else {
                showToast("Language already added");
            }
        });
    }

    private void loadUserData() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        String image = documentSnapshot.getString(Constants.KEY_IMAGE);
                        String location = documentSnapshot.getString(Constants.KEY_LOCATION);
                        List<String> languages = (List<String>) documentSnapshot.get(Constants.KEY_LANGUAGES);

                        binding.inputName.setText(name);
                        if (image != null && !image.isEmpty()) {
                            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                        }
                        if (location != null) {
                            binding.inputLocation.setText(location);
                        }
                        if (languages != null) {
                            for (String language : languages) {
                                addLanguageChip(language);
                                selectedLanguages.add(language);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load user data"));
    }

    private void saveChanges() {
        String name = binding.inputName.getText().toString();
        String location = binding.inputLocation.getText().toString();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.inputConfirmPassword.getText().toString();

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        List<String> languages = new ArrayList<>(selectedLanguages);

        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_NAME, name);
        if (isImageChanged) {
            updates.put(Constants.KEY_IMAGE, encodedImage);
        }
        updates.put(Constants.KEY_LOCATION, location);
        updates.put(Constants.KEY_LANGUAGES, languages);

        // Check if password fields are not empty to update
        if (!password.isEmpty()) {
            updates.put(Constants.KEY_PASSWORD, password);
        }

        loading(true); // Show loading indicator

        DocumentReference userRef = db.collection(Constants.KEY_COLLECTION_USERS).document(userId);
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    showToast("Changes saved");
                    loading(false); // Hide loading indicator

                    // Clear password fields after successful update
                    binding.inputPassword.setText("");
                    binding.inputConfirmPassword.setText("");

                    // Navigate to GuideMainActivity after saving changes
                    Intent intent = new Intent(GuideProfileActivity.this, GuideMainActivity.class);
                    startActivity(intent);
                    finish(); // Finish current activity
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save changes");
                    loading(false); // Hide loading indicator
                });
    }

    private void setupLanguageDropdown() {
        List<String> languages = Arrays.asList("English", "Chinese", "Malay", "Spanish", "German", "French");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguages.setAdapter(spinnerAdapter);
    }

    private void addLanguageChip(String language) {
        ChipGroup chipGroup = binding.chipGroupLanguages;
        Chip chip = new Chip(this);
        chip.setText(language);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectedLanguages.remove(language); // Remove language from selectedLanguages set
        });
        chipGroup.addView(chip);
        spinnerLanguages.setSelection(0); // Reset spinner selection
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                            isImageChanged = true; // Image is changed
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSave.setVisibility(View.VISIBLE);
        }
    }
}





