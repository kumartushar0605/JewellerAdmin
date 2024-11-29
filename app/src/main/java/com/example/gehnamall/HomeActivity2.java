package com.example.gehnamall;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.*;

public class HomeActivity2 extends AppCompatActivity {
    private static final int REQUEST_CODE_BANNER = 1;
    private static final int REQUEST_CODE_TESTIMONIAL = 2;

    private LinearLayout dynamicContentLayout;
    private Uri bannerUri;
    private Uri testimonialUri;
    private EditText inputPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        Button btnAddBanner = findViewById(R.id.btnAddBanner);
        Button btnAddTestimonial = findViewById(R.id.btnAddTestimonial);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        inputPrice = findViewById(R.id.inputPrice);
        dynamicContentLayout = findViewById(R.id.dynamicContentLayout);

        btnAddBanner.setOnClickListener(view -> openGallery(REQUEST_CODE_BANNER));
        btnAddTestimonial.setOnClickListener(view -> openGallery(REQUEST_CODE_TESTIMONIAL));
        btnSubmit.setOnClickListener(view -> submitData());
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
            if (selectedImageUri != null) {
                if (requestCode == REQUEST_CODE_BANNER) {
                    bannerUri = selectedImageUri;
                    addSection("Banner", bannerUri);
                } else if (requestCode == REQUEST_CODE_TESTIMONIAL) {
                    testimonialUri = selectedImageUri;
                    addSection("Testimonial", testimonialUri);
                }
            }
        }
    }

    private void addSection(String label, Uri imageUri) {
        // Add a title for the section
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(label);
        sectionTitle.setTextSize(18);
        sectionTitle.setTextColor(Color.BLACK);
        sectionTitle.setPadding(8, 8, 8, 4);

        // Add the selected image
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500 // Fixed height for uniformity
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(0, 8, 0, 8);

        // Add the title and image to the layout
        dynamicContentLayout.addView(sectionTitle);
        dynamicContentLayout.addView(imageView);
    }

    private void submitData() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        if (token == null) {
            Toast.makeText(this, "Authorization token not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String price = inputPrice.getText().toString();
        if (price.isEmpty() || bannerUri == null || testimonialUri == null) {
            Toast.makeText(this, "Please select images and enter a price!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Upload Banner
                uploadImage(client, token, "http://192.168.148.25:9191/admin/upload/Banner", bannerUri);
                // Update Price
                updatePrice(client, token, "http://192.168.148.25:9191/admin/update/price", price);
                // Upload Testimonial
                uploadImage(client, token, "http://192.168.148.25:9191/admin/upload/testimonial", testimonialUri);



                runOnUiThread(() -> Toast.makeText(this, "Data submitted successfully!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("SubmitData", "Error submitting data", e);
            }
        }).start();
    }

    private void uploadImage(OkHttpClient client, String token, String url, Uri imageUri) throws IOException {
        File imageFile = createFileFromUri(imageUri);
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Failed to resolve or create a valid file from URI: " + imageUri.toString());
        }

        Log.d("UploadImage", "Uploading file: " + imageFile.getAbsolutePath());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", imageFile.getName(), // Field name must be "images"
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            Log.e("UploadImage", "Upload failed: " + responseBody);
            throw new IOException("Failed to upload image: " + responseBody);
        }
        Log.d("UploadImage", "Upload successful.");
    }

    private File createFileFromUri(Uri uri) throws IOException {
        String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(getCacheDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            if (inputStream == null) {
                throw new IOException("InputStream is null for URI: " + uri.toString());
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Error creating temp file from URI: " + uri.toString(), e);
        }

        return tempFile;
    }

    private void updatePrice(OkHttpClient client, String token, String url, String price) throws IOException {
        RequestBody requestBody = new FormBody.Builder()
                .add("price", price)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            Log.e("UpdatePrice", "Failed to update price: " + responseBody);
            throw new IOException("Failed to update price: " + responseBody);
        }
        Log.d("UpdatePrice", "Price update successful.");
    }
}
