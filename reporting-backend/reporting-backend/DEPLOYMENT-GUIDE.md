# Guide de Déploiement Production

## Prérequis

- Java 17+
- MySQL 8.0+ ou PostgreSQL 13+
- Maven 3.8+
- Certificat SSL pour HTTPS
- Serveur avec minimum 2GB RAM

## Configuration Production

### 1. Variables d'environnement

Créer un fichier `.env` basé sur `.env.example` :

```bash
cp .env.example .env
# Éditer .env avec vos valeurs réelles
```

**Variables critiques à configurer :**

- `JWT_SECRET` : Clé secrète forte (minimum 256 bits)
- `DB_USERNAME` et `DB_PASSWORD` : Credentials base de données
- `SMTP_USERNAME` et `SMTP_PASSWORD` : Credentials email

### 2. Base de données

Créer la base de données :

```sql
CREATE DATABASE reporting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'reporting_user'@'%' IDENTIFIED BY 'STRONG_PASSWORD';
GRANT ALL PRIVILEGES ON reporting_db.* TO 'reporting_user'@'%';
FLUSH PRIVILEGES;
```

### 3. Build de production

```bash
# Nettoyer et compiler
mvn clean package -DskipTests -Pprod

# Le JAR sera dans target/reporting-backend-1.0.0.jar
```

### 4. Lancement

```bash
# Avec variables d'environnement
export SPRING_PROFILES_ACTIVE=prod
java -jar target/reporting-backend-1.0.0.jar

# Ou avec fichier de configuration externe
java -jar target/reporting-backend-1.0.0.jar \
  --spring.config.location=file:./application-prod.properties
```

## Sécurité

### Checklist avant déploiement

- [ ] JWT_SECRET changé (minimum 256 bits)
- [ ] Credentials DB sécurisés
- [ ] HTTPS activé avec certificat valide
- [ ] SMTP credentials configurés
- [ ] Logs en mode production (INFO/WARN)
- [ ] ddl-auto=validate (pas create/update)
- [ ] Firewall configuré (port 8080 ou 443)
- [ ] Rate limiting activé (si implémenté)
- [ ] Backup automatique configuré

## Monitoring

### Endpoints Actuator

- `/actuator/health` : Santé de l'application
- `/actuator/metrics` : Métriques
- `/actuator/info` : Informations version

### Logs

Logs disponibles dans : `/var/log/reporting-backend/application.log`

```bash
# Suivre les logs en temps réel
tail -f /var/log/reporting-backend/application.log
```

## Déploiement Docker (Optionnel)

Créer `Dockerfile` :

```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/reporting-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

Build et run :

```bash
docker build -t reporting-backend .
docker run -p 8080:8080 --env-file .env reporting-backend
```

## Troubleshooting

### L'application ne démarre pas

- Vérifier les logs dans `application.log`
- Vérifier que MySQL est accessible
- Vérifier les variables d'environnement

### Erreurs de connexion DB

- Tester la connexion : `mysql -h HOST -u USER -p`
- Vérifier le firewall
- Vérifier les credentials

### Erreurs JWT

- Vérifier que JWT_SECRET est défini
- Longueur minimum 256 bits requise
