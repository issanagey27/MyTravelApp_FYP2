package com.example.mytravelapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytravelapp.databinding.ActivityTouristProfileBinding;
import com.example.mytravelapp.utilities.Constants;
import com.example.mytravelapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class TouristProfileActivity extends AppCompatActivity {

    private ActivityTouristProfileBinding binding;
    private FirebaseFirestore db;
    private String encodedImage;
    private PreferenceManager preferenceManager;
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
        binding = ActivityTouristProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            Intent intent = new Intent(TouristProfileActivity.this, TouristMainActivity.class);
            startActivity(intent);
            finish();
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

                        binding.inputName.setText(name);
                        if (image != null && !image.isEmpty()) {
                            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load user data"));
    }

    private void saveChanges() {
        String name = binding.inputName.getText().toString();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.inputConfirmPassword.getText().toString();

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_NAME, name);
        if (isImageChanged) {
            updates.put(Constants.KEY_IMAGE, encodedImage);
            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage); // Update image in PreferenceManager
        }
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

                    preferenceManager.putString(Constants.KEY_NAME, name);

                    // Clear password fields after successful update
                    binding.inputPassword.setText("");
                    binding.inputConfirmPassword.setText("");

                    // Navigate to TouristMainActivity after saving changes
                    Intent intent = new Intent(TouristProfileActivity.this, TouristMainActivity.class);
                    startActivity(intent);
                    finish(); // Finish current activity
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save changes");
                    loading(false); // Hide loading indicator
                });
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
