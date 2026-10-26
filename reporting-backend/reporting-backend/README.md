# Plateforme d'automatisation de génération de rapports industriels — Backend

Backend Spring Boot construit à partir du cahier des charges (projet SEWS Cabind), couvrant toutes
les fonctionnalités demandées : authentification & rôles, import Excel, calcul des indicateurs,
génération de rapports PDF/Excel, dashboard KPI, envoi automatique par email, historique et
planification des rapports.

## Stack technique (conforme au cahier des charges)

| Besoin | Technologie utilisée |
|---|---|
| Backend | Java 17 + Spring Boot 3.3 |
| Sécurité / rôles | Spring Security + JWT |
| Planification | Spring Scheduler (`@Scheduled`) |
| Base de données | MySQL / PostgreSQL (JPA/Hibernate) |
| Traitement Excel | Apache POI |
| Génération de rapports PDF | JasperReports |
| Génération de rapports Excel | Apache POI |
| Email | Spring Mail |

## Structure du projet

```
src/main/java/com/sews/reporting/
├── config/          → SecurityConfig, DataInitializer
├── security/         → JWT (JwtUtil, JwtAuthFilter, CustomUserDetailsService)
├── entity/           → User, Role, ProductionRecord, ProductionReport, ReportSchedule
├── repository/       → Repositories JPA
├── service/          → AuthService, ExcelImportService, ReportGenerationService, EmailService
├── scheduler/        → DailyReportScheduler (envoi automatique 08:00)
├── controller/        → AuthController, UserController, ReportController,
│                         DashboardController, ScheduleController
├── dto/               → DTOs (login, création utilisateur, résumé de rapport)
└── exception/         → Gestion centralisée des erreurs
```

## Correspondance avec le cahier des charges

- **5.1 Authentification** → `SecurityConfig`, `AuthController`, JWT, rôles ADMIN/MANAGER/USER
- **5.2 Import Excel** → `ExcelImportService` (vérifie format `.xlsx`, colonnes requises
  `Production_Date`, `Line`, `Quantity`, `Defects`, détecte les erreurs de données)
- **5.3 Analyse des données** → `ReportGenerationService` (production totale, par ligne, défauts,
  taux de qualité)
- **5.4 Génération de rapports** → PDF (JasperReports) + Excel (Apache POI),
  téléchargeables via `/api/reports/{id}/download/pdf` et `/download/excel`
- **5.5 Dashboard** → `DashboardController` (`/api/dashboard/kpis`) : production journalière,
  taux de défaut, production par ligne
- **5.6 Envoi automatique** → `DailyReportScheduler` (cron configurable, 08:00 par défaut)
- **5.7 Historique** → `ProductionReportRepository` + `GET /api/reports` (recherche par titre)
- **5.8 Planification** → `ReportSchedule` + `ScheduleController` (`/api/admin/schedules`)

## Endpoints principaux

| Méthode | URL | Rôle requis | Description |
|---|---|---|---|
| POST | `/api/auth/login` | public | Connexion (retourne un JWT) |
| GET/POST | `/api/admin/users` | ADMIN | Gestion des utilisateurs |
| POST | `/api/reports/import` | ADMIN, MANAGER | Import d'un fichier Excel |
| POST | `/api/reports/generate?start=...&end=...` | ADMIN, MANAGER | Générer un rapport |
| GET | `/api/reports` | tous | Historique des rapports |
| GET | `/api/reports/{id}/download/pdf` | tous | Télécharger le PDF |
| GET | `/api/reports/{id}/download/excel` | tous | Télécharger l'Excel |
| GET | `/api/dashboard/kpis` | tous | Indicateurs du tableau de bord |
| GET/POST | `/api/admin/schedules` | ADMIN | Planification des envois |

## Démarrage

1. Créer la base de données : `CREATE DATABASE reporting_db;`
2. Configurer `src/main/resources/application.properties` :
   - identifiants MySQL/PostgreSQL
   - identifiants SMTP (`spring.mail.*`) pour l'envoi d'emails
   - `app.jwt.secret` (à changer en production)
3. Lancer : `mvn spring-boot:run`
4. Un compte administrateur est créé automatiquement au premier démarrage :
   `admin@sews.com` / `Admin@123` (**à changer immédiatement**).

## Format du fichier Excel attendu

Colonnes obligatoires dans la première feuille :

| Production_Date | Line | Quantity | Defects |
|---|---|---|---|
| 2026-07-01 | Ligne A | 20000 | 150 |
| 2026-07-01 | Ligne B | 30000 | 200 |

## Évolutions prévues (section 8 du cahier des charges)

- Intégration directe avec l'ERP SIGIP (connexion DB2 IBM i) pour remplacer les exports Excel
- Détection d'anomalies / prédiction via IA sur les données de production

Ces évolutions ne sont pas implémentées ici mais l'architecture (couche `service` séparée de la
couche `repository`) permet de brancher une nouvelle source de données (SIGIP) sans modifier les
contrôleurs ni la génération de rapports.
