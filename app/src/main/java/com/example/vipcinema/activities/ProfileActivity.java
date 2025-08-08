package com.example.vipcinema.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.bumptech.glide.Glide;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import com.example.vipcinema.utils.TokenManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "vip_profile";
    private static final String KEY_PROFILE_URI = "profileImageUri";

    TextView userName, userEmail, userDate;
    ImageView profileImage;
    Button changeInfoBtn;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Affiche et sauvegarde l’image localement
                    Glide.with(this).load(uri).into(profileImage);
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PROFILE_URI, uri.toString())
                            .apply();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userDate = findViewById(R.id.userDate);
        profileImage = findViewById(R.id.profileImage);
        changeInfoBtn = findViewById(R.id.changeInfoBtn);

        fetchProfile();
        loadSavedProfileImage();

        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        changeInfoBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

    }

    private void loadSavedProfileImage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriString = prefs.getString(KEY_PROFILE_URI, null);
        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            Glide.with(this).load(uri).into(profileImage);
        }
    }

    private void fetchProfile() {
        String url = ApiConfig.BASE_URL + "/profile";
        String token = TokenManager.getToken(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        String email = response.getString("email");
                        String date = response.optString("created_at", "");

                        userName.setText("Nom : " + name);
                        userEmail.setText("Email : " + email);
                        userDate.setText("Date d'inscription : " + (date.isEmpty() ? "Non disponible" : date));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur chargement profil", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
