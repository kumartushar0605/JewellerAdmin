package com.example.gehnamall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import static android.app.Activity.RESULT_OK;


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



public class ProfileeFragment extends Fragment {
    private static final int REQUEST_CODE_BANNER = 1;

    private RecyclerView photosRecyclerView;
    private ImageView cameraIcon;
    private List<Bitmap> capturedImages = new ArrayList<>();
    private PhotosAdapter photosAdapter;  // Custom adapter for RecyclerView
    private EditText editTextName, editTextShopName, editTextShopContact, editTextShopAddress, editTextGSTIN,
            editTextMOQ, editTextDescription, editTextReturnPolicy, editTextOrderNumber1, editTextOrderNumber2,
            editTextOrderNumber3, editTextBankName, editTextBankAccountNumber, editTextIFSCCode, editTextUPIID;
    private Button submitButton, logoutButton;

    private static final String BACKEND_SUBMIT_URL = "https://your.backend.api/submit";  // Replace with actual URL



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_profilee, container, false);

        // Initialize views from the XML
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);
        cameraIcon = view.findViewById(R.id.cameraIcon);
        editTextName = view.findViewById(R.id.editTextName);
        editTextShopName = view.findViewById(R.id.editTextShopName);
        editTextShopContact = view.findViewById(R.id.editTextShopContact);
        editTextShopAddress = view.findViewById(R.id.editTextShopAddress);
        editTextGSTIN = view.findViewById(R.id.editTextGSTIN);
        editTextMOQ = view.findViewById(R.id.editTextMOQ);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextReturnPolicy = view.findViewById(R.id.editTextReturnPolicy);
        editTextOrderNumber1 = view.findViewById(R.id.editTextOrderNumber1);
        editTextOrderNumber2 = view.findViewById(R.id.editTextOrderNumber2);
        editTextOrderNumber3 = view.findViewById(R.id.editTextOrderNumber3);
        editTextBankName = view.findViewById(R.id.editTextBankName);
        editTextBankAccountNumber = view.findViewById(R.id.editTextBankAccountNumber);
        editTextIFSCCode = view.findViewById(R.id.editTextIFSCCode);
        editTextUPIID = view.findViewById(R.id.editTextUPIID);
        submitButton = view.findViewById(R.id.submitButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Initialize RecyclerView with custom adapter
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false));
        photosAdapter = new PhotosAdapter(capturedImages);  // Assuming PhotosAdapter handles image display
        photosRecyclerView.setAdapter(photosAdapter);

        // Set up camera icon click listener to open gallery
        cameraIcon.setOnClickListener(v -> openGallery(REQUEST_CODE_BANNER));

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> submitData());

        // Set up logout button click listener
        logoutButton.setOnClickListener(v -> logout());

        return view;
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
        // Collect data from UI elements
        String name = editTextName.getText().toString().trim();
        String shopName = editTextShopName.getText().toString().trim();
        String shopContact = editTextShopContact.getText().toString().trim();
        String shopAddress = editTextShopAddress.getText().toString().trim();
        String gstin = editTextGSTIN.getText().toString().trim();
        String moq = editTextMOQ.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String returnPolicy = editTextReturnPolicy.getText().toString().trim();
        String orderNumber1 = editTextOrderNumber1.getText().toString().trim();
        String orderNumber2 = editTextOrderNumber2.getText().toString().trim();
        String orderNumber3 = editTextOrderNumber3.getText().toString().trim();
        String bankName = editTextBankName.getText().toString().trim();
        String bankAccountNumber = editTextBankAccountNumber.getText().toString().trim();
        String ifscCode = editTextIFSCCode.getText().toString().trim();
        String upiId = editTextUPIID.getText().toString().trim();




        // Validate required fields
        if (name.isEmpty() || shopName.isEmpty() || shopContact.isEmpty() || shopAddress.isEmpty() || gstin.isEmpty() ||
                moq.isEmpty() || description.isEmpty() || returnPolicy.isEmpty() || orderNumber1.isEmpty() ||
                orderNumber2.isEmpty() || orderNumber3.isEmpty() || bankName.isEmpty() || bankAccountNumber.isEmpty() ||
                ifscCode.isEmpty() || upiId.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Please upload at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare multipart request
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("shopName", shopName)
                .addFormDataPart("shopContact", shopContact)
                .addFormDataPart("shopAddress", shopAddress)
                .addFormDataPart("gstin", gstin)
                .addFormDataPart("moq", moq)
                .addFormDataPart("description", description)
                .addFormDataPart("returnPolicy", returnPolicy)
                .addFormDataPart("orderNumber1", orderNumber1)
                .addFormDataPart("orderNumber2", orderNumber2)
                .addFormDataPart("orderNumber3", orderNumber3)
                .addFormDataPart("bankName", bankName)
                .addFormDataPart("bankAccountNumber", bankAccountNumber)
                .addFormDataPart("ifscCode", ifscCode)
                .addFormDataPart("upiId", upiId);

        // Add images to the multipart request
        for (int i = 0; i < capturedImages.size(); i++) {
            Bitmap bitmap = capturedImages.get(i);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            multipartBuilder.addFormDataPart("images", "image_" + i + ".webp",
                    RequestBody.create(byteArray, MediaType.parse("image/webp")));
        }

        MultipartBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(BACKEND_SUBMIT_URL)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .build();

        // Send request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Submission failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void logout() {

            // Clear SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Redirect to MainActivity
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            requireActivity().finish(); // Close current activity



    }
}