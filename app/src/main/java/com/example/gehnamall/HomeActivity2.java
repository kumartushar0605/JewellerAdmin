package com.example.gehnamall;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.*;

public class HomeActivity2 extends AppCompatActivity {
    private static final int REQUEST_CODE_BANNER = 1;
    private static final int REQUEST_CODE_TESTIMONIAL = 2;

    private Uri bannerUri;
    private Uri testimonialUri;

    private Spinner dropdownMetal;
    private Spinner dropdownKarat;

    private HashMap<String, JSONObject> metalDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        Button btnAddBannerImage = findViewById(R.id.btnAddBannerImage);
        Button btnAddTestimonialImage = findViewById(R.id.btnAddTestimonialImage);
        Button btnSubmitPrice = findViewById(R.id.btnSubmitPrice);

        dropdownMetal = findViewById(R.id.dropdownPrice);
        dropdownKarat = findViewById(R.id.dropdownKarat);

        ImageView bannerImagePreview = findViewById(R.id.bannerImagePreview);
        ImageView testimonialImagePreview = findViewById(R.id.testimonialImagePreview);

        btnAddBannerImage.setOnClickListener(view -> openGallery(REQUEST_CODE_BANNER));
        btnAddTestimonialImage.setOnClickListener(view -> openGallery(REQUEST_CODE_TESTIMONIAL));
        btnSubmitPrice.setOnClickListener(view -> submitData(bannerImagePreview, testimonialImagePreview));

        fetchPrices();
        setupMetalDropdownListener();
    }

    private void fetchPrices() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://3.110.34.172:8080/api/prices")
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    List<String> metalNames = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject metalData = jsonArray.getJSONObject(i);
                        String metalName = metalData.getString("metalName");
                        metalNames.add(metalName);
                        metalDataMap.put(metalName, metalData);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, metalNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dropdownMetal.setAdapter(adapter);
                    });
                } else {
                    throw new IOException("Failed to fetch prices");
                }
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error fetching prices: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("FetchPrices", "Error", e);
            }
        }).start();
    }

    private void setupMetalDropdownListener() {
        dropdownMetal.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedMetal = dropdownMetal.getSelectedItem().toString();
                JSONObject metalData = metalDataMap.get(selectedMetal);

                if (metalData != null) {
                    dropdownKarat.setVisibility(View.VISIBLE); // Show dropdownKarat when a metal is selected
                    List<String> karats = new ArrayList<>();
                    try {
                        // Get an iterator over the keys of the JSON object
                        Iterator<String> keys = metalData.keys();

                        // Iterate over the keys in the metalData JSON object
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.endsWith("K:")) {
                                karats.add(key.replace(":", "")); // Remove ':' from the key
                            }
                        }

                        // Set the adapter with the extracted karat values
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity2.this, android.R.layout.simple_spinner_item, karats);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dropdownKarat.setAdapter(adapter);
                    } catch (Exception e) {
                        Log.e("DropdownListener", "Error parsing karats", e);
                    }
                } else {
                    dropdownKarat.setVisibility(View.GONE); // Hide dropdownKarat if no valid metal is selected
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                dropdownKarat.setVisibility(View.GONE); // Hide dropdownKarat when no metal is selected
            }
        });
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
                    showPreview(R.id.bannerImagePreview, bannerUri);
                } else if (requestCode == REQUEST_CODE_TESTIMONIAL) {
                    testimonialUri = selectedImageUri;
                    showPreview(R.id.testimonialImagePreview, testimonialUri);
                }
            }
        }
    }

    private void showPreview(int imageViewId, Uri imageUri) {
        ImageView imageView = findViewById(imageViewId);
        imageView.setImageURI(imageUri);
        imageView.setVisibility(View.VISIBLE);
    }

    private void submitData(ImageView bannerImagePreview, ImageView testimonialImagePreview) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        if (token == null) {
            Toast.makeText(this, "Authorization token not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedMetal = dropdownMetal.getSelectedItem() != null ? dropdownMetal.getSelectedItem().toString() : "";
        String selectedKarat = dropdownKarat.getSelectedItem() != null ? dropdownKarat.getSelectedItem().toString() : "";
        String enteredPrice = ((android.widget.EditText) findViewById(R.id.inputPrice)).getText().toString().trim();

        if (selectedMetal.isEmpty() || selectedKarat.isEmpty() || enteredPrice.isEmpty() || bannerUri == null || testimonialUri == null) {
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Upload Banner
                uploadImage(client, token, "http://3.110.34.172:8080/admin/upload/Banner", bannerUri);
                // Upload Testimonial
                uploadImage(client, token, "http://3.110.34.172:8080/admin/upload/testimonial", testimonialUri);

                // Submit Price Data
                submitPriceData(client, token, selectedMetal, selectedKarat, enteredPrice);

                runOnUiThread(() -> Toast.makeText(this, "Data submitted successfully!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("SubmitData", "Error submitting data", e);
            }
        }).start();
    }

    private void submitPriceData(OkHttpClient client, String token, String metal, String karat, String price) throws IOException {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("metalType", metal);
            jsonObject.put("karat", karat);
            jsonObject.put("price", price);
        } catch (JSONException e) {
            throw new IOException("Failed to create JSON payload", e);
        }

        RequestBody requestBody = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://3.110.34.172:8080/admin/update/price")
                .put(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to submit price data: " + response.body().string());
        }
    }

    private void uploadImage(OkHttpClient client, String token, String url, Uri imageUri) throws IOException {
        File imageFile = createFileFromUri(imageUri);
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Failed to resolve or create a valid file from URI: " + imageUri.toString());
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to upload image: " + response.body().string());
        }
    }

    private File createFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), "temp_image.jpg");

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
