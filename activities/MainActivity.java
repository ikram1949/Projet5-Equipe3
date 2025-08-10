package com.example.vipcinema.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import com.example.vipcinema.utils.TokenManager; // 🛠️ ajout IMPORT ici
import org.json.*;

public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            String url = ApiConfig.BASE_URL + "/login";

            JSONObject postData = new JSONObject();
            try {
                postData.put("email", email);
                postData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> {
                        try {
                            String token = response.getString("token");

                            // 🛠️ Correction IMPORTANTE : sauvegarder le token !!
                            TokenManager.saveToken(MainActivity.this, token);

                            Toast.makeText(MainActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        String errorMsg = "Échec de connexion";
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            errorMsg = "Mot de passe incorrect";
                        } else if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            errorMsg = "Utilisateur non trouvé";
                        }
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    });

            Volley.newRequestQueue(this).add(request);
        });

        registerLink.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );
    }
}
