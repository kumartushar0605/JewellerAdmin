package com.example.gehnamall;

import android.net.Uri;

public class BannerItem {

    private String imageUrl;
    private Uri imageUri;
    private boolean isFromBackend;
    private int bannerId;

    public BannerItem(String imageUrl, boolean isFromBackend, int bannerId) {
        this.imageUrl = imageUrl;
        this.isFromBackend = isFromBackend;
        this.bannerId = bannerId;
    }

    public BannerItem(Uri imageUri, boolean isFromBackend) {
        this.imageUri = imageUri;
        this.isFromBackend = isFromBackend;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public boolean isFromBackend() {
        return isFromBackend;
    }

    public int getBannerId() {
        return bannerId;
    }
}
