package com.example.mytravelapp.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.example.mytravelapp.activities.AccommodationDetails;
import com.example.mytravelapp.activities.RestaurantDetails;
import com.example.mytravelapp.activities.ThingsToDoDetails;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserPlansAdapter extends RecyclerView.Adapter<UserPlansAdapter.ViewHolder> {
    private final FragmentActivity activity;
    private final List<DocumentSnapshot> userSavedPlans;
    private final String destinationId;
    private final String fragmentType;
    private final String userEmail;
    private final String planName;

    public UserPlansAdapter(FragmentActivity activity, List<DocumentSnapshot> userSavedPlans, String destinationId, String fragmentType, String userEmail, String planName) {
        this.activity = activity;
        this.userSavedPlans = userSavedPlans;
        this.destinationId = destinationId;
        this.fragmentType = fragmentType;
        this.userEmail = userEmail;
        this.planName = planName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot userSavedPlan = userSavedPlans.get(position);

        // Set image using Glide library
        String imageUrl = userSavedPlan.getString("image_url");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(activity)
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.imageView);
        }

        // Set name if available
        String name = userSavedPlan.getString("name");
        if (name != null && !name.isEmpty()) {
            holder.textViewName.setText(name);
        }

        // Handle click on details button
        holder.buttonDetails.setOnClickListener(v -> {
            // Retrieve the auto-generated document ID
            String userSavedPlanId = userSavedPlan.getId();

            // Start the appropriate activity based on the fragment type
            Intent intent = null;
            if (fragmentType.equals("accommodations")) {
                intent = new Intent(v.getContext(), AccommodationDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("destinationId", destinationId);
                intent.putExtra("accommodationId", userSavedPlanId);
            } else if (fragmentType.equals("restaurants")) {
                intent = new Intent(v.getContext(), RestaurantDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("restaurantId", userSavedPlanId);
            } else if (fragmentType.equals("thingstodo")) {
                intent = new Intent(v.getContext(), ThingsToDoDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("thingsToDoId", userSavedPlanId);
            }
            intent.putExtra("destinationId", destinationId);
            v.getContext().startActivity(intent);
        });

        // Handle click on delete button
        holder.buttonDelete.setOnClickListener(v -> {
            // Implement deletion logic here
            deleteItem(position);
        });
    }

    @Override
    public int getItemCount() {
        return userSavedPlans.size();
    }

    private void deleteItem(int position) {
        DocumentSnapshot document = userSavedPlans.get(position);

        // Assuming 'fragmentType' defines the collection ('accommodations', 'restaurants', 'thingstodo')
        String documentId = document.getId();

        // Perform deletion from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_plans")
                .document(userEmail)
                .collection("plans")
                .document(planName)
                .collection(fragmentType)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove item from list and notify adapter
                    userSavedPlans.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, userSavedPlans.size());
                    Toast.makeText(activity, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Error deleting item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UserPlansAdapter", "Error deleting item", e);
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        Button buttonDetails;
        Button buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewName = itemView.findViewById(R.id.textViewName);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
