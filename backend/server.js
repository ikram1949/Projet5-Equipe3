const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const dotenv = require('dotenv');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// Database SQLite
const db = new sqlite3.Database('./database.db');

// Static files
app.use('/VIPCinema/uploads', express.static(path.join(__dirname, 'uploads')));

// JWT Middleware
const verifyToken = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) return res.status(401).json({ message: 'Token is missing.' });
  const token = authHeader.split(' ')[1];
  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) return res.status(401).json({ message: 'Invalid token.' });
    req.user = decoded;
    next();
  });
};

// Multer setup
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, 'uploads');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir);
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `user_${req.user.id}${ext}`);
  }
});
const upload = multer({ storage });

// API Router
const apiRouter = express.Router();

// === ROUTES ===

// 🔹 Récupérer tous les films
apiRouter.get('/films', (req, res) => {
  db.all('SELECT * FROM films ORDER BY popularity DESC LIMIT 20', (err, rows) => {
    if (err) return res.status(500).json({ message: 'Erreur récupération films.' });
    res.json(rows);
  });
});

// 🔹 Inscription
apiRouter.post('/register', (req, res) => {
  const { name, email, password } = req.body;
  db.get('SELECT * FROM users WHERE email = ?', [email], (err, user) => {
    if (err) return res.status(500).json({ message: 'Erreur recherche email.' });
    if (user) return res.status(409).json({ message: 'Email déjà utilisé.' });

    bcrypt.hash(password, 10, (err, hash) => {
      if (err) return res.status(500).json({ message: 'Erreur hash mot de passe.' });
      db.run('INSERT INTO users (name, email, password) VALUES (?, ?, ?)', [name, email, hash], err => {
        if (err) return res.status(500).json({ message: 'Erreur enregistrement utilisateur.' });
        res.status(201).json({ message: 'Compte créé avec succès.' });
      });
    });
  });
});

// 🔹 Connexion
apiRouter.post('/login', (req, res) => {
  const { email, password } = req.body;
  db.get('SELECT * FROM users WHERE email = ?', [email], (err, user) => {
    if (err) return res.status(500).json({ message: 'Erreur serveur.' });
    if (!user) return res.status(404).json({ message: 'Utilisateur non trouvé.' });

    bcrypt.compare(password, user.password, (err, isMatch) => {
      if (err || !isMatch) return res.status(401).json({ message: 'Mot de passe incorrect.' });

      const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, { expiresIn: '1h' });
      res.json({ message: 'Connexion réussie.', token });
    });
  });
});

// 🔹 Louer un film
apiRouter.post('/rent', verifyToken, (req, res) => {
  const { filmId } = req.body;
  if (!filmId) return res.status(400).json({ message: 'ID du film requis.' });

  db.get('SELECT COUNT(*) AS count FROM rentals WHERE user_id = ? AND return_date IS NULL', [req.user.id], (err, result) => {
    if (err) return res.status(500).json({ message: 'Erreur serveur.' });

    if (result.count >= 5) return res.status(400).json({ message: 'Maximum 5 films loués.' });

    db.get('SELECT * FROM rentals WHERE user_id = ? AND film_id = ? AND return_date IS NULL', [req.user.id, filmId], (err, row) => {
      if (err) return res.status(500).json({ message: 'Erreur vérification location.' });

      if (row) return res.status(400).json({ message: 'Film déjà loué.' });

      db.run('INSERT INTO rentals (user_id, film_id, rental_date) VALUES (?, ?, datetime("now"))', [req.user.id, filmId], err => {
        if (err) return res.status(500).json({ message: 'Erreur location film.' });
        res.json({ message: 'Film loué avec succès.' });
      });
    });
  });
});

// 🔹 Retourner un film
apiRouter.post('/return', verifyToken, (req, res) => {
  const { filmId } = req.body;
  db.run('UPDATE rentals SET return_date = datetime("now") WHERE user_id = ? AND film_id = ? AND return_date IS NULL', [req.user.id, filmId], function(err) {
    if (err) return res.status(500).json({ message: 'Erreur retour film.' });
    if (this.changes === 0) return res.status(404).json({ message: 'Aucune location active trouvée.' });
    res.json({ message: 'Film retourné avec succès.' });
  });
});

// 🔹 Voir son profil
apiRouter.get('/profile', verifyToken, (req, res) => {
  db.get('SELECT name, email, profile_picture FROM users WHERE id = ?', [req.user.id], (err, row) => {
    if (err) return res.status(500).json({ message: 'Erreur serveur.' });
    if (!row) return res.status(404).json({ message: 'Utilisateur introuvable.' });
    res.json(row);
  });
});

// 🔹 Voir films loués ( CORRIGÉ pour renvoyer l'affiche aussi)
apiRouter.get('/rented-movies', verifyToken, (req, res) => {
  db.all(
    `SELECT f.id AS film_id, f.title, f.poster_path, r.rental_date
     FROM rentals r
     JOIN films f ON f.id = r.film_id
     WHERE r.user_id = ? AND r.return_date IS NULL
     ORDER BY r.rental_date DESC`,
    [req.user.id],
    (err, rows) => {
      if (err) return res.status(500).json({ message: 'Erreur récupération films loués.' });
      res.json(rows);
    }
  );
});

// 🔹 Upload image profil
apiRouter.post('/upload-profile-picture', verifyToken, upload.single('profilePicture'), (req, res) => {
  const imagePath = `/uploads/${req.file.filename}`;
  db.run('UPDATE users SET profile_picture = ? WHERE id = ?', [imagePath, req.user.id], err => {
    if (err) return res.status(500).json({ message: 'Erreur upload photo.' });
    res.json({ message: 'Photo de profil mise à jour.', path: imagePath });
  });
});

// 🔹 Modifier profil
apiRouter.put('/update-profile', verifyToken, (req, res) => {
  const { name, email, password } = req.body;
  if (!name || !email) return res.status(400).json({ message: 'Nom et email requis.' });

  const updateUser = () => {
    db.run('UPDATE users SET name = ?, email = ? WHERE id = ?', [name, email, req.user.id], err => {
      if (err) return res.status(500).json({ message: 'Erreur mise à jour.' });
      res.json({ message: 'Profil mis à jour.' });
    });
  };

  if (password) {
    bcrypt.hash(password, 10, (err, hash) => {
      if (err) return res.status(500).json({ message: 'Erreur hash mot de passe.' });
      db.run('UPDATE users SET password = ? WHERE id = ?', [hash, req.user.id], err => {
        if (err) return res.status(500).json({ message: 'Erreur mise à jour mot de passe.' });
        updateUser();
      });
    });
  } else {
    updateUser();
  }
});

// Test API
apiRouter.get('/', (req, res) => {
  res.send('API is working!');
});

// Mount API
app.use('/VIPCinema/api', apiRouter);

// Static files
app.use('/VIPCinema', express.static(path.join(__dirname)));

// Home page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

// Start server
app.listen(5000, '0.0.0.0', () => {
  console.log('Server running on 0.0.0.0:5000');
});
