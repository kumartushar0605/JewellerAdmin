package com.example.gehnamall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText phoneInput, passwordInput;
    private Button signupButton;
    String token;
    String identity;
    private final String BACKEND_URL = "http://3.110.34.172:8080/auth/login"; // Replace with your API endpoint

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);




        // Set button click listener
        signupButton.setOnClickListener(v -> {
            String email = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                sendSignupData(email, password);
            }
        });
    }

    private void sendSignupData(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        // Create JSON payload
        String jsonPayload = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\""
                + "}";

        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(BACKEND_URL)
                .post(body)
                .build();

        // Make the HTTP request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse the response JSON
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        String message = jsonObject.getString("message");
                        int status = jsonObject.getInt("status");
                        token = jsonObject.getString("token");
                       identity = jsonObject.getString("identity");

                        // Store the token in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("authToken", token);
                        editor.putString("identity",identity);
                        editor.apply();

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Start HomeActivity

                               if(identity.equals("BANSAL")){
                                   Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                   startActivity(intent);
                                   finish(); // Optional: Close the current activity
                                   return;
                               } else if(identity.equals("SLGOLD"))
                               {
                                   Intent intent = new Intent(MainActivity.this, HomeActivity2.class);
                                   startActivity(intent);
                                   finish();

                               }


                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            Log.e("MainActivity", "Error: " + e.getMessage());
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Sign Up Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
