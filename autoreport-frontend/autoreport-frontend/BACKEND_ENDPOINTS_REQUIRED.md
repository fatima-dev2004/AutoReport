# 🔌 Endpoints Backend Requis

## Vue d'ensemble

Ce document liste tous les endpoints API que le frontend appelle. Le backend doit implémenter ces endpoints pour que l'application fonctionne complètement.

---

## ✅ Endpoints Déjà Implémentés (Existants)

### Authentification
```
POST /api/auth/login
Body: { email, password }
Response: { token, user: { id, email, fullName, role, enabled } }

POST /api/auth/logout
Headers: Authorization: Bearer {token}
```

### Dashboard
```
GET /api/dashboard/kpis?start={date}&end={date}
Response: {
  totalProduction, totalDefects, qualityRate, defectRate,
  dailyProduction: { "2024-01-10": 1500, ... },
  productionByLine: { "LINE-A": 5000, ... }
}
```

### Rapports
```
GET /api/reports?q={search}
Response: [{id, title, type, periodStart, periodEnd, totalProduction, qualityRate, generatedByEmail, generatedAt}]

POST /api/reports/generate
Body: { start, end }

POST /api/reports/import-excel
Body: FormData avec fichier Excel
Response: { recordsImported, errors: [], filename }

GET /api/reports/{id}/download?format=pdf|excel
Response: Fichier binaire
```

### Utilisateurs
```
GET /api/users?q={search}
Response: [{id, email, fullName, role, enabled, firstName, lastName, age, phone}]

POST /api/users
Body: { email, password, fullName, role, firstName, lastName, age, phone, enabled }

PUT /api/users/{id}/enabled
Body: { enabled: boolean }

PUT /api/users/{id}/role
Body: { role: "ADMIN"|"MANAGER"|"USER" }

DELETE /api/users/{id}
```

### Planification
```
GET /api/schedules
Response: [{id, frequency, recipients, time, enabled}]

POST /api/schedules
Body: { frequency, recipients, time, enabled }

DELETE /api/schedules/{id}
```

---

## 🆕 Endpoints à Implémenter (Nouveaux)

### 1. Paramètres Système (Settings)

#### Configuration SMTP
```http
GET /api/settings/smtp
Response: {
  host: string,
  port: number,
  username: string,
  password: string (masked),
  fromName: string,
  fromEmail: string
}

PUT /api/settings/smtp
Body: { host, port, username, password, fromName, fromEmail }
Response: { success: true }

POST /api/settings/smtp/test
Response: { success: true, message: "Email de test envoyé" }
```

#### Configuration Planification
```http
GET /api/settings/schedule
Response: {
  autoSendEnabled: boolean,
  sendTime: string ("HH:MM"),
  sendDays: string[] (["monday", "tuesday", ...])
}

PUT /api/settings/schedule
Body: { autoSendEnabled, sendTime, sendDays }
Response: { success: true }
```

#### Configuration Entreprise
```http
GET /api/settings/company
Response: {
  name: string,
  logoUrl: string (URL du logo)
}

PUT /api/settings/company
Body: FormData ou { name, logo: File }
Response: { success: true, logoUrl: string }
```

#### Destinataires Email
```http
GET /api/settings/email-recipients
Response: [{ id, name, email }]

POST /api/settings/email-recipients
Body: { name, email }
Response: { id, name, email }

DELETE /api/settings/email-recipients/{id}
Response: { success: true }
```

#### Toutes les configurations
```http
GET /api/settings
Response: {
  smtp: {...},
  schedule: {...},
  company: {...},
  emailRecipients: [...]
}
```

---

### 2. Audit Log (Journal d'Activité)

```http
GET /api/audit-logs?type={type}&user={user}&startDate={date}&endDate={date}&search={query}
Response: [{
  id: number,
  type: "login"|"report"|"import"|"email"|"download",
  typeLabel: string,
  user: string (email),
  userName: string,
  timestamp: string (ISO),
  details: string,
  ipAddress: string
}]

GET /api/audit-logs/stats
Response: {
  login: number,
  report: number,
  import: number,
  email: number,
  download: number
}

GET /api/audit-logs/export?type={type}&user={user}&startDate={date}&endDate={date}
Response: CSV file (Content-Type: text/csv)
```

**Types d'événements à tracker :**
- `login` - Connexions utilisateur
- `report` - Génération de rapports
- `import` - Imports Excel
- `email` - Envois d'emails
- `download` - Téléchargements de fichiers

---

### 3. Envoi de Rapport par Email (Optionnel mais Recommandé)

```http
POST /api/reports/{id}/send-email
Body: {
  recipients: string[] (emails),
  message: string (optionnel),
  ccUser: boolean (copier l'utilisateur)
}
Response: {
  success: true,
  sentTo: string[],
  timestamp: string
}
```

---

### 4. Chat IA (Optionnel - Bonus)

```http
POST /api/ai/chat
Body: {
  message: string,
  userId: number,
  conversationId: string (optionnel)
}
Response: {
  response: string,
  conversationId: string
}

GET /api/ai/conversations/{userId}
Response: [{
  id: string,
  userId: number,
  messages: [{role: "user"|"assistant", content: string, timestamp: string}],
  createdAt: string
}]
```

**Alternative** : Intégrer directement Google Gemini API côté frontend
```javascript
// Dans AIChat.jsx, ajouter :
const GEMINI_API_KEY = import.meta.env.VITE_GEMINI_API_KEY;
const response = await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'x-goog-api-key': GEMINI_API_KEY
  },
  body: JSON.stringify({
    contents: [{ parts: [{ text: userMessage }] }]
  })
});
```

---

## 📊 Priorité d'Implémentation

### Priorité 1 (CRITIQUE - Cahier des Charges)
- [x] Authentification (déjà fait)
- [x] Dashboard KPIs (déjà fait)
- [x] Import Excel (déjà fait)
- [x] Génération Rapports (déjà fait)
- [x] Téléchargement PDF/Excel (déjà fait)
- [x] Gestion Utilisateurs (déjà fait)

### Priorité 2 (IMPORTANT - Soutenance)
- [ ] **Settings** - Configuration système
- [ ] **Audit Log** - Traçabilité
- [ ] **Email Recipients** - Gestion destinataires

### Priorité 3 (BONUS - Impression Jury)
- [ ] Envoi Rapport par Email
- [ ] Chat IA (ou utiliser API Gemini directe)
- [ ] Export CSV Audit Log

---

## 🔐 Authentification

Tous les endpoints (sauf `/api/auth/login`) doivent vérifier le token JWT :

```http
Headers:
Authorization: Bearer {jwt_token}
```

**Rôles et permissions :**
- `USER` - Lecture Dashboard et Rapports
- `MANAGER` - USER + Import Excel + Génération Rapports
- `ADMIN` - Tout + Gestion Users + Settings + Audit Log

---

## 🧪 Comment Tester

### Backend Local
```bash
# Tester un endpoint
curl -X GET http://localhost:8080/api/settings \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Avec Postman
1. Importer la collection (créer un fichier `postman_collection.json`)
2. Ajouter le token JWT dans Authorization
3. Tester chaque endpoint

### Frontend avec Mock
Si backend pas prêt, le frontend a des fallbacks :
- AuditLog.jsx → génère des données mock
- Settings.jsx → utilise localStorage
- Chat IA → réponses codées en dur

---

## 📝 Exemple de Réponse d'Erreur

Standardiser les erreurs :
```json
{
  "error": "Message d'erreur lisible",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-10T10:30:00Z"
}
```

**Codes HTTP :**
- 200 - Success
- 201 - Created
- 400 - Bad Request
- 401 - Unauthorized
- 403 - Forbidden
- 404 - Not Found
- 500 - Internal Server Error

---

## ✅ Checklist Backend

### Configuration
- [ ] CORS configuré pour `http://localhost:5173`
- [ ] JWT configuré avec secret sécurisé
- [ ] Base de données connectée
- [ ] SMTP configuré (pour emails)

### Endpoints Critiques
- [ ] `/api/settings/*` - Tous fonctionnels
- [ ] `/api/audit-logs` - Listing avec filtres
- [ ] `/api/audit-logs/stats` - Statistiques
- [ ] `/api/audit-logs/export` - Export CSV

### Tests
- [ ] Tous les endpoints retournent JSON valide
- [ ] Authentification vérifiée sur chaque endpoint
- [ ] Rôles respectés
- [ ] Pagination fonctionne
- [ ] Filtres fonctionnent

---

## 📞 Support

Si backend pas encore implémenté :
1. Frontend fonctionne avec données mock
2. Fallbacks en place
3. Aucun crash si API indisponible
4. Messages d'erreur clairs

---

**Statut Actuel** : 
- ✅ Endpoints de base (70%) implémentés
- 🔄 Settings API (0%) - À implémenter
- 🔄 Audit Log API (0%) - À implémenter
- ⏳ Bonus features (0%) - Optionnel

**Temps estimé backend** :
- Settings API : 2-3 heures
- Audit Log API : 2-3 heures
- Tests complets : 1-2 heures
- **Total : 5-8 heures**
