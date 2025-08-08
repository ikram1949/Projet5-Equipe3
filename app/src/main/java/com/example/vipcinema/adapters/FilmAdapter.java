package com.example.vipcinema.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vipcinema.R;
import com.example.vipcinema.activities.FilmDetailActivity;
import com.example.vipcinema.models.Film;

import java.util.ArrayList;
import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    private static final String TMDB_BASE = "https://image.tmdb.org/t/p/w342";
    private final Context context;
    private final List<Film> filmList = new ArrayList<>();
    private final List<Film> filmListFull = new ArrayList<>();

    private final boolean isFromMyMovies; // ✅ NEW

    // ✅ Updated constructor
    public FilmAdapter(Context ctx, boolean isFromMyMovies) {
        this.context = ctx;
        this.isFromMyMovies = isFromMyMovies;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder h, int pos) {
        Film f = filmList.get(pos);

        Glide.with(context)
                .load(TMDB_BASE + f.getPosterPath())
                .placeholder(R.drawable.placeholder)
                .into(h.posterImage);

        // ✅ Pass "isRented" flag to FilmDetailActivity
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FilmDetailActivity.class);
            intent.putExtra("id",       f.getId());
            intent.putExtra("title",    f.getTitle());
            intent.putExtra("genre",    f.getGenre());
            intent.putExtra("year",     f.getReleaseDate());
            intent.putExtra("desc",     f.getOverview());
            intent.putExtra("trailer",  f.getTrailerUrl());
            intent.putExtra("poster",   f.getPosterPath());
            intent.putExtra("isRented", isFromMyMovies); // ✅ KEY LINE
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filmList.size();
    }

    public void setFilms(List<Film> newFilms) {
        filmListFull.clear();
        filmListFull.addAll(newFilms);
        filmList.clear();
        filmList.addAll(newFilms);
        notifyDataSetChanged();
    }

    public void filter(String txt) {
        filmList.clear();
        if (txt == null || txt.trim().isEmpty()) {
            filmList.addAll(filmListFull);
        } else {
            String pat = txt.toLowerCase().trim();
            for (Film f : filmListFull) {
                if (f.getTitle().toLowerCase().contains(pat)) {
                    filmList.add(f);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;

        FilmViewHolder(View v) {
            super(v);
            posterImage = v.findViewById(R.id.posterImage);
        }
    }
}
