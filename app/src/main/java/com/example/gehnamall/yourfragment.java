package com.example.gehnamall;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class yourfragment extends Fragment implements ProductAdapter.ProductDeleteListener {
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_your, container, false);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(requireContext()));

        productAdapter = new ProductAdapter(productList, requireContext(), this);
        recyclerViewProducts.setAdapter(productAdapter);

        fetchProducts();
return view;
    }
    private void fetchProducts() {
        OkHttpClient client = new OkHttpClient();

        String url = "http://3.110.34.172:8080/api/getProducts?wholeseller=test";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    try {
                        // Parse the JSON response
                        JSONObject responseObject = new JSONObject(responseBody);
                        JSONArray productsArray = responseObject.getJSONArray("products");
                        JSONArray imagesArray = responseObject.getJSONArray("imageUrl");

                        List<Product> fetchedProducts = new ArrayList<>();

                        for (int i = 0; i < productsArray.length(); i++) {
                            JSONObject productJson = productsArray.getJSONObject(i);

                            Product product = new Product();
                            product.setId(productJson.getString("productId"));
                            product.setCategory(productJson.getString("categoryCode"));
                            product.setSubcategory(productJson.getString("subcategoryCode"));
                            product.setSoulmate(productJson.getString("soulmate"));
                            product.setOccasion(productJson.getString("occasion"));
                            product.setGifting(productJson.getString("gifting"));
                            String productId = productJson.getString("productId");

                            // Parse related images
                            List<String> imageUrls = new ArrayList<>();
                            for (int j = 0; j < imagesArray.length(); j++) {
                                JSONObject imageJson = imagesArray.getJSONObject(j);
                                String imageProductId = imageJson.getString("productId");

                                if (imageProductId.equals(productId)) {
                                    String imageUrl = imageJson.getString("imageUrl");
                                    imageUrls.add(imageUrl);
                                }
                            }
                            product.setImageUrls(imageUrls);

                            fetchedProducts.add(product);
                        }

                        // Update UI on the main thread
                        requireActivity().runOnUiThread(() -> {
                            productList.clear();
                            productList.addAll(fetchedProducts);
                            productAdapter.notifyDataSetChanged();
                        });

                    } catch (JSONException e) {
                        Log.e("YourFragment", "Error parsing JSON", e);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to fetch products", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onDelete(String productId, int position) {
        OkHttpClient client = new OkHttpClient();

        // Replace with your API endpoint for deleting the product
        String url = "http://3.110.34.172:8080/admin/deleteProduct/" + productId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        // Remove the item from the list and update the RecyclerView
                        productList.remove(position);
                        productAdapter.notifyItemRemoved(position);
                        Toast.makeText(requireContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to delete product", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}


