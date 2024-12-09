package com.example.gehnamall;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class HomeFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;
    private BottomNavigationView userBottomnevigation;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Spinner categoryDropdown, caratDropdown, giftingDropdown, occasionDropdown, subcategoryDropdown, soulmateDropdown;
    private EditText weightInput,DesInput, wattageInput;
    private Button submitButton;
    private RadioGroup genderRadioGroup;

    private List<Bitmap> capturedImages = new ArrayList<>();
    private final int MAX_PHOTOS = 7;
    private final int MIN_PHOTOS = 4;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Map<String, String> categoryMap;
    private Map<String, String> subcategoryMap;
    // Add a RecyclerView or similar layout to display multiple photos
    private RecyclerView photosRecyclerView;
    private PhotosAdapter photosAdapter;

    private ImageView cameraIcon, capturedImageView;

    private Bitmap capturedImageBitmap;

    private final String BACKEND_CATEGORY_URL = "http://3.110.34.172:8080/api/categories?wholeseller=BANSAL"; // Replace with your category API
    private final String BACKEND_CARAT_URL = "http://3.110.34.172:8080/api/prices"; // Replace with your carat API
    private final String BACKEND_SUBMIT_URL = "http://3.110.34.172:8080/admin/upload/Products"; // Replace with your submit API
    //http://3.110.34.172:8080/api/gifting
    //http://3.110.34.172:8080/api/occasion
    private final String BACKEND_GIFTING_URL = "http://3.110.34.172:8080/api/gifting";
    private final String BACKEND_OCCASION_URL = "http://3.110.34.172:8080/api/occasion";
    private final String BACKEND_SOULMATE_URL = "http://3.110.34.172:8080/api/soulmate";
    private static final int REQUEST_CODE_BANNER = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_home2, container, false);

        // Initialize UI components
        categoryDropdown = view.findViewById(R.id.categoryDropdown);
        caratDropdown = view.findViewById(R.id.caratDropdown);
        giftingDropdown = view.findViewById(R.id.giftingDropdown);
        occasionDropdown = view.findViewById(R.id.occasionDropdown);
        weightInput = view.findViewById(R.id.weightInput);
        wattageInput = view.findViewById(R.id.wattageInput);
        submitButton = view.findViewById(R.id.submitButton);
        soulmateDropdown = view.findViewById(R.id.soulmateDropdown);
        cameraIcon = view.findViewById(R.id.cameraIcon);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        subcategoryDropdown = view.findViewById(R.id.subcategoryDropdown);
        View subcategoryLabel = view.findViewById(R.id.subcategoryLabel);


        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter(capturedImages);
        photosRecyclerView.setAdapter(photosAdapter);

        // Fetch dropdown data from backend
        fetchDropdownData(BACKEND_CATEGORY_URL, categoryDropdown);
        fetchDropdownDataKarat(BACKEND_CARAT_URL, caratDropdown);
        fetchDropdownDataGifting(BACKEND_GIFTING_URL, giftingDropdown);
        fetchDropdownDataOccasion(BACKEND_OCCASION_URL, occasionDropdown);
        fetchDropdownDataSoulmate(BACKEND_SOULMATE_URL, soulmateDropdown);


        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);

        if (token != null) {
            Log.d("MyFragment", "Retrieved Token: " + token);
            // Use the token for your logic
        } else {
            Log.d("MyFragment", "No token found!");
        }


        // Camera functionality
        cameraIcon.setOnClickListener(v -> {
            if (capturedImages.size() < MAX_PHOTOS) {
                Toast.makeText(getActivity(), "Camera ", Toast.LENGTH_SHORT).show();
                System.out.println("camerraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

                openGallery(REQUEST_CODE_BANNER);

            } else {
                Toast.makeText(getActivity(), "You can capture a maximum of " + MAX_PHOTOS + " photos", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit data to backend
        submitButton.setOnClickListener(v -> submitData());


        // Show/hide gender RadioGroup based on category selection
        categoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categoryDropdown.getSelectedItem().toString();

                   if(selectedCategory.equals("Gold") || selectedCategory.equals("Diamond")) {
                genderRadioGroup.setVisibility(View.VISIBLE);


                   }else{
                       genderRadioGroup.setVisibility(View.GONE);

                   }
                subcategoryLabel.setVisibility(View.VISIBLE);
                subcategoryDropdown.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genderRadioGroup.setVisibility(View.GONE);
            }
        });

        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String url;
            if (checkedId == R.id.maleRadioButton) {
                url = "http://3.110.34.172:8080/api/subCategories/4001?gender=1&wholeseller=BANSAL";

            } else if (checkedId == R.id.femaleRadioButton) {
                url = "http://3.110.34.172:8080/api/subCategories/4002?gender=2&wholeseller=BANSAL";

            } else {
                return;
            }
            if (!url.isEmpty()) {
                fetchDropdownDataForSubcategory(url, subcategoryDropdown);
            }
        });
        return view;

    }


    private void fetchDropdownDataForSubcategory(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> subcategories = new ArrayList<>();
                        subcategoryMap = new HashMap<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String name = jsonArray.getJSONObject(i).getString("subcategoryName");
                            String code = jsonArray.getJSONObject(i).getString("subcategoryCode");
                            subcategories.add(name);
                            subcategoryMap.put(name, code);
                        }

                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subcategories);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }

                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing subcategories", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                } else {
                    if (getViewLifecycleOwner() != null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to fetch subcategories: " + response.message(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }


    private void fetchDropdownDataKarat(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        Set<String> karatSet = new HashSet<>(); // Use a Set to store unique karat values

                        // Iterate over the JSON array and extract keys containing 'K'
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject metalData = jsonArray.getJSONObject(i);

                            // Iterate through the keys of the JSONObject
                            Iterator<String> keys = metalData.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                if (key.endsWith("K:")) { // Check if the key ends with 'K:'
                                    karatSet.add(key.replace(":", "")); // Remove ':' and add the karat value (e.g., '14K')
                                }
                            }
                        }

                        // Convert the set to a list and update the spinner on the UI thread
                        List<String> karatItems = new ArrayList<>(karatSet);
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, karatItems);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }
                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                } else {
                    if (getViewLifecycleOwner() != null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private void fetchDropdownData(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
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

                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }
                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                            );
                        }
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("giftingName"));
                        }

                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }
                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                            );
                        }
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("giftingName"));
                        }

                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }
                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }
            }
        });
    }

    private void fetchDropdownDataSoulmate(String url, Spinner dropdown) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getViewLifecycleOwner() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            items.add(jsonArray.getJSONObject(i).getString("giftingName"));
                        }

                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                            });
                        }
                    } catch (Exception e) {
                        if (getViewLifecycleOwner() != null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }
            }
        });
    }

//    private void openCamera() {
//        try {
//            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
//            } else {
//                Toast.makeText(getActivity(), "No camera app found!", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            Toast.makeText(getActivity(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                openCamera();
//            } else {
//                // Permission denied
//                Toast.makeText(getActivity(), "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            if (photo != null) {
//                capturedImages.add(photo);
//                photosAdapter.notifyDataSetChanged();
//            }
//        }
//    }

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
                    // Decode the URI to a Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);

                    // Add the bitmap to the capturedImages list
                    capturedImages.add(bitmap);

                    // Notify the adapter of data change
                    photosAdapter.notifyDataSetChanged();

                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void submitData() {
        String category = categoryDropdown.getSelectedItem().toString();
        String carat = caratDropdown.getSelectedItem().toString();
        String subCategory = subcategoryDropdown.getSelectedItem().toString();
        String gifting = giftingDropdown.getSelectedItem().toString();
        String occasion = occasionDropdown.getSelectedItem().toString();

        String weight = weightInput.getText().toString().trim();
        String wattage = wattageInput.getText().toString().trim();
        String solmate = soulmateDropdown.getSelectedItem().toString();
        String categoryCode = categoryMap.get(category);
        String subCategoryCode = subcategoryMap.get(subCategory);

        // Validate input fields
        if (weight.isEmpty() || wattage.isEmpty() || solmate.isEmpty() || gifting.isEmpty() || occasion.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoryCode == null || subCategoryCode == null) {
            Toast.makeText(requireContext(), "Error in category and subcategory", Toast.LENGTH_SHORT).show();
            return;
        }
        if (capturedImages.size() < MIN_PHOTOS) {
            Toast.makeText(requireContext(), "Please capture at least " + MIN_PHOTOS + " photos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the token and identity from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        String identity = sharedPreferences.getString("identity", null);

        if (token == null || identity == null) {
            Log.d("MyFragment", "No token or identity found!");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("karat", carat)
                .addFormDataPart("gifting", gifting)
                .addFormDataPart("occasion", occasion)
                .addFormDataPart("weight", weight)
                .addFormDataPart("wastage", wattage)
                .addFormDataPart("soulmate", solmate);

        // Add compressed images to the request
        for (int i = 0; i < capturedImages.size(); i++) {
            Bitmap bitmap = capturedImages.get(i);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            multipartBuilder.addFormDataPart(
                    "images", "image_" + i + ".webp",
                    RequestBody.create(byteArray, MediaType.parse("image/webp"))
            );
        }

        MultipartBody requestBody = multipartBuilder.build();

        // Build the request
        Request request = new Request.Builder()
                .url(BACKEND_SUBMIT_URL + "?category=" + categoryCode + "&subCategory=" + subCategoryCode + "&wholeseller="+identity)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Send the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to submit data", Toast.LENGTH_SHORT).show();
                    Log.e("MyFragment", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Data submitted successfully: " + response.message(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Submission failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



}
