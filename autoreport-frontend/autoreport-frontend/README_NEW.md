# AutoReport System - Frontend

Plateforme d'automatisation de génération de rapports industriels pour SEWS CABIND MAROC.

## 🚀 Démarrage Rapide

### Prérequis

- Node.js 16+ et npm
- Backend Spring Boot démarré sur `http://localhost:8080`

### Installation

```bash
npm install
```

### Configuration

Créez un fichier `.env` à la racine :

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Lancement

```bash
# Mode développement
npm run dev

# Build production
npm run build

# Prévisualisation build
npm run preview
```

L'application sera accessible sur `http://localhost:5173`

## 🔐 Comptes de Test

**Administrateur :**
- Email: `admin@sews.com`
- Password: `Admin@123`
- Accès: Toutes les fonctionnalités

**Manager :**
- Email: `manager@sews.com`
- Password: `Manager@123`
- Accès: Dashboard, Rapports, Import Excel

**Employé :**
- Email: `employe@sews.com`
- Password: `Employe@123`
- Accès: Dashboard, Rapports (lecture seule)

## 📋 Fonctionnalités Principales

### Pour tous les utilisateurs

- **Dashboard** - Vue d'ensemble avec KPIs et graphiques
- **Rapports** - Consultation et téléchargement (PDF/Excel)
- **Profil** - Gestion du compte utilisateur

### Pour ADMIN et MANAGER

- **Import Excel** - Upload de données de production
- **Génération** - Création de rapports personnalisés

### Pour ADMIN uniquement

- **Utilisateurs** - Gestion des comptes
- **Planification** - Configuration des envois automatiques
- **Paramètres** - Configuration SMTP et entreprise
- **Audit Log** - Journal d'activité complet

## 📁 Structure du Projet

```
frontend/
├── src/
│   ├── components/     # Composants réutilisables
│   ├── pages/          # Pages de l'application
│   ├── context/        # Contextes React (Auth, Toast)
│   ├── api/            # Appels API
│   ├── assets/         # Images et fichiers statiques
│   ├── App.jsx         # Composant racine
│   ├── main.jsx        # Point d'entrée
│   └── index.css       # Styles globaux
├── public/             # Fichiers publics
└── package.json        # Dépendances
```

## 🎨 Technologies Utilisées

- **React 18** - Framework frontend
- **React Router DOM 6** - Routing
- **Axios** - Requêtes HTTP
- **Tailwind CSS** - Styling
- **Recharts** - Graphiques
- **Lucide React** - Icônes
- **Vite** - Build tool

## 📖 Documentation Complète

Voir [FEATURES.md](./FEATURES.md) pour la liste complète des fonctionnalités et le guide d'utilisation détaillé.

## 🔧 Scripts Disponibles

```bash
npm run dev      # Lancer le serveur de développement
npm run build    # Build pour production
npm run preview  # Prévisualiser le build
```

## 🌐 Endpoints API Requis

Le frontend s'attend à ce que le backend expose ces endpoints :

### Authentification
- `POST /api/auth/login` - Connexion
- `POST /api/auth/logout` - Déconnexion

### Dashboard
- `GET /api/dashboard/kpis?start=...&end=...` - KPIs et données graphiques

### Rapports
- `GET /api/reports?q=...` - Liste des rapports
- `POST /api/reports/generate` - Générer un rapport
- `POST /api/reports/import-excel` - Import Excel
- `GET /api/reports/:id/download?format=pdf|excel` - Télécharger

### Utilisateurs (ADMIN)
- `GET /api/users` - Liste des utilisateurs
- `POST /api/users` - Créer un utilisateur
- `PUT /api/users/:id` - Mettre à jour
- `DELETE /api/users/:id` - Supprimer

### Planification (ADMIN)
- `GET /api/schedules` - Liste des planifications
- `POST /api/schedules` - Créer une planification
- `DELETE /api/schedules/:id` - Supprimer

## 🎯 Workflow Complet

1. **Connexion** → Authentification avec rôle
2. **Import Excel** → Upload des données de production
3. **Dashboard** → Visualisation automatique des KPIs
4. **Génération** → Création de rapport pour une période
5. **Consultation** → Filtrage et recherche dans l'historique
6. **Téléchargement** → Export PDF ou Excel
7. **Audit** → Traçabilité complète des actions

## 🔒 Sécurité

- Protection des routes par rôle
- Token JWT dans les headers
- Validation côté frontend
- Gestion des sessions
- Messages d'erreur sécurisés

## 🐛 Débogage

### Le backend ne répond pas

Vérifiez que :
1. Le backend Spring Boot est démarré
2. L'URL dans `.env` est correcte
3. CORS est configuré sur le backend
4. Le port 8080 est accessible

### Problèmes de connexion

- Vérifiez les comptes de test dans `Users.jsx`
- Consultez la console navigateur (F12)
- Vérifiez les logs du backend

## 📞 Support

Pour toute question ou problème :
- Consultez [FEATURES.md](./FEATURES.md)
- Vérifiez les logs navigateur (Console F12)
- Vérifiez les logs backend

---

**Développé pour SEWS CABIND MAROC - Stage 2024** 🏭
