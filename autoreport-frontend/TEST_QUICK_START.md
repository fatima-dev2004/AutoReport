# ⚡ DÉMARRAGE RAPIDE - TEST VÉRIFICATION EMAIL

## 🎯 Test en 2 minutes (Mode Mock - Sans Backend)

### 1️⃣ Démarrer le frontend

```powershell
cd autoreport-frontend\autoreport-frontend
npm run dev
```

### 2️⃣ Ouvrir l'application

**URL** : `http://localhost:5173`

### 3️⃣ Se connecter

```
Email: admin@sews.com
Mot de passe: admin123
```

### 4️⃣ Aller sur la page Rapports

Menu → **Rapports**

### 5️⃣ Cliquer sur "Envoyer par email" 📧

Sur n'importe quel rapport de la liste

### 6️⃣ Suivre le workflow

```
┌──────────────────────────────────┐
│ ÉTAPE 1 : Entrer votre email    │
├──────────────────────────────────┤
│ Email: test@example.com          │
│                                  │
│ [Envoyer le code] ◄─ CLIQUER    │
└──────────────────────────────────┘
```

### 7️⃣ Ouvrir la Console (F12)

**Cherchez cette ligne :**
```
🔐 CODE DE VÉRIFICATION MOCK: 123456
```

### 8️⃣ Copier le code

Le code affiché dans la console (6 chiffres)

### 9️⃣ Entrer le code dans le modal

```
┌──────────────────────────────────┐
│ ÉTAPE 2 : Entrer le code         │
├──────────────────────────────────┤
│   ┌──────────────────────────┐  │
│   │   1  2  3  4  5  6       │  │
│   └──────────────────────────┘  │
│                                  │
│ [Vérifier et envoyer] ◄─ CLIQUER│
└──────────────────────────────────┘
```

### 🔟 Succès ! ✅

```
┌──────────────────────────────────┐
│          ✅                       │
│  Rapport envoyé avec succès !    │
│                                  │
│         [Fermer]                 │
└──────────────────────────────────┘
```

---

## 🧪 TESTS RAPIDES

### ✅ Test 1 : Code correct
```
Code console: 123456
Entrer: 123456
Résultat: ✅ Succès !
```

### ❌ Test 2 : Code incorrect
```
Code console: 123456
Entrer: 999999
Résultat: ❌ "Code incorrect. Il vous reste 2 tentative(s)"
Réessayer avec 123456
Résultat: ✅ Succès !
```

### 🔄 Test 3 : Renvoyer le code
```
Recevoir code: 123456
Cliquer "Renvoyer le code"
Nouveau code console: 654321
Entrer: 654321
Résultat: ✅ Succès !
```

---

## 📸 CAPTURES D'ÉCRAN ATTENDUES

### Écran 1 : Saisie email
```
╔═══════════════════════════════════════╗
║ Vérification email           [×]      ║
╠═══════════════════════════════════════╣
║ ⚠️ Vérification de sécurité           ║
║                                       ║
║ Adresse email                         ║
║ ┌───────────────────────────────────┐║
║ │📧 votre.email@example.com         │║
║ └───────────────────────────────────┘║
║                                       ║
║      [Annuler]  [Envoyer le code]    ║
╚═══════════════════════════════════════╝
```

### Écran 2 : Saisie code
```
╔═══════════════════════════════════════╗
║ Vérification email           [×]      ║
╠═══════════════════════════════════════╣
║            📧                         ║
║   Code de vérification envoyé        ║
║   à votre.email@example.com          ║
║   Expire dans 4:57                   ║
║                                       ║
║   Entrez le code à 6 chiffres        ║
║   ┌───────────────────────────────┐ ║
║   │      1  2  3  4  5  6         │ ║
║   └───────────────────────────────┘ ║
║                                       ║
║   🔄 Renvoyer le code                ║
║                                       ║
║ [Modifier]  [Vérifier et envoyer]    ║
╚═══════════════════════════════════════╝
```

### Écran 3 : Succès
```
╔═══════════════════════════════════════╗
║ Vérification email           [×]      ║
╠═══════════════════════════════════════╣
║            ✅                         ║
║   Rapport envoyé avec succès !       ║
║                                       ║
║   Le rapport "Production Sem. 15"    ║
║   a été envoyé à votre.email@...     ║
║                                       ║
║              [Fermer]                ║
╚═══════════════════════════════════════╝
```

---

## 🎓 POUR LA SOUTENANCE

### Démonstration (30 secondes)

1. "Je vais envoyer ce rapport par email de façon sécurisée"
2. Cliquer 📧 → Entrer email
3. "Un code de vérification est envoyé"
4. Montrer la console : "Voici le code"
5. Entrer le code → Succès ✅
6. "Le rapport est maintenant envoyé !"

### Phrase clé
> "Pour éviter les erreurs d'envoi de données sensibles, j'ai implémenté une double vérification par code temporaire qui expire en 5 minutes."

### Points forts
- ✅ Sécurité (double vérification)
- ✅ UX fluide (3 étapes guidées)
- ✅ Protection (expiration + limitation tentatives)
- ✅ Feedback visuel (timer, messages clairs)

---

## ⚠️ SI PROBLÈME

### Le modal ne s'ouvre pas ?
**Vérifiez que le bouton 📧 est bien là.**
Si non, actualisez la page (F5).

### Pas de code dans la console ?
**Ouvrez la console AVANT de cliquer "Envoyer le code"**
Console = F12 → Onglet "Console"

### Erreur "Network Error" ?
**C'est normal en mode mock !**
Le mock n'a pas besoin du backend.

---

## 🚀 MODE AVANCÉ (Avec Backend Réel)

Si votre backend Spring Boot est prêt :

1. **Vérifier** : Backend sur `http://localhost:8080`
2. **Configurer SMTP** dans `application.properties`
3. **Utiliser votre VRAI email** dans le test
4. **Recevoir le code par email** 📧
5. **Recevoir le rapport par email** 📎

---

**C'est parti ! 🎉**

Temps total : **2 minutes** ⏱️
