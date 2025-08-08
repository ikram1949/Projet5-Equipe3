package com.example.vipcinema.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.vipcinema.ApiConfig;
import com.example.vipcinema.R;
import com.example.vipcinema.adapters.FilmAdapter;
import com.example.vipcinema.models.Film;
import com.example.vipcinema.utils.TokenManager;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FilmAdapter filmAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        recyclerView = findViewById(R.id.filmsRecyclerView);
        SearchView searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        filmAdapter = new FilmAdapter(this, false);
        recyclerView.setAdapter(filmAdapter);

        findViewById(R.id.menuIcon).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        View headerView = navigationView.getHeaderView(0);
        ImageView drawerProfileImage = headerView.findViewById(R.id.drawerProfileImage);
        TextView drawerUserName = headerView.findViewById(R.id.drawerUserName);

        SharedPreferences prefs = getSharedPreferences("vip_profile", MODE_PRIVATE);
        String profileUri = prefs.getString("profileImageUri", null);
        if (profileUri != null) {
            Glide.with(this).load(Uri.parse(profileUri)).into(drawerProfileImage);
        } else {
            drawerProfileImage.setImageResource(R.drawable.ic_profile);
        }

        String token = TokenManager.getToken(this);
        String url = ApiConfig.BASE_URL + "/profile";

        JsonObjectRequest profileRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        drawerUserName.setText(name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur chargement profil", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(profileRequest);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_movies) {
                startActivity(new Intent(this, MyMoviesActivity.class));
            } else if (id == R.id.nav_all_movies) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                TokenManager.clearToken(this);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filmAdapter.filter(newText);
                return true;
            }
        });

        fetchFilms();
    }

    private void fetchFilms() {
        String url = "http://10.0.2.2:5000/VIPcinema/api/films";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Film> parsed = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            int id = o.getInt("id");
                            String title = o.optString("title", "Film inconnu");
                            String genre = o.optString("genre", "");
                            String releaseDate = o.optString("release_date", "");
                            String overview = o.optString("overview", "");
                            String trailerUrl = o.optString("trailer_url", "");
                            String posterPath = o.optString("poster_path", "");

                            parsed.add(new Film(id, title, genre, releaseDate, overview, trailerUrl, posterPath));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    filmAdapter.setFilms(parsed);
                },
                error -> Toast.makeText(this, "Erreur réseau : " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}
