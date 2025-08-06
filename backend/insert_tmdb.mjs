import fetch from 'node-fetch';
import sqlite3 from 'sqlite3';

const API_KEY = '17f97fb3319b9e6f62ffe6a5eb45087a'; // Clé TMDb
const db = new sqlite3.Database('./database.db');

async function fetchTrailerUrl(movieId) {
  try {
    const res = await fetch(`https://api.themoviedb.org/3/movie/${movieId}/videos?api_key=${API_KEY}`);
    const data = await res.json();

    if (data.results && data.results.length > 0) {
      const trailer = data.results.find(v => v.type === "Trailer" && v.site === "YouTube");
      if (trailer) {
        return `https://www.youtube.com/embed/${trailer.key}`;
      }
    }
    return ""; // No trailer found
  } catch (error) {
    console.error("Erreur trailer pour movieId", movieId, error.message);
    return "";
  }
}

async function fetchAndInsertMovies() {
  try {
    const res = await fetch(`https://api.themoviedb.org/3/movie/popular?api_key=${API_KEY}&language=fr-FR&page=1`);
    const data = await res.json();

    const movies = data.results;
    console.log("Nombre de films récupérés :", movies.length);

    db.serialize(async () => {
      const stmt = db.prepare(`REPLACE INTO films
        (id, title, overview, release_date, poster_path, backdrop_path, original_language, vote_average, vote_count, popularity, trailer_url)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);

      for (const movie of movies) {
        const trailerUrl = await fetchTrailerUrl(movie.id);

        stmt.run([
          movie.id,
          movie.title,
          movie.overview,
          movie.release_date,
          movie.poster_path,
          movie.backdrop_path,
          movie.original_language,
          movie.vote_average,
          movie.vote_count,
          movie.popularity,
          trailerUrl
        ]);
      }

      stmt.finalize(() => {
        console.log('✅ Films insérés avec succès dans SQLite avec trailers!');
        db.close();
      });
    });

  } catch (error) {
    console.error("Erreur API TMDb :", error.message);
  }
}

fetchAndInsertMovies();
