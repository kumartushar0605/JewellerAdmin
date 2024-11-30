package com.example.gehnamall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;

    private Spinner categoryDropdown, caratDropdown ,giftingDropdown,occasionDropdown,subcategoryDropdown;
    private EditText weightInput, wattageInput, solmateInput;
    private Button submitButton , mangeContent;
    private RadioGroup genderRadioGroup;

    private List<Bitmap> capturedImages = new ArrayList<>();
    private final int MAX_PHOTOS = 7;
    private final int MIN_PHOTOS = 4;

    // Add a RecyclerView or similar layout to display multiple photos
    private RecyclerView photosRecyclerView;
    private PhotosAdapter photosAdapter;

    private ImageView cameraIcon, capturedImageView;

    private Bitmap capturedImageBitmap;

    private final String BACKEND_CATEGORY_URL = "http://3.110.34.172:8080/api/categories"; // Replace with your category API
    private final String BACKEND_CARAT_URL = "http://3.110.34.172:8080/api/prices"; // Replace with your carat API
    private final String BACKEND_SUBMIT_URL = "http://3.110.34.172:8080/admin/upload/Products?category=4001&subCategory=6001&"; // Replace with your submit API
//http://3.110.34.172:8080/api/gifting
    //http://3.110.34.172:8080/api/occasion
    private final String BACKEND_GIFTING_URL = "http://3.110.34.172:8080/api/gifting";
    private final String BACKEND_OCCASION_URL = "http://3.110.34.172:8080/api/occasion";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        categoryDropdown = findViewById(R.id.categoryDropdown);
        caratDropdown = findViewById(R.id.caratDropdown);
        giftingDropdown = findViewById(R.id.giftingDropdown);
        occasionDropdown = findViewById(R.id.occasionDropdown);
        weightInput = findViewById(R.id.weightInput);
        wattageInput = findViewById(R.id.wattageInput);
        solmateInput = findViewById(R.id.solmateInput);
        submitButton = findViewById(R.id.submitButton);
        cameraIcon = findViewById(R.id.cameraIcon);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
         subcategoryDropdown = findViewById(R.id.subcategoryDropdown);
        View subcategoryLabel = findViewById(R.id.subcategoryLabel);
        mangeContent = findViewById(R.id.mangeContent);


        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter(capturedImages);
        photosRecyclerView.setAdapter(photosAdapter);

        // Fetch dropdown data from backend
        fetchDropdownData(BACKEND_CATEGORY_URL, categoryDropdown);
        fetchDropdownDataKarat(BACKEND_CARAT_URL, caratDropdown);
        fetchDropdownDataGifting(BACKEND_GIFTING_URL, giftingDropdown);
        fetchDropdownDataOccasion(BACKEND_OCCASION_URL, occasionDropdown);


        // Retrieve the token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);

        if (token != null) {
            Log.d("HomeActivity", "Retrieved Token: " + token);
            // Use the token for your logic
        } else {
            Log.d("HomeActivity", "No token found!");
        }

        mangeContent.setOnClickListener(v -> mC());
        // Camera functionality
        cameraIcon.setOnClickListener(v -> {
            if (capturedImages.size() < MAX_PHOTOS) {
                openCamera();
            } else {
                Toast.makeText(this, "You can capture a maximum of " + MAX_PHOTOS + " photos", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit data to backend
        submitButton.setOnClickListener(v -> submitData());


        // Show/hide gender RadioGroup based on category selection
        categoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categoryDropdown.getSelectedItem().toString();

                if (selectedCategory.equalsIgnoreCase("Gold") || selectedCategory.equalsIgnoreCase("Diamond")) {
                    genderRadioGroup.setVisibility(View.VISIBLE);
                    subcategoryLabel.setVisibility(View.VISIBLE);
                    subcategoryDropdown.setVisibility(View.VISIBLE);
                } else {
                    genderRadioGroup.setVisibility(View.GONE);
                    subcategoryLabel.setVisibility(View.GONE);
                    subcategoryDropdown.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genderRadioGroup.setVisibility(View.GONE);
            }
        });

        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String url;
            if (checkedId == R.id.maleRadioButton) {
                url = "http://3.110.34.172:8080/api/subCategories/4002?genderCode=1";

            } else if (checkedId == R.id.femaleRadioButton) {
                url = "http://3.110.34.172:8080/api/subCategories/4002?genderCode=2";

            } else {
                return;
            }
            if(!url.isEmpty()){
                fetchDropdownDataForSubcategory(url, subcategoryDropdown);
            }
        });

    }
    private void fetchDropdownDataForSubcategory(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch subcategories", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> subcategories = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            subcategories.add(jsonArray.getJSONObject(i).getString("subcategoryName"));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, subcategories);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error parsing subcategories", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch subcategories: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void fetchDropdownDataKarat(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        Set<String> karatSet = new HashSet<>();  // Use a Set to store unique karat values

                        // Iterate over the JSON array and extract keys containing 'K'
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject metalData = jsonArray.getJSONObject(i);

                            // Iterate through the keys of the JSONObject
                            Iterator<String> keys = metalData.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                if (key.endsWith("K:")) {  // Check if the key ends with 'K:'
                                    karatSet.add(key.replace(":", ""));  // Remove the ':' and add the karat value (like '14K')
                                }
                            }
                        }

                        // Convert the set to a list and update the spinner
                        List<String> karatItems = new ArrayList<>(karatSet);
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, karatItems);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchDropdownData(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("categoryName"));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, items);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
    private void fetchDropdownDataOccasion(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("giftingName"));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, items);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
    private void fetchDropdownDataGifting(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("giftingName"));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, items);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void mC(){
        Intent intent = new Intent(HomeActivity.this, HomeActivity2.class);
        startActivity(intent);
    }


    private void submitData() {
        String category = categoryDropdown.getSelectedItem().toString();
        String carat = caratDropdown.getSelectedItem().toString();
        String subCategory = subcategoryDropdown.getSelectedItem().toString();
        String gifting = giftingDropdown.getSelectedItem().toString();
        String occasion = occasionDropdown.getSelectedItem().toString();
        String weight = weightInput.getText().toString().trim();
        String wattage = wattageInput.getText().toString().trim();
        String solmate = solmateInput.getText().toString().trim();
        String categoryCode = category.replace(",", "").trim().replaceAll("[a-zA-Z]", "");
        String subCategoryCode = subCategory.replace(",", "").trim().replaceAll("[a-zA-Z]", "");

        // Validate input fields
        if (weight.isEmpty() || wattage.isEmpty() || solmate.isEmpty() || gifting.isEmpty() || occasion.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Gold") || category.equals("Diamond")) {
            if (subCategory.isEmpty()) {
                Toast.makeText(this, "Please fill the subcategory details", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (capturedImages.size() < MIN_PHOTOS) {
            Toast.makeText(this, "Please capture at least " + MIN_PHOTOS + " photos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        if (token == null) {
            Toast.makeText(this, "Authorization token not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the multipart request
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("category", categoryCode)
                .addFormDataPart("karat", carat)
                .addFormDataPart("gifting", gifting)
                .addFormDataPart("occasion", occasion)
                .addFormDataPart("weight", weight)
                .addFormDataPart("wastage", wattage)
                .addFormDataPart("soulmate", solmate);

        if (category.equals("Gold") || category.equals("Diamond")) {
            multipartBuilder.addFormDataPart("subCategory", subCategoryCode);
        }

        // Add images to the multipart request
        for (int i = 0; i < capturedImages.size(); i++) {
            Bitmap bitmap = capturedImages.get(i);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            multipartBuilder.addFormDataPart(
                    "images", // Field name
                    "image_" + i + ".jpg", // File name
                    RequestBody.create(byteArray, MediaType.parse("image/jpeg")) // Image content
            );
        }

        MultipartBody requestBody = multipartBuilder.build();

        // Prepare the request
        Request request = new Request.Builder()
                .url(BACKEND_SUBMIT_URL)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Send the request
        System.out.println("hiiiiiiii"+token);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    System.out.println("hiiiiiiiifffttt"+token);
                    Toast.makeText(HomeActivity.this, "Failed to submit data", Toast.LENGTH_SHORT).show();
                    Log.e("HomeActivity", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        System.out.println("hiiiiiiiiipppp"+token+response);
                        Toast.makeText(HomeActivity.this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("hiiiiiiiiffff"+token);
                        System.out.println(response);
                        Toast.makeText(HomeActivity.this, "Submission failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                capturedImages.add(photo);
                photosAdapter.notifyDataSetChanged();
            }
        }
    }
}
