package com.example.vipcinema.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.bumptech.glide.Glide;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import com.example.vipcinema.utils.TokenManager;
import com.example.vipcinema.utils.VolleyMultipartRequest;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    EditText editName, editEmail, editPassword;
    Button saveProfileBtn;
    ImageView profileImage;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        profileImage = findViewById(R.id.profileImage);

        profileImage.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, 101);
        });

        saveProfileBtn.setOnClickListener(v -> {
            updateProfile();
            uploadProfilePicture();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri); // preview immédiat
        }
    }

    private void updateProfile() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String token = TokenManager.getToken(this);

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nom et email sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            if (!password.isEmpty()) body.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, ApiConfig.BASE_URL + "/update-profile", body,
                response -> Toast.makeText(this, "Informations mises à jour", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Erreur serveur", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void uploadProfilePicture() {
        if (selectedImageUri == null) return;

        String token = TokenManager.getToken(this);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                ApiConfig.BASE_URL + "/upload-profile-picture",
                response -> Toast.makeText(this, "Image envoyée", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Erreur image", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> map = new HashMap<>();
                map.put("profilePicture", new DataPart("profile.jpg", getFileDataFromUri(selectedImageUri)));
                return map;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private byte[] getFileDataFromUri(Uri uri) {
        try (InputStream iStream = getContentResolver().openInputStream(uri)) {
            return Objects.requireNonNull(iStream).readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
