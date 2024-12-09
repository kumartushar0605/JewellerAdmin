package com.example.gehnamall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Arrays;
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

public class HomeActivity2 extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {
    BottomNavigationView bottomNavigationView;


    sngold_fragment sngold_fragment =new sngold_fragment();
    sngoldProfile sngoldProfile = new sngoldProfile();
    yourActivity_sngold yourActivity_sngold = new yourActivity_sngold();
    Content_sngold content_sngold = new Content_sngold();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);
        bottomNavigationView = findViewById(R.id.bottomnevigatiomuserhome3);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.upload_products);
        getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutuserhome, sngold_fragment).commit();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.upload_products) {
            getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutuserhome, sngold_fragment).commit();
        } else if (item.getItemId() == R.id.menu_manage_content) {
            getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutuserhome, content_sngold).commit();
        } else if (item.getItemId() == R.id.menu_products) {
            getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutuserhome, yourActivity_sngold).commit();
        } else if (item.getItemId() == R.id.menu_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutuserhome, sngoldProfile).commit();
        }
        return true;
    }
}
