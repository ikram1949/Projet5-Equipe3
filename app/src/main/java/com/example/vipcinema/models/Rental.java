package com.example.vipcinema.models;

import java.util.Date;

public class Rental {
    private int id;
    private int filmId;
    private int userId;
    private Date rentedAt;

    public Rental(int id, int filmId, int userId, Date rentedAt) {
        this.id = id;
        this.filmId = filmId;
        this.userId = userId;
        this.rentedAt = rentedAt;
    }

    public int getId() { return id; }
    public int getFilmId() { return filmId; }
    public int getUserId() { return userId; }
    public Date getRentedAt() { return rentedAt; }

}