package com.example.gehnamall;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



        import android.graphics.Bitmap;


        import androidx.annotation.NonNull;


        import android.provider.MediaStore;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.RadioGroup;
        import android.widget.Spinner;
        import android.widget.Toast;

        import com.google.android.material.bottomnavigation.BottomNavigationView;
        import com.google.android.material.navigation.NavigationView;

        import org.jetbrains.annotations.NotNull;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.io.ByteArrayOutputStream;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.HashSet;
        import java.util.Iterator;
        import java.util.List;
        import java.util.Map;
        import java.util.Set;

        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.MediaType;
        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;




public class sngold_fragment extends Fragment {
    private Spinner categoryDropdown, subcategoryDropdown;
    private static final int REQUEST_CODE_BANNER = 1;
    private RecyclerView photosRecyclerView;
    private PhotosAdapter photosAdapter;
    private List<Bitmap> capturedImages = new ArrayList<>();
    private EditText editTextLength, editTextSize, editTextKarat, editTextWeight, editTextWastage, editTextTagNumber;
    private RadioGroup genderRadioGroup;
    private Map<String, String> categoryMap;
    private Map<String, String> subcategoryMap;
    private String token;
    private static final String BACKEND_CATEGORY_URL = "http://3.110.34.172:8080/api/categories?wholeseller=SLGOLD";
    private static final String BACKEND_SUBMIT_URL = "http://3.110.34.172:8080/admin/upload/Products";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bansal, container, false);

        // Fetch the token from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", null);

        // Initialize UI components
        initUI(view);

        // Fetch category data
        fetchDropdownData(BACKEND_CATEGORY_URL, categoryDropdown);

        // Handle subcategory fetching
        categoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = (String) categoryDropdown.getSelectedItem();
                if ("mala".equalsIgnoreCase(category)) {
                    editTextLength.setVisibility(View.VISIBLE);
                    editTextSize.setVisibility(View.GONE);
                } else if ("bangle".equalsIgnoreCase(category)) {
                    editTextLength.setVisibility(View.GONE);
                    editTextSize.setVisibility(View.VISIBLE);
                } else {
                    // Default visibility for other categories
                    editTextLength.setVisibility(View.GONE);
                    editTextSize.setVisibility(View.GONE);
                }

                String categoryCode = categoryMap.get(category);
                Toast.makeText(getContext(), category+"  "+categoryCode, Toast.LENGTH_SHORT).show();
                fetchDropdownDataForSubcategory(subcategoryDropdown,categoryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where no selection is made
            }
        });

        return view;
    }

    private void initUI(View view) {
        // Initialize all UI elements
        categoryDropdown = view.findViewById(R.id.spinnerCategory);
        subcategoryDropdown = view.findViewById(R.id.spinnerSubCategory);
        editTextLength = view.findViewById(R.id.editTextLength);
        editTextSize = view.findViewById(R.id.editTextSize);
        editTextKarat = view.findViewById(R.id.editTextKarat);
        editTextWeight = view.findViewById(R.id.editTextWeight);
        editTextWastage = view.findViewById(R.id.editTextWastage);
        editTextTagNumber = view.findViewById(R.id.editTextTagNumber);
        photosRecyclerView = view.findViewById(R.id.photosRecyclerrView);

        // RecyclerView for captured images
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter(capturedImages);
        photosRecyclerView.setAdapter(photosAdapter);

        // Button click listeners
        Button btnAddBannerImage = view.findViewById(R.id.btnAddBannerImage);
        Button submitButton = view.findViewById(R.id.buttonSubmit);

        btnAddBannerImage.setOnClickListener(v -> openGallery(REQUEST_CODE_BANNER));
        submitButton.setOnClickListener(v -> submitData());
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CODE_BANNER) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                    capturedImages.add(bitmap);
                    photosAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void fetchDropdownData(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && isAdded()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> categoryNames = new ArrayList<>();
                        categoryMap = new HashMap<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            String categoryName = item.getString("categoryName");
                            String categoryCode = item.getString("categoryCode");
                            categoryNames.add(categoryName);
                            categoryMap.put(categoryName, categoryCode);
                        }

                        requireActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void fetchDropdownDataForSubcategory(Spinner dropdown ,String categoryCode ) {

        String url = "http://3.110.34.172:8080/api/subCategories/" + categoryCode + "?&wholeseller=SLGOLD";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch subcategories", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && isAdded()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> subcategories = new ArrayList<>();
                        subcategoryMap = new HashMap<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String subcategoryName = jsonArray.getJSONObject(i).getString("subcategoryName");
                            String subcategoryCode = jsonArray.getJSONObject(i).getString("subcategoryCode");
                            subcategories.add(subcategoryName);
                            subcategoryMap.put(subcategoryName, subcategoryCode);
                        }

                        requireActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, subcategories);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dropdown.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error parsing subcategories", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void submitData() {
        String category = categoryDropdown.getSelectedItem().toString();
        String subCategory = subcategoryDropdown.getSelectedItem().toString();
        String karat = editTextKarat.getText().toString();
        String weight = editTextWeight.getText().toString();
        String wastage = editTextWastage.getText().toString();
        String tagNumber = editTextTagNumber.getText().toString();
        String length = editTextLength.getText().toString();
        String size = editTextSize.getText().toString();
        String categoryCode = categoryMap.get(category);
        String subCategoryCode = subcategoryMap.get(subCategory);

        if (categoryCode == null && subCategoryCode == null) {
            Toast.makeText(getActivity(), "Error: category and subcategory", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        String identity = sharedPreferences.getString("identity", null);

        if (token == null && identity == null) {
            Toast.makeText(getActivity(), "Authorization token not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("karat", karat)
                .addFormDataPart("weight", weight)
                .addFormDataPart("wastage", wastage)
                .addFormDataPart("tagNumber", tagNumber);

        // Add optional fields based on category
        if (!length.isEmpty()) {
            multipartBuilder.addFormDataPart("length", length);
        }
        if (!size.isEmpty()) {
            multipartBuilder.addFormDataPart("size", size);
        }
        if (capturedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Please upload at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        // Add each banner image (compressed to WebP)
        // Add images to the multipart request
        for (int i = 0; i < capturedImages.size(); i++) {
            Bitmap bitmap = capturedImages.get(i);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            multipartBuilder.addFormDataPart("images", "image_" + i + ".webp",
                    RequestBody.create(byteArray, MediaType.parse("image/webp")));
        }
        RequestBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(BACKEND_SUBMIT_URL + "?category=" + categoryCode + "&subCategory=" + subCategoryCode + "&wholeseller=" + identity)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "multipart/form-data")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Submission failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Submission failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    }



