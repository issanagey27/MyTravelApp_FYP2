package com.example.mytravelapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytravelapp.R;
import com.example.mytravelapp.models.LocationItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationItem> locationItemList;
    private OnItemClickListener itemClickListener;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;
    private Context context;

    public LocationAdapter(List<LocationItem> locationItemList, OnItemClickListener itemClickListener, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.locationItemList = locationItemList;
        this.itemClickListener = itemClickListener;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationItem locationItem = locationItemList.get(position);
        holder.bind(locationItem, itemClickListener, editClickListener, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return locationItemList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private ImageView imageViewLocation;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        LocationViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewLocation = itemView.findViewById(R.id.imageViewLocation);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(final LocationItem locationItem, final OnItemClickListener itemClickListener, final OnEditClickListener editClickListener, final OnDeleteClickListener deleteClickListener) {
            textViewName.setText(locationItem.getName());
            Glide.with(context)
                    .load(locationItem.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageViewLocation);

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));

            buttonEdit.setOnClickListener(v -> editClickListener.onEditClick(getAdapterPosition()));

            buttonDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(getAdapterPosition()));
        }
    }
}
