# 🔍 Diagnostic Problème Envoi Email

## ❌ Erreur Constatée
**"Échec de l'envoi du code de vérification"**

---

## ✅ Configuration Actuelle

### SMTP Configuré dans la Base
```
Host: smtp.gmail.com
Port: 587
Username: fatimabenritounia37@gmail.com
Password: taqe cfnu nqcn teda (mot de passe application)
From: fatimabenritounia37@gmail.com
From Name: SEWS Reporting
```

---

## 🔍 Causes Possibles

### 1. **Mot de Passe Application Gmail Invalide**

Le mot de passe `taqe cfnu nqcn teda` est peut-être :
- ❌ Expiré
- ❌ Révoqué
- ❌ Invalide

**Solution :**
1. Allez sur https://myaccount.google.com/apppasswords
2. Créez un NOUVEAU mot de passe d'application
3. Configurez-le dans l'application (Paramètres > SMTP)

---

### 2. **Compte Gmail Bloqué**

Gmail a peut-être bloqué l'accès SMTP.

**Vérification :**
1. Vérifiez vos emails Gmail pour un message de sécurité
2. Allez sur https://myaccount.google.com/security
3. Vérifiez l'activité récente
4. Autorisez l'accès si bloqué

---

### 3. **Validation en 2 Étapes Non Activée**

Les mots de passe d'application nécessitent la validation en 2 étapes.

**Solution :**
1. Allez sur https://myaccount.google.com/security
2. Activez la validation en 2 étapes
3. Créez un nouveau mot de passe d'application

---

### 4. **Port 587 Bloqué par le Pare-feu**

Le port SMTP peut être bloqué.

**Test :**
```powershell
Test-NetConnection -ComputerName smtp.gmail.com -Port 587
```

Si échec → Débloquer le port dans le pare-feu Windows

---

### 5. **Problème SSL/TLS**

La configuration SSL peut être incorrecte.

**Vérification dans le Code :**
- `spring.mail.properties.mail.smtp.starttls.enable=true`
- `spring.mail.properties.mail.smtp.auth=true`

---

## 🧪 Tests à Faire

### Test 1 : Vérifier Connexion SMTP
```powershell
Test-NetConnection -ComputerName smtp.gmail.com -Port 587
```

**Résultat attendu :**
```
TcpTestSucceeded : True
```

---

### Test 2 : Test Email depuis l'Application

1. **Se connecter en tant qu'admin**
2. **Aller dans Paramètres** > Configuration SMTP
3. **Cliquer "Tester la connexion"**
4. **Vérifier l'email reçu**

**OU via API :**
```bash
curl -X POST http://localhost:8080/api/admin/settings/test-email \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email":"fatimabenritounia37@gmail.com"}'
```

---

### Test 3 : Voir les Logs Backend en Temps Réel

1. **Backend doit tourner**
2. **Cliquer "Envoyer le code"** dans le frontend
3. **Regarder immédiatement les logs backend**

**Les logs montreront l'erreur exacte :**
```
ERROR: Authentication failed
ERROR: Connection refused
ERROR: Invalid credentials
```

---

## 🔧 Solutions par Ordre de Priorité

### Solution 1 : Régénérer le Mot de Passe Application ⭐ (PRIORITÉ 1)

1. Allez sur https://myaccount.google.com/apppasswords
2. Supprimez l'ancien mot de passe "SEWS Reporting" (si existant)
3. Créez un nouveau :
   - Nom : "SEWS Reporting V2"
   - Copiez le mot de passe (ex: `xxxx yyyy zzzz aaaa`)
4. Dans l'application :
   - Paramètres > SMTP
   - Collez le NOUVEAU mot de passe
   - Testez la connexion

---

### Solution 2 : Vérifier Backend Logs

Regardez les logs quand vous cliquez "Envoyer le code" :

**Erreur typique :**
```
javax.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Solution :** Mot de passe invalide → Régénérer

---

### Solution 3 : Tester avec un Autre Email

Si Gmail bloque, testez avec :
- **Outlook** : smtp-mail.outlook.com:587
- **Yahoo** : smtp.mail.yahoo.com:587

---

### Solution 4 : Utiliser Application.properties par Défaut

Si la base de données pose problème, configurez directement dans `application.properties` :

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=fatimabenritounia37@gmail.com
spring.mail.password=NOUVEAU_MOT_DE_PASSE_ICI
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Redémarrez le backend.

---

## 🎯 Action Immédiate Recommandée

### Étape 1 : Régénérer le Mot de Passe
1. https://myaccount.google.com/apppasswords
2. Nouveau mot de passe
3. Copier

### Étape 2 : Mettre à Jour dans l'Application
1. Se connecter en admin
2. Paramètres > SMTP
3. Coller le nouveau mot de passe
4. **Sauvegarder**
5. **Tester la connexion**

### Étape 3 : Réessayer
1. Aller sur la page d'envoi rapport
2. Cliquer "Envoyer le code"
3. Vérifier email reçu

---

## 📊 Diagnostic Rapide

| Vérification | Statut | Action |
|--------------|--------|--------|
| Configuration SMTP existe | ✅ OUI | Vérifier mot de passe |
| Port 587 accessible | ❓ À tester | `Test-NetConnection` |
| Mot de passe valide | ❓ À vérifier | Régénérer |
| Validation 2 étapes active | ❓ À vérifier | Activer si besoin |
| Backend logs | ❓ À regarder | Reproduire erreur |

---

## 📞 Si Rien Ne Marche

1. **Envoyez-moi les logs backend** au moment exact de l'erreur
2. **Capture d'écran** de l'onglet Network (F12) > requête "send-code"
3. **Message d'erreur** exact dans la console backend

---

## 🚀 Solution de Secours : Service Email Alternatif

Si Gmail bloque définitivement, utilisez :

### Brevo (ex-Sendinblue) - GRATUIT
- 300 emails/jour gratuits
- Configuration simple
- Pas de mot de passe application

**Configuration :**
```
Host: smtp-relay.brevo.com
Port: 587
Username: votre-email@gmail.com
Password: Clé API Brevo
```

---

**La cause #1 est souvent : mot de passe application invalide/expiré ! 🔑**
