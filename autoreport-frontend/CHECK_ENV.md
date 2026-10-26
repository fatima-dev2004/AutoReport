# 🔍 VÉRIFIER LA CONFIGURATION

## ❌ Erreur : "API key not valid"

Cette erreur signifie que la clé API Gemini n'est **pas correctement configurée**.

---

## ✅ SOLUTION EN 3 ÉTAPES

### 1️⃣ Obtenir la Clé API

🔗 **https://aistudio.google.com/app/apikey**

1. Se connecter avec Google
2. Cliquer "Create API Key"
3. Copier la clé (format : `AIzaSy...`)

---

### 2️⃣ Vérifier le Fichier `.env`

**Ouvrir** : `autoreport-frontend/.env`

**Contenu attendu** :
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXX
```

⚠️ **IMPORTANT** :
- Pas d'espaces autour du `=`
- Pas de guillemets `"` ou `'`
- La clé doit commencer par `AIzaSy`
- **PAS** de `your_gemini_api_key_here`

---

### 3️⃣ Redémarrer le Serveur

```bash
# ARRÊTER le serveur (Ctrl+C dans le terminal)

# REDÉMARRER
npm run dev
```

⚠️ **OBLIGATOIRE** : Le serveur ne détecte pas automatiquement les changements dans `.env` !

---

## 🔍 VÉRIFICATIONS

### ✅ Checklist

- [ ] J'ai obtenu ma clé API sur https://aistudio.google.com/app/apikey
- [ ] J'ai copié la clé (commence par `AIzaSy`)
- [ ] J'ai ouvert le fichier `.env`
- [ ] J'ai remplacé `VOTRE_CLE_API_ICI` par ma vraie clé
- [ ] J'ai sauvegardé le fichier `.env`
- [ ] J'ai arrêté le serveur (Ctrl+C)
- [ ] J'ai relancé `npm run dev`
- [ ] J'ai actualisé la page (F5)

---

## 📸 EXEMPLE `.env` CORRECT

```env
# ✅ CORRECT
VITE_API_BASE_URL=http://localhost:8080
VITE_GEMINI_API_KEY=AIzaSyDfK3pL9mN8qR7tU2vW4xY6zA1bC3dE5fG

# ❌ INCORRECT (ne marche pas)
VITE_GEMINI_API_KEY=VOTRE_CLE_API_ICI
VITE_GEMINI_API_KEY="AIzaSy..."  # Pas de guillemets !
VITE_GEMINI_API_KEY = AIzaSy...  # Pas d'espaces !
```

---

## 🐛 DÉPANNAGE

### Erreur persiste après config ?

1. **Vérifier la console** (F12)
   ```
   Ctrl+Shift+I → Console
   ```
   Chercher les messages d'erreur

2. **Vérifier que le fichier est bien .env**
   - Pas `.env.txt`
   - Pas `.env.example`
   - Juste `.env`

3. **Vérifier l'emplacement**
   ```
   autoreport-frontend/
   └── .env  ← Ici !
   ```

4. **Tester la clé manuellement**
   Ouvrir : https://aistudio.google.com/app/prompts/new_chat
   Si ça marche là, votre clé est valide

---

## 💡 COMMANDES UTILES

```bash
# Afficher le contenu de .env (Windows)
type .env

# Vérifier si .env existe
dir .env

# Supprimer et recréer .env
del .env
echo VITE_API_BASE_URL=http://localhost:8080 > .env
echo VITE_GEMINI_API_KEY=VOTRE_CLE >> .env
```

---

## 🎯 TEST FINAL

Après avoir configuré :

1. **Ouvrir** : http://localhost:5173/chat
2. **Poser** : "Bonjour"
3. ✅ **Réponse** attendue : Message d'accueil de l'IA

---

## 📧 SI ÇA NE MARCHE TOUJOURS PAS

Envoyer dans le chat :
1. Contenu de votre fichier `.env` (masquer la vraie clé)
2. Message d'erreur exact dans la console
3. Screenshot de la page

---

**🔑 N'oubliez pas : Après CHAQUE modification de `.env`, il faut REDÉMARRER `npm run dev` !**
