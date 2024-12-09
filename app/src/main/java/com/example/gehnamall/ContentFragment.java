package com.example.gehnamall;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_content, container, false);

        // Find buttons by their IDs
        Button btnAddBanner = view.findViewById(R.id.btnAddBanner);
        Button btnAddTestimonial = view.findViewById(R.id.btnAddTestimonial);
        Button btnUpdatePrice = view.findViewById(R.id.btnUpdatePrice);

        // Set click listeners for the buttons
        btnAddBanner.setOnClickListener(v -> {
            // Navigate to BannerActivity
            Intent intent = new Intent(getActivity(), BannerActivity.class);
            startActivity(intent);
        });

        btnAddTestimonial.setOnClickListener(v -> {
            // Navigate to TestimonialActivity
            Intent intent = new Intent(getActivity(), TestimonialActivity.class);
            startActivity(intent);
        });

        btnUpdatePrice.setOnClickListener(v -> {
            // Navigate to UpdatePriceActivity
            Intent intent = new Intent(getActivity(), UpdatePriceActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
