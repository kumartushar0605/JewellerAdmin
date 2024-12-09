package com.example.gehnamall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Content extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Find buttons by their IDs
        Button btnAddBanner = findViewById(R.id.btnAddBanner);
        Button btnAddTestimonial = findViewById(R.id.btnAddTestimonial);
        Button btnUpdatePrice = findViewById(R.id.btnUpdatePrice);

        // Set click listeners for the buttons
        btnAddBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to BannerActivity
                Intent intent = new Intent(Content.this, BannerActivity.class);
                startActivity(intent);
            }
        });

        btnAddTestimonial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to TestimonialActivity
                Intent intent = new Intent(Content.this, TestimonialActivity.class);
                startActivity(intent);
            }
        });

        btnUpdatePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to UpdatePriceActivity
                Intent intent = new Intent(Content.this, UpdatePriceActivity.class);
                startActivity(intent);
            }
        });
    }
}
