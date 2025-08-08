package com.example.vipcinema.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.vipcinema.R;
import com.example.vipcinema.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FilmDetailActivity extends AppCompatActivity {

    private static final String API_BASE = "http://10.0.2.2:5000/VIPCinema/api";
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private boolean isRented = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_detail);

        // UI Elements
        ImageView posterImage = findViewById(R.id.posterImage);
        TextView genreTv = findViewById(R.id.genreTv);
        TextView yearTv = findViewById(R.id.yearTv);
        TextView descTv = findViewById(R.id.descTv);
        TextView trailerTv = findViewById(R.id.trailerTv);
        ImageView playBtn = findViewById(R.id.playBtn);
        MaterialButton actionBtn = findViewById(R.id.actionBtn);
        WebView trailerWebView = findViewById(R.id.trailerWebView);

        // Intent Extras
        Intent i = getIntent();
        int filmId = i.getIntExtra("id", -1);
        String genre = i.getStringExtra("genre");
        String year = i.getStringExtra("year");
        String desc = i.getStringExtra("desc");
        String trailerUrl = i.getStringExtra("trailer");
        Log.d("TrailerURL", "Received: " + trailerUrl);

        String posterPath = i.getStringExtra("poster");
        boolean isRentedIntent = i.getBooleanExtra("isRented", false);
        this.isRented = isRentedIntent;

        // Poster Image
        Glide.with(this)
                .load(TMDB_IMAGE_BASE + posterPath)
                .placeholder(R.drawable.placeholder)
                .into(posterImage);

        genreTv.setText("🎬 " + genre);
        yearTv.setText("📅 " + year);
        descTv.setText(desc);
        trailerTv.setText("Bande‑annonce");

        // Trailer WebView Setup
        trailerWebView.getSettings().setJavaScriptEnabled(true);
        trailerWebView.setWebChromeClient(new WebChromeClient());

        if (trailerUrl != null && !trailerUrl.isEmpty()) {
            String embedUrl = getEmbedUrl(trailerUrl);

            Log.d("TrailerURL", "Original URL: " + trailerUrl);
            Log.d("TrailerURL", "Embed URL: " + embedUrl);

            String iframe = "<html><body style='margin:0;padding:0;'><iframe width='100%' height='100%' src='"
                    + embedUrl + "' frameborder='0' allowfullscreen></iframe></body></html>";

            trailerWebView.loadData(iframe, "text/html", "utf-8");
        }

        // Action Button
        updateActionButton(actionBtn);

        actionBtn.setOnClickListener(v -> rentOrReturnFilm(filmId, actionBtn));
    }

    private void updateActionButton(MaterialButton btn) {
        btn.setText(isRented ? "RETOURNER" : "LOUER");
    }

    private void rentOrReturnFilm(int filmId, MaterialButton actionBtn) {
        String endpoint = isRented ? "/return" : "/rent";
        String url = API_BASE + endpoint;

        JSONObject body = new JSONObject();
        try {
            body.put("filmId", filmId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, isRented ? "Film retourné" : "Film loué", Toast.LENGTH_SHORT).show();
                    isRented = !isRented;
                    updateActionButton(actionBtn);
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erreur réseau : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TokenManager.getToken(FilmDetailActivity.this));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String getEmbedUrl(String originalUrl) {
        if (originalUrl == null) return "";

        if (originalUrl.contains("youtu.be")) {
            return originalUrl.replace("youtu.be/", "www.youtube.com/embed/");
        } else if (originalUrl.contains("watch?v=")) {
            return originalUrl.replace("watch?v=", "embed/");
        } else {
            return originalUrl; // Already good
        }
    }
}
