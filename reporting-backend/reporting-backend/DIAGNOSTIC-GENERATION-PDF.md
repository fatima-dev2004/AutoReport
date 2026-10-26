# 🔍 Diagnostic : Échec Génération PDF

## ✅ Backend Fonctionnel

Le backend **fonctionne correctement** :
- ✅ Serveur démarré sur port 8080
- ✅ Template JasperReports corrigé (forecolor déplacé)
- ✅ Fichiers PDF générés récemment dans `generated-reports/`
- ✅ Aucune erreur dans les logs backend

## 🔧 Endpoints Disponibles

### 1. Génération de Rapport (Dashboard)
```
POST /api/dashboard/generate-report
Params:
  - start: LocalDate (optionnel, défaut: aujourd'hui - 30 jours)
  - end: LocalDate (optionnel, défaut: aujourd'hui)
  - enhanced: boolean (défaut: true)
  - title: String (optionnel)

Réponse:
{
  "success": true,
  "message": "Rapport généré avec succès",
  "reportId": 123,
  "title": "...",
  "downloadUrl": "/api/reports/123/download/pdf",
  "downloadUrlExcel": "/api/reports/123/download/excel"
}
```

### 2. Téléchargement PDF
```
GET /api/reports/{id}/download/pdf
Headers:
  - Authorization: Bearer <token>
```

### 3. Téléchargement Excel
```
GET /api/reports/{id}/download/excel
Headers:
  - Authorization: Bearer <token>
```

## 🐛 Causes Possibles de l'Erreur

### Côté Frontend

1. **Mauvaise URL** 
   - Frontend appelle `/generate` au lieu de `/dashboard/generate-report`
   
2. **Mauvais Content-Type**
   - Endpoint attend `application/json` mais reçoit `application/x-www-form-urlencoded`
   
3. **Token JWT manquant**
   - Le header `Authorization: Bearer <token>` n'est pas envoyé
   
4. **CORS**
   - Si frontend est sur un domaine différent (ex: localhost:3000), CORS peut bloquer

5. **Timeout Frontend**
   - La génération prend quelques secondes, le frontend timeout trop vite

6. **Gestion d'erreur Frontend**
   - Le backend retourne success=true mais le frontend vérifie autre chose

### Côté Backend (peu probable vu les logs)

1. **Données manquantes**
   - Aucune donnée dans la période sélectionnée
   - Mais le rapport serait quand même créé avec "Aucune donnée"
   
2. **Permissions fichier**
   - Le dossier `generated-reports/` n'est pas accessible en écriture
   - Mais on voit des PDFs récents

## 🧪 Tests à Faire

### Test 1: Appel Direct avec curl

```powershell
# Remplacez YOUR_TOKEN par votre token JWT

curl -X POST "http://localhost:8080/api/dashboard/generate-report?start=2026-08-01&end=2026-08-10&enhanced=true" `
  -H "Authorization: Bearer YOUR_TOKEN" `
  -H "Content-Type: application/json"
```

Si ça fonctionne, le problème est côté frontend.

### Test 2: Vérifier la Requête Frontend

1. Ouvrir F12 → Network
2. Cliquer sur "Générer PDF"
3. Trouver la requête qui échoue
4. Noter :
   - URL exacte
   - Méthode (GET/POST)
   - Headers
   - Body (si POST)
   - Code de statut (200, 400, 500...)
   - Réponse du serveur

### Test 3: Vérifier le Token

Dans la console du navigateur (F12 → Console) :
```javascript
console.log(localStorage.getItem('token'))
// ou
console.log(sessionStorage.getItem('token'))
```

Si `null`, le problème est l'authentification.

## 🔧 Solutions par Scénario

### Si "401 Unauthorized"
→ Le token JWT est invalide ou expiré
→ Solution: Se reconnecter

### Si "404 Not Found"
→ Le frontend appelle le mauvais endpoint
→ Solution: Corriger l'URL dans le code frontend

### Si "500 Internal Server Error"
→ Erreur backend (peu probable vu les logs)
→ Solution: Regarder les logs backend au moment exact de l'erreur

### Si "CORS error"
→ Frontend et backend sur domaines différents
→ Solution: Vérifier la config CORS dans SecurityConfig.java

### Si "Network Error" / "Failed to fetch"
→ Backend inaccessible ou timeout
→ Solution: Vérifier que le backend tourne sur localhost:8080

## 📝 Informations à Fournir

Pour diagnostiquer précisément, j'ai besoin de :

1. **Message d'erreur EXACT** affiché dans le frontend
2. **Requête qui échoue** (F12 → Network → copier comme curl)
3. **Code de statut HTTP** (200, 400, 401, 404, 500...)
4. **Réponse du serveur** (F12 → Network → Response tab)

## 🎯 Prochaine Étape

**Ouvrez F12 → Network → Cliquez sur "Générer PDF" → Trouvez la requête qui échoue**

Ensuite on pourra corriger précisément !
