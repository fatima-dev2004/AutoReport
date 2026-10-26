# 🤖 CONFIGURATION GEMINI AI

## 📋 ÉTAPES RAPIDES

### 1️⃣ Obtenir la Clé API (2 minutes)

1. **Aller sur** : https://aistudio.google.com/app/apikey
2. **Se connecter** avec votre compte Google
3. **Cliquer** sur "Create API Key" (Créer une clé API)
4. **Copier** la clé (format : `AIzaSy...`)

---

### 2️⃣ Configurer le Frontend (30 secondes)

1. **Ouvrir** le fichier `.env` dans `autoreport-frontend/`
2. **Remplacer** `VOTRE_CLE_API_ICI` par votre vraie clé
3. **Sauvegarder** le fichier

**Exemple** :
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GEMINI_API_KEY=AIzaSyDXXXXXXXXXXXXXXXXXXXXXXXXX
```

---

### 3️⃣ Redémarrer le Frontend (10 secondes)

```bash
# Arrêter le serveur (Ctrl+C)
# Redémarrer
npm run dev
```

---

### 4️⃣ Tester le Chat IA (1 minute)

1. **Aller sur** : http://localhost:5173
2. **Cliquer** sur "Chat IA" dans la navigation
3. **Poser une question** : "Explique-moi les indicateurs de production"
4. ✅ **Le chatbot répond !**

---

## 📸 CAPTURE D'ÉCRAN DE L'API

Quand vous êtes sur https://aistudio.google.com/app/apikey :

```
┌─────────────────────────────────────────┐
│  Google AI Studio                       │
├─────────────────────────────────────────┤
│                                         │
│  Your API Keys                          │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Create API Key                    │ │
│  └───────────────────────────────────┘ │
│                                         │
│  My API Key                             │
│  AIzaSy... [Copy] [Delete]              │
│                                         │
└─────────────────────────────────────────┘
```

---

## ⚠️ IMPORTANT

### Sécurité de la Clé
- ✅ **Ne partagez JAMAIS** votre clé API publiquement
- ✅ Le fichier `.env` est dans `.gitignore` (pas committé)
- ✅ La clé fonctionne côté navigateur (client-side)

### Quota Gratuit
- **Limite** : 60 requêtes/minute
- **Coût** : GRATUIT pour usage normal
- **Suffisant** : Pour démonstration et tests

---

## 🎯 FONCTIONNALITÉS DU CHAT

### Questions Supportées
✅ Analyse de production  
✅ Explication des indicateurs  
✅ Conseils qualité  
✅ Prédictions tendances  
✅ Assistance utilisateur  

### Exemple de Questions

```
❓ "Quels sont mes taux de qualité ?"
❓ "Comment réduire les défauts ?"
❓ "Analyse les tendances de production"
❓ "Explique le taux de défaut"
❓ "Donne-moi des recommandations"
```

---

## 🔧 DÉPANNAGE

### Problème : Chat ne répond pas

**Vérifier** :
1. ✅ Clé API copiée correctement (pas d'espaces)
2. ✅ Fichier `.env` sauvegardé
3. ✅ Frontend redémarré après modification
4. ✅ Console navigateur (F12) pour voir erreurs

**Erreur commune** :
```
❌ "API key not valid"
→ Vérifier que la clé commence par AIzaSy...
```

### Problème : Quota dépassé

**Message** : `429 - Quota exceeded`

**Solution** :
- Attendre 1 minute
- Créer une nouvelle clé API

---

## 📂 STRUCTURE FICHIERS

```
autoreport-frontend/
├── .env                    ✅ Votre clé ici
├── .env.example           📋 Template
├── .gitignore             🔒 .env exclu
└── src/
    └── pages/
        └── AIChat.jsx     🤖 Page chat
```

---

## 🎓 POUR LA SOUTENANCE

### Démonstration Chat IA (1 minute)

1. **Ouvrir** Chat IA
2. **Poser** : "Analyse mes indicateurs de production"
3. **Montrer** : Réponse détaillée du chatbot
4. **Expliquer** : 
   - Utilise Gemini AI de Google
   - Analyse en temps réel
   - Conseils personnalisés

### Phrase Clé
> "J'ai intégré **Gemini AI** de Google pour fournir une assistance intelligente. Le chatbot analyse les données de production et donne des recommandations en temps réel."

---

## 🌟 AVANTAGES GEMINI

✅ **Gratuit** : Pas de coût pour usage normal  
✅ **Intelligent** : Modèle IA avancé  
✅ **Rapide** : Réponses instantanées  
✅ **Multilingue** : FR + EN supportés  
✅ **Contextuel** : Comprend le domaine industriel  

---

## 📊 STATISTIQUES API

| Métrique | Valeur |
|----------|--------|
| Requêtes/min | 60 |
| Coût | GRATUIT |
| Latence | ~2 secondes |
| Langues | 100+ |
| Fiabilité | 99.9% |

---

## ✅ CHECKLIST

- [ ] Clé API obtenue
- [ ] Fichier `.env` créé
- [ ] Clé copiée dans `.env`
- [ ] Frontend redémarré
- [ ] Chat IA accessible
- [ ] Question de test posée
- [ ] Réponse reçue ✅

---

## 🔗 LIENS UTILES

- **API Key** : https://aistudio.google.com/app/apikey
- **Documentation** : https://ai.google.dev/docs
- **Quota** : https://console.cloud.google.com/apis/dashboard

---

**🎉 Votre Chat IA est prêt !**

**Commande rapide** :
```bash
# Créer .env
echo "VITE_GEMINI_API_KEY=VOTRE_CLE" >> .env

# Redémarrer
npm run dev
```

**Tester** : http://localhost:5173/chat 🚀
