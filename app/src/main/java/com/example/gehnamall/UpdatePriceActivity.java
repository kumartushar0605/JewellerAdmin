package com.example.gehnamall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdatePriceActivity extends AppCompatActivity {
    private Spinner dropdownMetal;
    private Spinner dropdownKarat;
    private HashMap<String, JSONObject> metalDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_price);
        dropdownMetal = findViewById(R.id.dropdownPrice);
        dropdownKarat = findViewById(R.id.dropdownKarat);
        Button btnSubmitPrice = findViewById(R.id.btnSubmitPrice);
        btnSubmitPrice.setOnClickListener(view -> submitData());
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

    private void submitData() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        if (token == null) {
            Toast.makeText(this, "Authorization token not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedMetal = dropdownMetal.getSelectedItem() != null ? dropdownMetal.getSelectedItem().toString() : "";
        String selectedKarat = dropdownKarat.getSelectedItem() != null ? dropdownKarat.getSelectedItem().toString() : "";
        String enteredPrice = ((android.widget.EditText) findViewById(R.id.inputPrice)).getText().toString().trim();

        if (selectedMetal.isEmpty() || selectedKarat.isEmpty() || enteredPrice.isEmpty() ) {
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Upload Banner


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
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdatePriceActivity.this, android.R.layout.simple_spinner_item, karats);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dropdownKarat.setAdapter(adapter);

                        // Set a listener for dropdownKarat
                        dropdownKarat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                String selectedKarat = dropdownKarat.getSelectedItem().toString() + ":";
                                try {
                                    // Get the price for the selected karat
                                    String price = metalData.getString(selectedKarat);
                                    // Update the EditText with the price
                                    ((android.widget.EditText) findViewById(R.id.inputPrice)).setText(price);
                                } catch (JSONException e) {
                                    Log.e("KaratSelection", "Error fetching price", e);
                                }
                            }

                            @Override
                            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                                // Clear the EditText if no karat is selected
                                ((android.widget.EditText) findViewById(R.id.inputPrice)).setText("");
                            }
                        });

                    } catch (Exception e) {
                        Log.e("DropdownListener", "Error parsing karats", e);
                    }
                } else {
                    dropdownKarat.setVisibility(View.GONE); // Hide dropdownKarat if no valid metal is selected
                    ((android.widget.EditText) findViewById(R.id.inputPrice)).setText(""); // Clear the EditText
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                dropdownKarat.setVisibility(View.GONE); // Hide dropdownKarat when no metal is selected
                ((android.widget.EditText) findViewById(R.id.inputPrice)).setText(""); // Clear the EditText
            }
        });
    }
}