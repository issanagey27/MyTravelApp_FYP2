package com.example.mytravelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mytravelapp.R;
import com.example.mytravelapp.models.Plans;

import java.util.List;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.PlansViewHolder> {

    private final List<Plans> plansList;
    private final OnItemClickListener listener;
    private final OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Plans plan);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Plans plan);
    }

    public PlansAdapter(List<Plans> plansList, OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.plansList = plansList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PlansViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlansViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_plans_layout, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PlansViewHolder holder, int position) {
        holder.bind(plansList.get(position), listener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return plansList.size();
    }

    static class PlansViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageItem;
        private final TextView textName;
        private final Button deleteButton;

        PlansViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.imageItem);
            textName = itemView.findViewById(R.id.textName);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Plans plan, final OnItemClickListener listener, final OnDeleteClickListener deleteListener) {
            textName.setText(plan.getName());
            String imageUrl = plan.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_image))
                        .into(imageItem);
            } else {
                imageItem.setImageResource(R.drawable.placeholder_image);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(plan));

            deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(plan)); // Ensure this line is correctly wired
        }
    }
}
