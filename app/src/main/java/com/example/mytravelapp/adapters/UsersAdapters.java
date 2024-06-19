package com.example.mytravelapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelapp.R;
import com.example.mytravelapp.databinding.ItemContainerUserBinding;
import com.example.mytravelapp.listeners.UserListener;
import com.example.mytravelapp.models.User;

import java.util.List;

public class UsersAdapters extends RecyclerView.Adapter<UsersAdapters.UserViewHolder> {

    private List<User> users;
    private final UserListener userListener;

    public UsersAdapters(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    public void updateUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);

            // Decode and set the user's profile image
            Bitmap userBitmap = getUserImage(user.image);
            if (userBitmap != null) {
                binding.imageProfile.setImageBitmap(userBitmap);
            } else {
                binding.imageProfile.setImageResource(R.drawable.default_profile_image);
            }

            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        if (encodedImage == null) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("UsersAdapters", "Invalid Base64 string", e);
            return null;
        }
    }
}
