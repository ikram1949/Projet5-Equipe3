package com.example.vipcinema.models;

public class Film {
    private int id;
    private String title;
    private String genre;
    private String releaseDate;   // ex. "2025-03-26" or rental date
    private String overview;      // description
    private String trailerUrl;    // YouTube or other link
    private String posterPath;    // image URL or path

    // Empty constructor for JSON parsing
    public Film() { }

    // Full constructor (7 parameters)
    public Film(int id,
                String title,
                String genre,
                String releaseDate,
                String overview,
                String trailerUrl,
                String posterPath) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.trailerUrl = trailerUrl;
        this.posterPath = posterPath;
    }

    // Simplified constructor without trailerUrl
    public Film(int id,
                String title,
                String genre,
                String releaseDate,
                String overview,
                String posterPath) {
        this(id, title, genre, releaseDate, overview, "", posterPath);
    }

    // Getters and Setters

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }
    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getPosterPath() {
        return posterPath;
    }
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
