# AutoReport System — Frontend

Interface React (Vite + Tailwind) pour la plateforme d'automatisation de génération de rapports
industriels, conçue pour consommer l'API `reporting-backend` (Spring Boot).

## Stack technique
- React 18 + Vite
- React Router (navigation, routes protégées par rôle)
- Tailwind CSS (design system industriel : graphite, amber, teal)
- Recharts (production journalière, production par ligne)
- Axios (client API + JWT automatique)
- lucide-react (icônes)

## Fonctionnalités couvertes (cahier des charges §5)
- 5.1 Authentification (JWT) et gestion des rôles ADMIN / MANAGER / USER, navigation adaptée au rôle
- 5.2 Import de fichiers Excel (.xlsx) avec retour d'erreur (ex : colonne manquante)
- 5.3 / 5.5 Tableau de bord : KPI (production totale, défauts, taux de qualité/défaut), courbe de
  production journalière, production par ligne
- 5.4 Génération de rapports sur une période, téléchargement PDF / Excel
- 5.7 Historique des rapports avec recherche
- 5.8 Planification des envois automatiques (fréquence, destinataires) — réservé à l'administrateur
- Gestion des utilisateurs (créer, activer/désactiver, changer de rôle, supprimer) — réservé à l'administrateur

## Démarrage

```bash
npm install
cp .env.example .env   # ajuster VITE_API_BASE_URL si besoin
npm run dev
```

L'application démarre sur `http://localhost:5173` et appelle le backend sur
`http://localhost:8080` par défaut (voir `.env`).

Assurez-vous que le backend `reporting-backend` tourne (MySQL/PostgreSQL configuré, `mvn spring-boot:run`)
et que le CORS est ouvert (déjà le cas dans `SecurityConfig`).

Compte administrateur par défaut créé au premier démarrage du backend :
`admin@sews.com` / `Admin@123`

## Build de production

```bash
npm run build
```

Le dossier `dist/` généré peut être servi par n'importe quel serveur statique (Nginx, etc.)
ou copié dans `src/main/resources/static` du backend pour un déploiement en un seul artefact.

## Structure

```
src/
  api/          appels HTTP vers chaque contrôleur du backend (auth, reports, dashboard, users, schedules)
  context/      AuthContext (session, token JWT en localStorage)
  components/   Layout (sidebar + navigation par rôle), KpiCard, PageHeader, ProtectedRoute
  pages/        Login, Dashboard, Reports, Users, Schedules, NotFound
```
