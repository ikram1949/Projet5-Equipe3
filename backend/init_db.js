const sqlite3 = require('sqlite3').verbose();

const db = new sqlite3.Database('./database.db');

db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    email TEXT UNIQUE,
    password TEXT,
    profile_picture TEXT
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS films (
    id INTEGER PRIMARY KEY,
    title TEXT,
    overview TEXT,
    release_date TEXT,
    poster_path TEXT,
    backdrop_path TEXT,
    original_language TEXT,
    vote_average REAL,
    vote_count INTEGER,
    popularity REAL,
    trailer_url TEXT
  )`);


  db.run(`CREATE TABLE IF NOT EXISTS rentals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    film_id INTEGER,
    rental_date TEXT,
    return_date TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (film_id) REFERENCES films(id)
  )`);

  console.log("✔ Toutes les tables ont été créées avec succès.");
});

db.close();
