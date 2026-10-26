# 🏭 Plateforme de Reporting Industriel - Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

Backend REST API pour la plateforme d'automatisation de génération de rapports de production industrielle SEWS CABIND.

---

## 🚀 Quick Start

### Prérequis
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (ou H2 pour développement)

### Installation

```bash
# Cloner le projet
git clone <repository-url>
cd reporting-backend

# Démarrer (H2 en mémoire)
mvn spring-boot:run

# OU avec profil H2 explicite
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

L'application démarre sur **http://localhost:8080**

### Premier Test

```bash
# Login (utilisateur par défaut)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sews.com","password":"Admin@123"}'

# Réponse : {"token":"eyJhbG...", "refreshToken":"550e8400-...", ...}
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| **[SYNTHESE-PROJET.md](SYNTHESE-PROJET.md)** | 📋 Vue d'ensemble complète du projet |
| **[AMELIORATIONS-SOUTENANCE.md](AMELIORATIONS-SOUTENANCE.md)** | 🎯 Améliorations pour soutenance |
| **[DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md)** | 🚀 Guide de déploiement production |
| **[AUTHENTICATION-JWT-GUIDE.md](AUTHENTICATION-JWT-GUIDE.md)** | 🔐 Guide JWT, refresh token, logout |
| **[PAGINATION-GUIDE.md](PAGINATION-GUIDE.md)** | 📄 Guide pagination avec exemples |
| **[PHOTO-SECURITY-GUIDE.md](PHOTO-SECURITY-GUIDE.md)** | 🖼️ Sécurité photos de profil |
| **[README-TESTS.md](src/test/README-TESTS.md)** | 🧪 Guide des tests unitaires |

---

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/com/sews/reporting/
│   │   ├── config/           # Configuration (Security, CORS)
│   │   ├── controller/       # REST Controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # Entités JPA
│   │   ├── exception/        # Gestion erreurs
│   │   ├── repository/       # Repositories JPA
│   │   ├── security/         # JWT, Auth, Filters
│   │   └── service/          # Logique métier
│   └── resources/
│       ├── application.properties          # Config dev
│       ├── application-prod.properties     # Config prod
│       └── reports/                        # Templates JasperReports
└── test/
    ├── java/                 # Tests unitaires (30+)
    └── resources/
        └── test-data/        # Fichiers Excel de test
```

---

## 🔑 Fonctionnalités Principales

### ✅ Authentification & Sécurité
- JWT avec refresh token (7 jours)
- Logout sécurisé avec blacklist
- 3 rôles : ADMIN, MANAGER, USER
- 21 permissions granulaires

### 📊 Gestion des Rapports
- Import Excel intelligent (détection auto colonnes)
- Génération PDF/Excel professionnelle
- Planification automatique (CRON)
- Historique avec pagination et recherche
- Téléchargement et suppression sécurisés

### 👥 Gestion Utilisateurs
- CRUD complet (Admin)
- Photos de profil sécurisées
- Permissions personnalisables
- Profil utilisateur modifiable

### 📈 Dashboard & KPIs
- Production totale, défauts, qualité
- Agrégation par ligne de production
- Période personnalisable
- API REST pour graphiques

### 🔍 Audit & Traçabilité
- Logs automatiques de toutes actions
- Filtrage avancé et export CSV
- Tracking IP et User-Agent
- Pagination et statistiques

---

## 🌐 API Endpoints

### Authentification
```
POST   /api/auth/login       # Connexion
POST   /api/auth/refresh     # Renouveler token
POST   /api/auth/logout      # Déconnexion
```

### Rapports
```
GET    /api/reports                      # Liste (paginé)
POST   /api/reports/import               # Import Excel
POST   /api/reports/generate             # Génération simple
POST   /api/reports/generate/enhanced    # Génération améliorée
GET    /api/reports/{id}/download/pdf    # Téléchargement PDF
GET    /api/reports/{id}/download/excel  # Téléchargement Excel
DELETE /api/reports/{id}                 # Suppression
```

### Utilisateurs (Admin)
```
GET    /api/admin/users               # Liste (paginé)
POST   /api/admin/users               # Création
PUT    /api/admin/users/{id}          # Modification
DELETE /api/admin/users/{id}          # Suppression
POST   /api/admin/users/{id}/photo    # Upload photo
PUT    /api/admin/users/{id}/permissions  # Permissions
```

### Profil Utilisateur
```
GET    /api/profile           # Profil courant
PUT    /api/profile           # Mise à jour
GET    /api/profile/photo-url # URL photo
```

### Dashboard
```
GET    /api/dashboard/kpis    # KPIs période
```

### Audit Logs
```
GET    /api/audit-logs        # Logs (paginé, filtrable)
GET    /api/audit-logs/export # Export CSV
```

---

## 🧪 Tests

```bash
# Exécuter tous les tests
mvn test

# Exécuter un test spécifique
mvn test -Dtest=ExcelImportServiceTest

# Avec rapport de couverture
mvn clean test jacoco:report
```

**30+ tests unitaires** couvrant :
- Services (ExcelImportService, ReportGenerationService)
- Controllers (AuthController, PhotoController)
- Sécurité (JwtUtil)

---

## ⚙️ Configuration

### Développement (H2)

Utilise `application.properties` par défaut :
- Base H2 en mémoire
- Port 8080
- JWT : 24h (access) / 7j (refresh)
- Console H2 : http://localhost:8080/h2-console

### Production (MySQL)

1. Copier `.env.example` → `.env`
2. Configurer les variables :
   ```bash
   DB_URL=jdbc:mysql://localhost:3306/reporting_db
   DB_USERNAME=reporting_user
   DB_PASSWORD=***
   JWT_SECRET=*** (256 bits minimum)
   SMTP_USERNAME=***
   SMTP_PASSWORD=***
   ```
3. Lancer avec profil production :
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   mvn spring-boot:run
   ```

---

## 🔒 Sécurité

### Implémenté
- ✅ JWT avec signature HS256
- ✅ Refresh token stocké en BDD
- ✅ Blacklist des tokens révoqués
- ✅ Protection CORS
- ✅ Validation Bean (JSR-380)
- ✅ Protection path traversal
- ✅ HTTPS ready (production)
- ✅ Audit logs complet

### Recommandé Production
- [ ] Rate limiting sur /login
- [ ] Redis pour blacklist (multi-instances)
- [ ] Certificat SSL/TLS valide
- [ ] Firewall configuré
- [ ] Backup automatique BDD

---

## 📊 Base de Données

### Entités
- **User** : Utilisateurs (rôles, permissions, photos)
- **ProductionRecord** : Données brutes production
- **ProductionReport** : Rapports générés
- **ReportSchedule** : Planifications auto
- **RefreshToken** : Tokens refresh
- **AuditLog** : Traçabilité
- **SystemSettings** : Configuration
- **EmailRecipient** : Destinataires emails

### Migration
```sql
-- Initialisation automatique via Hibernate
spring.jpa.hibernate.ddl-auto=update  # Dev
spring.jpa.hibernate.ddl-auto=validate # Prod
```

---

## 🚀 Déploiement

### Build Production
```bash
mvn clean package -DskipTests -Pprod
```

### Docker (Optionnel)
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/reporting-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

```bash
docker build -t reporting-backend .
docker run -p 8080:8080 --env-file .env reporting-backend
```

---

## 🛠️ Technologies

### Backend
- **Spring Boot 3.3.2** : Framework principal
- **Spring Security** : Authentification/Autorisation
- **Spring Data JPA** : Persistence
- **Hibernate** : ORM
- **MySQL 8.0** : Base de données production
- **H2** : Base de données développement

### Librairies
- **JJWT 0.12.6** : JWT
- **JasperReports 6.21.3** : Génération PDF
- **Apache POI 5.2.5** : Traitement Excel
- **Lombok 1.18.42** : Boilerplate code
- **JUnit 5** : Tests unitaires
- **Mockito** : Mocking

---

## 📦 Structure Maven

```xml
<groupId>com.sews</groupId>
<artifactId>reporting-backend</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

**Dépendances principales :**
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-mail
- jasperreports
- poi + poi-ooxml
- jjwt-api + impl

---

## 🎓 Pour la Soutenance

### Démonstration Recommandée (15 min)

1. **Import & Génération** (5 min)
   - Import fichier Excel
   - Visualisation KPIs
   - Génération rapport PDF

2. **Sécurité** (3 min)
   - Login/Logout
   - Refresh token
   - Permissions par rôle

3. **Administration** (2 min)
   - Création utilisateur
   - Gestion permissions
   - Audit logs

4. **Dashboard** (2 min)
   - KPIs temps réel
   - Comparaison périodes

5. **Questions/Réponses** (3 min)

### Points Forts à Mentionner
- Architecture REST professionnelle
- Sécurité JWT moderne (refresh + blacklist)
- 30+ tests unitaires
- Pagination complète
- Production ready

---

## 📞 Support

### Logs
```bash
# Suivre les logs
tail -f logs/spring.log
```

### Debugging
```bash
# Démarrer en mode debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Problèmes Courants

**Port 8080 déjà utilisé**
```bash
# Trouver le processus
netstat -ano | findstr :8080
# Tuer le processus
taskkill /PID <PID> /F
```

**Base de données inaccessible**
```bash
# Vérifier MySQL
mysql -u root -p
# Vérifier configuration
spring.datasource.url dans application.properties
```

---

## 📈 Statistiques Projet

- **Lignes de code** : ~8 000
- **Classes Java** : 50+
- **Endpoints REST** : 25+
- **Tests unitaires** : 30+
- **Documentation** : 7 guides MD
- **Temps développement** : 3 mois
- **Couverture tests** : 70%+

---

## 🏆 Améliorations Apportées

Comparé à la version initiale :

| Amélioration | Statut |
|--------------|--------|
| Tests unitaires | ✅ 0 → 30+ |
| Refresh token | ✅ Implémenté |
| Logout sécurisé | ✅ Avec blacklist |
| Pagination | ✅ Sur tous endpoints |
| Sécurité photos | ✅ Authentification requise |
| Suppression rapports | ✅ Avec permissions |
| Config production | ✅ application-prod.properties |

**Voir [AMELIORATIONS-SOUTENANCE.md](AMELIORATIONS-SOUTENANCE.md) pour détails complets.**

---

## 📄 License

Proprietary - SEWS CABIND © 2026

---

## 👤 Auteur

**Projet de Stage**  
SEWS CABIND - Plateforme de Reporting Industriel  
*Août 2026*

---

**🎓 Prêt pour la soutenance !**

Pour toute question, consulter la documentation dans le répertoire racine ou les commentaires dans le code source.
