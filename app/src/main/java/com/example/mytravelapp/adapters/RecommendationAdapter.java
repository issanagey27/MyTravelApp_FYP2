package com.example.mytravelapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.example.mytravelapp.activities.AccommodationDetails;
import com.example.mytravelapp.activities.RestaurantDetails;
import com.example.mytravelapp.activities.ThingsToDoDetails;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final FragmentActivity activity;
    private final List<DocumentSnapshot> recommendations;
    private final String destinationId;
    private final String fragmentType;
    private final String userEmail;
    private final String planName;
    private String selectedFilterType; // Track the currently selected filter type

    public RecommendationAdapter(FragmentActivity activity, List<DocumentSnapshot> recommendations, String destinationId, String fragmentType, String userEmail, String planName) {
        this.activity = activity;
        this.recommendations = recommendations;
        this.destinationId = destinationId;
        this.fragmentType = fragmentType;
        this.userEmail = userEmail;
        this.planName = planName;
        this.selectedFilterType = "All"; // Initialize selected filter type to "All"
    }

    // Update filter type when spinner selection changes
    public void setSelectedFilterType(String filterType) {
        this.selectedFilterType = filterType;
        notifyDataSetChanged(); // Notify adapter that data set has changed
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot recommendation = recommendations.get(position);

        // Check if item matches the selected filter type
        String itemType = recommendation.getString("type");
        if (!selectedFilterType.equals("All") && !itemType.equalsIgnoreCase(selectedFilterType)) {
            // Hide the item if it doesn't match the selected filter type
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        } else {
            // Show the item if it matches the selected filter type or "All" is selected
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Set image using Glide library
        String imageUrl = recommendation.getString("image_url");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(activity)
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.imageView);
        }

        // Set name if available
        String name = recommendation.getString("name");
        if (name != null && !name.isEmpty()) {
            holder.textViewName.setText(name);
        }

        holder.buttonDetails.setOnClickListener(v -> {
            // Retrieve the auto-generated document ID
            String recommendationId = recommendation.getId();

            // Start the appropriate activity based on the fragment type
            Intent intent = null;
            if (fragmentType.equals("accommodation")) {
                intent = new Intent(v.getContext(), AccommodationDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("destinationId", destinationId);
                intent.putExtra("accommodationId", recommendationId);
            } else if (fragmentType.equals("restaurant")) {
                intent = new Intent(v.getContext(), RestaurantDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("restaurantId", recommendationId);
            } else if (fragmentType.equals("thingstodo")) {
                intent = new Intent(v.getContext(), ThingsToDoDetails.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("planName", planName);
                intent.putExtra("thingsToDoId", recommendationId);
            }
            intent.putExtra("destinationId", destinationId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        Button buttonDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewName = itemView.findViewById(R.id.textViewName);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);
        }
    }

    public void updateRecommendations(List<DocumentSnapshot> newRecommendations) {
        recommendations.clear();
        recommendations.addAll(newRecommendations);
        notifyDataSetChanged();
    }
}

