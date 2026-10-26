# 🧪 GUIDE DE TEST - VÉRIFICATION EMAIL

## 🎯 Objectif

Tester la fonctionnalité de **vérification par email avant envoi de rapports**.

---

## 🚀 OPTION 1 : TEST AVEC MOCK (Sans Backend)

### Étape 1 : Activer le mode Mock

```bash
cd autoreport-frontend
```

Renommer le fichier mock :
```bash
# Windows PowerShell
Move-Item src/api/email-verification.mock.js src/api/email-verification-BACKUP.js.tmp
Move-Item src/api/email-verification.js src/api/email-verification-REAL.js.tmp
Move-Item src/api/email-verification-BACKUP.js.tmp src/api/email-verification.js
```

### Étape 2 : Démarrer le frontend

```bash
npm run dev
```

### Étape 3 : Tester

1. Allez sur `http://localhost:5173`
2. Connectez-vous
3. Page **Rapports**
4. Cliquez sur **📧 Envoyer par email** d'un rapport

### Étape 4 : Workflow de test

```
┌─────────────────────────────────────────────────┐
│ 1. Entrer email : test@example.com             │
│    → Cliquer "Envoyer le code"                 │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 2. Ouvrir la Console (F12)                     │
│    → Copier le code affiché :                  │
│       🔐 CODE DE VÉRIFICATION MOCK: 123456     │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 3. Entrer le code dans le modal                │
│    → Cliquer "Vérifier et envoyer"             │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 4. ✅ Succès !                                  │
│    "Rapport envoyé avec succès !"              │
└─────────────────────────────────────────────────┘
```

### Étape 5 : Tests spécifiques

#### Test A : Code correct
```javascript
// Console affiche : 🔐 CODE: 123456
// Entrer : 123456
// Résultat : ✅ Succès
```

#### Test B : Code incorrect
```javascript
// Console affiche : 🔐 CODE: 123456
// Entrer : 999999 (MAUVAIS)
// Résultat : ❌ "Code incorrect. Il vous reste 2 tentative(s)"
// Réessayer avec 123456
// Résultat : ✅ Succès
```

#### Test C : Expiration (simulée)
```javascript
// Dans la console, modifier l'expiration :
mockVerifications.forEach(v => {
  v.expiresAt = new Date(Date.now() - 1000).toISOString();
});
// Entrer le code
// Résultat : ❌ "Le code a expiré"
```

#### Test D : Renvoyer le code
```javascript
// Recevoir le premier code : 123456
// Cliquer "Renvoyer le code"
// Console affiche : 🔐 NOUVEAU CODE: 654321
// Entrer le nouveau code
// Résultat : ✅ Succès
```

---

## 🌐 OPTION 2 : TEST AVEC BACKEND RÉEL

### Prérequis Backend

✅ Backend Spring Boot lancé sur `localhost:8080`  
✅ Table `email_verifications` créée  
✅ Configuration SMTP active (Gmail)  
✅ Endpoints implémentés

### Étape 1 : Vérifier que le backend est prêt

```bash
# Windows PowerShell
curl http://localhost:8080/api/health
# ou
Invoke-WebRequest -Uri http://localhost:8080/api/health
```

### Étape 2 : Utiliser la vraie API

```bash
cd autoreport-frontend
```

Remettre la vraie API :
```bash
# Si vous aviez activé le mock, restaurer :
Move-Item src/api/email-verification.js src/api/email-verification-MOCK.js.tmp
Move-Item src/api/email-verification-REAL.js.tmp src/api/email-verification.js
```

### Étape 3 : Démarrer l'application complète

```bash
# Terminal 1 - Backend
cd autoreport-backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd autoreport-frontend/autoreport-frontend
npm run dev
```

### Étape 4 : Tester avec email réel

1. Ouvrir `http://localhost:5173`
2. Se connecter
3. Page **Rapports** → Cliquer **📧 Envoyer**
4. Entrer **votre vraie adresse email** (Gmail, Outlook, etc.)
5. Cliquer "Envoyer le code"
6. **Ouvrir votre boîte mail** 📧
7. Copier le code à 6 chiffres
8. Le coller dans le modal
9. Cliquer "Vérifier et envoyer"
10. ✅ Vous recevrez le rapport par email !

---

## 📧 Email de Test (Exemple)

### Email 1 : Code de vérification

```
De: AutoReport <noreply@autoreport.com>
Sujet: Code de vérification - AutoReport

Code de vérification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Vous avez demandé à recevoir le rapport 
"Production Semaine 15".

Votre code de vérification est :

    ╔═══════════════╗
    ║   1 2 3 4 5 6 ║
    ╚═══════════════╝

⏱️ Ce code expire dans 5 minutes.

Si vous n'avez pas demandé ce code, 
ignorez cet email.
```

### Email 2 : Rapport envoyé

```
De: AutoReport <noreply@autoreport.com>
Sujet: Rapport : Production Semaine 15

Votre rapport est prêt !
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

• Titre: Production Semaine 15
• Période: 2024-04-08 → 2024-04-14
• Production: 15,234 unités
• Qualité: 96.5%

Le rapport est disponible en pièce jointe.

📎 rapport-production-semaine-15.pdf
```

---

## 🧪 CHECKLIST DE TESTS

### Tests Fonctionnels

- [ ] ✅ **Test 1** : Flux normal avec code correct
- [ ] ❌ **Test 2** : Code incorrect (3 tentatives max)
- [ ] ⏱️ **Test 3** : Code expiré après 5 minutes
- [ ] 🔄 **Test 4** : Renvoyer un nouveau code
- [ ] 📧 **Test 5** : Email invalide (validation)
- [ ] 🔒 **Test 6** : Blocage après 3 échecs
- [ ] ✏️ **Test 7** : Modifier l'email avant vérification
- [ ] 📱 **Test 8** : Responsive mobile

### Tests Visuels

- [ ] Modal s'ouvre correctement
- [ ] Input code en grand (taille 2xl, mono, centré)
- [ ] Timer d'expiration visible et fonctionnel
- [ ] Bouton "Renvoyer" désactivé avant 1 minute
- [ ] Messages d'erreur clairs et colorés
- [ ] Animation de succès (✅)
- [ ] Toast notifications fonctionnent

### Tests Backend (si disponible)

- [ ] Code généré et stocké en BDD
- [ ] Email envoyé via SMTP
- [ ] Code hashé (BCrypt)
- [ ] Expiration automatique après 5 min
- [ ] Limitation tentatives (max 3)
- [ ] Nettoyage automatique des codes expirés
- [ ] Rapport PDF envoyé en pièce jointe

---

## 🐛 DÉPANNAGE

### Problème 1 : "Network Error"

**Cause** : Backend non lancé ou mauvaise URL

**Solution** :
```bash
# Vérifier que le backend tourne
netstat -an | findstr 8080

# Vérifier .env
cat .env
# VITE_API_BASE_URL=http://localhost:8080
```

### Problème 2 : Code non reçu par email

**Cause** : Configuration SMTP incorrecte

**Solution** :
```properties
# application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=VOTRE_EMAIL@gmail.com
spring.mail.password=VOTRE_MOT_DE_PASSE_APP

# Générer un mot de passe d'application Gmail :
# https://myaccount.google.com/apppasswords
```

### Problème 3 : "Vérification introuvable"

**Cause** : Table SQL non créée

**Solution** :
```sql
-- Exécuter dans MySQL/MariaDB
USE autoreport_db;
SHOW TABLES LIKE 'email_verifications';

-- Si vide, créer la table (voir BACKEND_EMAIL_VERIFICATION_REQUIREMENTS.md)
```

### Problème 4 : Modal ne s'ouvre pas

**Cause** : Import manquant

**Solution** :
```javascript
// Vérifier dans Reports.jsx
import EmailVerificationModal from '../components/EmailVerificationModal';
```

### Problème 5 : Console affiche le code en production

**Solution** : Le `console.log` est uniquement dans le mock !  
En production, utilisez la vraie API qui envoie par email.

---

## 📊 RÉSULTATS ATTENDUS

### Flux Complet Réussi

```
1. Cliquer "Envoyer par email"        ✅
   ↓
2. Modal s'ouvre (étape email)        ✅
   ↓
3. Entrer email valide                ✅
   ↓
4. Recevoir le code (console/email)   ✅
   ↓
5. Modal passe à l'étape "code"       ✅
   ↓
6. Timer d'expiration visible         ✅
   ↓
7. Entrer le code correct             ✅
   ↓
8. Modal passe à l'étape "success"    ✅
   ↓
9. Toast de succès                    ✅
   ↓
10. Email reçu avec le rapport        ✅
```

### Capture d'écran attendue

```
╔════════════════════════════════════════════╗
║  Vérification email              [×]       ║
╠════════════════════════════════════════════╣
║                                            ║
║             📧                             ║
║   Code de vérification envoyé             ║
║   à user@example.com                      ║
║   Expire dans 4:37                        ║
║                                            ║
║   Entrez le code à 6 chiffres             ║
║                                            ║
║   ┌──────────────────────────────┐       ║
║   │      1  2  3  4  5  6         │       ║
║   └──────────────────────────────┘       ║
║                                            ║
║   🔄 Renvoyer le code                     ║
║                                            ║
║   [Modifier l'email] [Vérifier et envoyer]║
║                                            ║
╚════════════════════════════════════════════╝
```

---

## ✅ VALIDATION FINALE

Cochez quand tout fonctionne :

- [ ] Modal s'ouvre au clic
- [ ] Email validé (format)
- [ ] Code envoyé (console ou email)
- [ ] Timer fonctionne
- [ ] Code accepté si correct
- [ ] Erreur si code incorrect
- [ ] Renvoyer le code fonctionne
- [ ] Succès final affiché
- [ ] Rapport "envoyé" (log ou email réel)
- [ ] Fermer le modal après succès

---

## 🎓 POUR LA SOUTENANCE

### Points à démontrer :

1. **Sécurité** : "Avant d'envoyer un rapport sensible, on vérifie l'identité via email"
2. **UX fluide** : "3 étapes simples et guidées"
3. **Protection** : "Code expire en 5 minutes, 3 tentatives max"
4. **Feedback** : "Timer visible, messages clairs, animations"

### Phrase clé :
> "Pour garantir que les rapports confidentiels ne partent pas vers une mauvaise adresse, j'ai implémenté un système de double vérification par code à 6 chiffres avec expiration automatique."

---

**Prêt pour les tests ! 🚀**

Commencez par **OPTION 1 (Mock)** pour tester rapidement sans backend ! 
