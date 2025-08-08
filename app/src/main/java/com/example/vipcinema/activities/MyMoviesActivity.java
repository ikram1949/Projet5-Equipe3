package com.example.vipcinema.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import com.example.vipcinema.adapters.FilmAdapter;
import com.example.vipcinema.models.Film;
import com.example.vipcinema.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMoviesActivity extends AppCompatActivity {

    private RecyclerView myMoviesRecyclerView;
    private TextView emptyTextView;
    private FilmAdapter filmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_movies);

        myMoviesRecyclerView = findViewById(R.id.myMoviesRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);

        myMoviesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        filmAdapter = new FilmAdapter(this, true); // true = déjà loué

        myMoviesRecyclerView.setAdapter(filmAdapter);

        fetchRentedFilms();
    }

    private void fetchRentedFilms() {
        String url = ApiConfig.BASE_URL + "/rented-movies";
        String token = TokenManager.getToken(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Film> rentedFilms = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            int filmId = obj.getInt("film_id");
                            String posterPath = obj.optString("poster_path", "");
                            String rentalDate = obj.optString("rental_date", "");

                            rentedFilms.add(new Film(
                                    filmId,
                                    "",           // Genre pas récupéré
                                    rentalDate,   // Date de location (peut afficher autre chose si besoin)
                                    "",           // Overview vide
                                    "",           // Trailer vide
                                    posterPath    // ✅ Correct ici
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (rentedFilms.isEmpty()) {
                        showEmptyMessage();
                    } else {
                        filmAdapter.setFilms(rentedFilms);
                        emptyTextView.setVisibility(View.GONE);
                        myMoviesRecyclerView.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyMessage();
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

    private void showEmptyMessage() {
        emptyTextView.setText("Aucun film loué.");
        emptyTextView.setVisibility(View.VISIBLE);
        myMoviesRecyclerView.setVisibility(View.GONE);
    }
}
