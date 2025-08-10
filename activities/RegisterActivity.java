package com.example.vipcinema.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import org.json.*;

public class RegisterActivity extends AppCompatActivity {
    EditText nameInput, emailInput, passwordInput;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = ApiConfig.BASE_URL + "/register";
            JSONObject postData = new JSONObject();
            try {
                postData.put("name", name);
                postData.put("email", email);
                postData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> {
                        Toast.makeText(this, "Compte créé !", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> {
                        String errorMsg = "Erreur inconnue";

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseData = new String(error.networkResponse.data);
                            Log.e("RegisterError", "Code: " + statusCode);
                            Log.e("RegisterError", "Data: " + responseData);

                            switch (statusCode) {
                                case 400:
                                    errorMsg = "Champs invalides ou manquants";
                                    break;
                                case 409:
                                    errorMsg = "Email déjà utilisé";
                                    break;
                                case 500:
                                    errorMsg = "Erreur serveur";
                                    break;
                                default:
                                    errorMsg = "Erreur " + statusCode;
                                    break;
                            }
                        } else {
                            Log.e("RegisterError", "Pas de réponse du serveur", error);
                            errorMsg = "Impossible de contacter le serveur";
                        }

                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    });

            Volley.newRequestQueue(this).add(request);
        });
    }
}
