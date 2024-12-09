package com.example.gehnamall;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {
    private final List<Bitmap> photos;

    public PhotosAdapter(List<Bitmap> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Bitmap photo = photos.get(position);
        holder.imageView.setImageBitmap(photo);

        // Handle cross button click to remove the image
        holder.crossButton.setOnClickListener(v -> {
            photos.remove(position); // Remove the image from the list
            notifyItemRemoved(position); // Notify the adapter about the removed item
            notifyItemRangeChanged(position, photos.size()); // Update the range for RecyclerView
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton crossButton;

        PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
            crossButton = itemView.findViewById(R.id.crossButton); // Reference to the cross button
        }
    }
}
