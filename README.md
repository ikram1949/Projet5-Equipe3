##  Fonctionnalités

-  Authentification par e-mail (connexion, inscription)
-  Liste de films à jour depuis l’API TMDb
-  Détails complets sur chaque film (affiche, date, synopsis, bande-annonce)
-  Bande-annonce intégrée dans un WebView (lecture YouTube)
-  Espace "Mes films" pour suivre les films loués
-  Profil utilisateur modifiable (avec image personnalisable locale)
-  Interface moderne avec Material Design

---

##  Technologies utilisées

- **Android Studio (Java)**
- **SQLite** (base de données locale)
- **Node.js + Express** (backend local)
- **TMDb API** pour les données de films
- **Volley** pour les appels API
- **Glide** pour le chargement des images
- **SharedPreferences** pour la persistance locale (photo de profil)

---

##  Prérequis

- Android Studio Bumblebee ou +
- Un émulateur ou appareil Android 8.0+
- Node.js v18+
- Une clé API gratuite de TMDb (https://www.themoviedb.org/)

---

##  Installation & Lancement

###  Backend (Express + SQLite)

```bash
# 1. Cloner le repo
git clone https://github.com/ton_profil/VIPCinema.git
cd VIPCinema/backend

# 2. Installer les dépendances
npm install

# 3. Créer ou mettre à jour la base de données
node init_db.js          # crée les tables
node insert_tmdb.js    # remplit avec des films populaires

# 4. Démarrer le backend
node server.js
