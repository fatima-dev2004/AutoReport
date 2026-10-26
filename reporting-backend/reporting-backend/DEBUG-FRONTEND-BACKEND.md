# 🔍 Guide de Débogage - Connexion Frontend-Backend

**Problème:** "Erreur lors de l'enregistrement" dans les paramètres système

---

## ✅ Vérifications Backend (FAIT)

- [x] Tables `system_settings` et `audit_logs` créées
- [x] 17 paramètres initialisés
- [x] Application tourne sur port 8080
- [x] Compilation sans erreurs

---

## ❓ À Vérifier dans le Frontend

### 1. URL de l'API

**Vérifier dans le code JavaScript:**

Le frontend doit appeler:
```
PUT http://localhost:8080/api/admin/settings
```

**Erreurs communes:**
- ❌ `/api/settings` (mauvais chemin)
- ❌ `/api/system/settings` (mauvais chemin)
- ✅ `/api/admin/settings` (correct)

---

### 2. Structure du Body

**Le backend attend ce format:**
```json
{
  "smtp": {
    "smtp.host": "smtp.gmail.com",
    "smtp.port": "587",
    "smtp.username": "Nourbenritounia@gmail.com",
    "smtp.password": "••••••••",
    "smtp.from": "benritouniafatima08@gmail.com",
    "smtp.fromName": "AutoReport System"
  },
  "company": {
    "company.name": "SEWS",
    "company.logo": "",
    "company.address": "",
    "company.phone": "",
    "company.email": ""
  },
  "scheduling": {
    "scheduling.defaultDays": "MONDAY,FRIDAY",
    "scheduling.defaultTime": "08:00",
    "scheduling.timezone": "Africa/Casablanca"
  },
  "general": {
    "general.dateFormat": "dd/MM/yyyy",
    "general.language": "fr",
    "general.reportsRetentionDays": "90"
  }
}
```

**Important:** Les clés doivent inclure le préfixe (ex: `"smtp.host"` pas juste `"host"`)

---

### 3. Headers HTTP

**Headers requis:**
```javascript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer ' + token
}
```

---

### 4. Code Frontend à Vérifier

**Ouvrir la console du navigateur (F12) et vérifier:**

#### A. Requête envoyée
```javascript
// Dans le fichier JavaScript qui gère les paramètres
async function saveSettings() {
  const token = localStorage.getItem('token');
  
  console.log('=== DEBUT DEBUG ===');
  console.log('Token:', token ? 'Présent' : 'MANQUANT');
  
  const settingsData = {
    smtp: {
      'smtp.host': document.getElementById('smtpHost').value,
      'smtp.port': document.getElementById('smtpPort').value,
      'smtp.username': document.getElementById('smtpUsername').value,
      'smtp.password': document.getElementById('smtpPassword').value,
      'smtp.from': document.getElementById('smtpFrom').value,
      'smtp.fromName': document.getElementById('smtpFromName').value
    },
    company: {
      'company.name': document.getElementById('companyName').value,
      // ... autres champs
    },
    scheduling: {
      'scheduling.defaultDays': getSelectedDays(), // Function qui retourne "MONDAY,FRIDAY"
      'scheduling.defaultTime': document.getElementById('scheduleTime').value,
      'scheduling.timezone': 'Africa/Casablanca'
    },
    general: {
      'general.dateFormat': 'dd/MM/yyyy',
      'general.language': 'fr',
      'general.reportsRetentionDays': '90'
    }
  };
  
  console.log('Données à envoyer:', JSON.stringify(settingsData, null, 2));
  console.log('URL:', 'http://localhost:8080/api/admin/settings');
  
  try {
    const response = await fetch('http://localhost:8080/api/admin/settings', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(settingsData)
    });
    
    console.log('Status:', response.status);
    console.log('Status Text:', response.statusText);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Erreur serveur:', errorText);
      throw new Error(`Erreur ${response.status}: ${errorText}`);
    }
    
    const result = await response.json();
    console.log('Réponse:', result);
    console.log('=== FIN DEBUG ===');
    
    // Afficher succès
    showNotification('Paramètres enregistrés avec succès!', 'success');
    
  } catch (error) {
    console.error('=== ERREUR ===');
    console.error('Message:', error.message);
    console.error('Stack:', error.stack);
    console.error('=== FIN ERREUR ===');
    
    showNotification('Erreur lors de l\'enregistrement', 'error');
  }
}
```

---

### 5. Vérification dans la Console Navigateur

**Ouvrir F12 → Console, puis cliquer sur "Enregistrer"**

**Vous devriez voir:**
```
=== DEBUT DEBUG ===
Token: Présent
Données à envoyer: { smtp: {...}, company: {...}, ... }
URL: http://localhost:8080/api/admin/settings
Status: 200
Status Text: OK
Réponse: {message: "Paramètres mis à jour avec succès"}
=== FIN DEBUG ===
```

**Si vous voyez plutôt:**
```
=== ERREUR ===
Message: Failed to fetch
```
→ Problème CORS ou backend non accessible

```
=== ERREUR ===
Message: Erreur 401: Unauthorized
```
→ Problème de token (expiré ou invalide)

```
=== ERREUR ===
Message: Erreur 403: Forbidden
```
→ L'utilisateur n'a pas la permission `CONFIGURE_SYSTEM`

```
=== ERREUR ===
Message: Erreur 404: Not Found
```
→ Mauvaise URL (vérifier `/api/admin/settings`)

```
=== ERREUR ===
Message: Erreur 400: Bad Request
```
→ Problème dans la structure du JSON envoyé

---

### 6. Vérification des Permissions

**L'utilisateur connecté doit avoir la permission `CONFIGURE_SYSTEM`**

**Vérifier dans le backend:**
```sql
-- Dans H2 Console (http://localhost:8080/h2-console)
SELECT u.email, p.permission 
FROM users u 
LEFT JOIN user_permissions p ON u.id = p.user_id 
WHERE u.email = 'admin@sews.com';
```

**Résultat attendu:**
```
admin@sews.com | CONFIGURE_SYSTEM
admin@sews.com | MANAGE_EMAIL_SETTINGS
admin@sews.com | VIEW_LOGS
... (toutes les permissions admin)
```

---

### 7. Test Direct avec cURL (depuis PowerShell)

**Pour vérifier que le backend fonctionne:**

```powershell
# 1. Login pour obtenir le token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"email":"admin@sews.com","password":"Admin@123"}'

$token = $loginResponse.token
Write-Host "Token: $token"

# 2. Récupérer les paramètres actuels
$headers = @{
  "Authorization" = "Bearer $token"
  "Content-Type" = "application/json"
}

$settings = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings" `
  -Method Get `
  -Headers $headers

Write-Host "Paramètres actuels:"
$settings | ConvertTo-Json -Depth 3

# 3. Mettre à jour les paramètres
$updateData = @{
  smtp = @{
    "smtp.host" = "smtp.gmail.com"
    "smtp.port" = "587"
    "smtp.username" = "test@gmail.com"
    "smtp.password" = "password"
    "smtp.from" = "noreply@sews.com"
    "smtp.fromName" = "SEWS Reporting"
  }
  company = @{
    "company.name" = "SEWS CABIND"
  }
  scheduling = @{
    "scheduling.defaultDays" = "MONDAY,FRIDAY"
    "scheduling.defaultTime" = "08:00"
  }
  general = @{
    "general.dateFormat" = "dd/MM/yyyy"
    "general.language" = "fr"
  }
} | ConvertTo-Json -Depth 3

$result = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings" `
  -Method Put `
  -Headers $headers `
  -Body $updateData

Write-Host "Résultat:"
$result | ConvertTo-Json
```

**Si cela fonctionne → Le problème est dans le frontend**  
**Si cela ne fonctionne pas → Le problème est dans le backend**

---

### 8. Vérification CORS

**Le backend doit autoriser les requêtes depuis le frontend**

**Dans tous les controllers, vérifier:**
```java
@CrossOrigin(origins = "*")  // Permet toutes les origines (dev seulement)
// OU
@CrossOrigin(origins = "http://localhost:3000")  // Origin spécifique
```

**Vérifier dans:**
- `SystemSettingsController.java` ✅
- `AuditLogController.java` ✅
- `ReportController.java` ✅

---

## 🔧 Solutions Rapides

### Solution 1: Vérifier l'URL Frontend

**Chercher dans le code frontend:**
```javascript
// Mauvais
fetch('/api/settings', ...)
fetch('http://localhost:8080/api/settings', ...)

// Correct
fetch('http://localhost:8080/api/admin/settings', ...)
```

### Solution 2: Vérifier la Structure des Données

**Le frontend envoie peut-être:**
```json
{
  "host": "smtp.gmail.com",  // ❌ Mauvais
  "port": "587"
}
```

**Au lieu de:**
```json
{
  "smtp": {
    "smtp.host": "smtp.gmail.com",  // ✅ Correct
    "smtp.port": "587"
  }
}
```

### Solution 3: Ajouter Gestion d'Erreur Détaillée

**Dans le frontend, changer:**
```javascript
// Avant
catch (error) {
  showNotification('Erreur lors de l\'enregistrement', 'error');
}

// Après
catch (error) {
  console.error('Erreur complète:', error);
  showNotification(`Erreur: ${error.message}`, 'error');
}
```

---

## 📋 Checklist de Débogage

1. [ ] Ouvrir F12 → Console
2. [ ] Cliquer sur "Enregistrer"
3. [ ] Noter l'erreur exacte dans la console
4. [ ] Vérifier l'onglet Network → Chercher la requête PUT
5. [ ] Vérifier le Status Code (200, 401, 403, 404, 500)
6. [ ] Vérifier le Request Payload
7. [ ] Vérifier le Response
8. [ ] Copier l'erreur exacte

---

## 📞 Informations pour le Support

**Envoyer ces informations:**
1. Message d'erreur exact dans la console (F12)
2. Status Code de la requête HTTP
3. Payload envoyé (Request)
4. Réponse reçue (Response)
5. URL exacte appelée

---

**Version Backend:** 2.0  
**Endpoint:** PUT /api/admin/settings  
**Status:** ✅ Opérationnel côté backend  
**Prochaine étape:** Vérifier le frontend avec console F12
