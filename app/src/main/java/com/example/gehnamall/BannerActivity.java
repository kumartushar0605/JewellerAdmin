package com.example.gehnamall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BannerActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BANNER = 1;
    private RecyclerView recyclerViewBanners;
    private BannerAdapter bannerAdapter;
    private List<BannerItem> bannerItems = new ArrayList<>();
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", null);

        recyclerViewBanners = findViewById(R.id.recyclerViewBanners);
        recyclerViewBanners.setLayoutManager(new LinearLayoutManager(this));

        bannerAdapter = new BannerAdapter(bannerItems, this::removeBanner);
        recyclerViewBanners.setAdapter(bannerAdapter);

        Button btnAddBannerImage = findViewById(R.id.btnAddBannerImage);
        btnAddBannerImage.setOnClickListener(view -> openGallery(REQUEST_CODE_BANNER));

        Button btnSubmit = findViewById(R.id.submit);
        btnSubmit.setOnClickListener(view -> submitData());

        fetchExistingBanners();
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null && requestCode == REQUEST_CODE_BANNER) {
                bannerItems.add(new BannerItem(selectedImageUri, false));
                bannerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void fetchExistingBanners() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://3.110.34.172:8080/api/banners")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(BannerActivity.this, "Failed to fetch banners", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray bannersArray = new JSONArray(response.body().string());
                        bannerItems.clear();
                        for (int i = 0; i < bannersArray.length(); i++) {
                            JSONObject bannerObject = bannersArray.getJSONObject(i);
                            String imageUrl = bannerObject.getString("imageUrl");
                            int bannerId = bannerObject.getInt("bannerId");
                            bannerItems.add(new BannerItem(imageUrl,true,bannerId));
                        }
                        runOnUiThread(() -> bannerAdapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        Log.e("ParseBanners", "Error parsing banners", e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(BannerActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void removeBanner(BannerItem bannerItem) {
        if (bannerItem.isFromBackend()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://3.110.34.172:8080/admin/delete/banners/" + bannerItem.getBannerId())
                    .delete()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(BannerActivity.this, "Failed to delete banner", Toast.LENGTH_SHORT).show());
                    Log.e("DeleteBanner", "Error deleting banner", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            bannerItems.remove(bannerItem);
                            bannerAdapter.notifyDataSetChanged();
                        });
                    } else {
                        Log.e("DeleteBanner", "Failed response: " + response.body().toString());
                    }
                }
            });
        } else {
            bannerItems.remove(bannerItem);
            bannerAdapter.notifyDataSetChanged();
        }
    }

    private void submitData() {
        // Get the values for Name and Description from the UI (EditText fields)
        EditText editTextName = findViewById(R.id.inputBannerName);
        EditText editTextDescription = findViewById(R.id.inputBannerDescription);

        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Ensure both name and description are not empty
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Name and description are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Add Name and Description to the multipart request
        multipartBuilder.addFormDataPart("bannerName", name);
        multipartBuilder.addFormDataPart("description", description);

        // Add each banner image (compressed to WebP)
        for (int i = 0; i < bannerItems.size(); i++) {
            BannerItem bannerItem = bannerItems.get(i);
            if (!bannerItem.isFromBackend() && bannerItem.getImageUri() != null) {
                try {
                    // Convert image URI to Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), bannerItem.getImageUri());

                    // Compress the Bitmap to WebP format with 80 quality
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    // Add the compressed WebP image to the multipart request
                    multipartBuilder.addFormDataPart(
                            "images", // Field name
                            "image_" + i + ".webp", // File name
                            RequestBody.create(byteArray, MediaType.parse("image/webp")) // Image content
                    );
                } catch (IOException e) {
                    Toast.makeText(this, "Error preparing image: " + bannerItem.getImageUri(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }
            }
        }

        // Build the request body with the multipart data
        MultipartBody requestBody = multipartBuilder.build();

        // Prepare the POST request
        Request request = new Request.Builder()
                .url("http://3.110.34.172:8080/admin/upload/Banner")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(BannerActivity.this, "Failed to upload banners", Toast.LENGTH_SHORT).show());
                Log.e("UploadBanners", "Error uploading banners", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(BannerActivity.this, "Banners uploaded successfully!", Toast.LENGTH_SHORT).show();
                        fetchExistingBanners(); // Refresh the banner list
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(BannerActivity.this, "Failed to upload banners"+response.body(), Toast.LENGTH_SHORT).show());
                    Log.e("UploadBanners", "Failed response: " + response.body());
                }
            }
        });
    }
}
