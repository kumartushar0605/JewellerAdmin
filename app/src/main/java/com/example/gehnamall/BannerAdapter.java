package com.example.gehnamall;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<BannerItem> bannerItems;
    private final OnBannerRemoveListener onBannerRemoveListener;

    public BannerAdapter(List<BannerItem> bannerItems, OnBannerRemoveListener onBannerRemoveListener) {
        this.bannerItems = bannerItems;
        this.onBannerRemoveListener = onBannerRemoveListener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem bannerItem = bannerItems.get(position);

        if (bannerItem.isFromBackend()) {
            // Load the image from backend URL
            Glide.with(holder.bannerImage.getContext())
                    .load(bannerItem.getImageUrl())
                    .into(holder.bannerImage);
        } else {
            // Load the image from URI (gallery)
            Glide.with(holder.bannerImage.getContext())
                    .load(bannerItem.getImageUri())
                    .into(holder.bannerImage);
        }

        holder.removeButton.setOnClickListener(v -> onBannerRemoveListener.onRemove(bannerItem));
    }

    @Override
    public int getItemCount() {
        return bannerItems.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {

        ImageView bannerImage;
        View removeButton;

        public BannerViewHolder(View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }

    public interface OnBannerRemoveListener {
        void onRemove(BannerItem bannerItem);
    }
}
