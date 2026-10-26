# 🚀 DÉMARRAGE RAPIDE

## Problème MySQL résolu !

MySQL refuse de démarrer sur votre machine. **Solution : Utiliser H2 Database** (base de données en mémoire, plus simple pour le développement).

## ✅ Comment démarrer le backend

### Méthode 1 : Double-cliquer sur le script
```
start-backend.bat
```

### Méthode 2 : Ligne de commande
```powershell
cd "c:\Users\pc\Desktop\works\SPRING BOOT 2024\projet de stage\reporting-backend"
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## 📊 Après le démarrage

- **Backend API** : http://localhost:8080
- **Console H2** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:reporting_db`
  - Username: `sa`
  - Password: (laisser vide)

## 👤 Compte par défaut

- **Email** : admin@sews.com
- **Mot de passe** : Admin@123

## ⚠️ Note importante

H2 est une base de données **en mémoire**. Les données sont perdues quand vous arrêtez l'application.

Pour la production, vous devrez :
1. Réparer MySQL
2. Ou utiliser PostgreSQL
3. Ou utiliser une base H2 persistante (fichier)

## 🔧 Revenir à MySQL plus tard

Éditez `application.properties` et commentez H2, décommentez MySQL.
