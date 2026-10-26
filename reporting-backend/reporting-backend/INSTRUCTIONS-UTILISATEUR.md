# 👤 Instructions pour l'Utilisateur

## 🎯 Ce qui a été corrigé

✅ **Le module "Objectives" manquant a été créé !**

Votre backend est maintenant opérationnel avec :
- Endpoint `/api/objectives` fonctionnel
- 5 objectifs de test créés automatiquement
- Toutes les permissions configurées

---

## 🚀 Actions Immédiates

### 1️⃣ Rafraîchir la Page Objectives

Dans votre navigateur :
1. Aller sur http://localhost:5173/objectives (ou le port de votre frontend)
2. **Appuyer sur F5** pour rafraîchir la page
3. Les 5 objectifs devraient maintenant s'afficher ! 🎉

**Objectifs créés :**
- Ligne A - MONTHLY : 50 000 unités (Août 2026)
- Ligne B - MONTHLY : 45 000 unités (Août 2026)
- Ligne C - MONTHLY : 40 000 unités (Août 2026)
- Ligne A - WEEKLY : 12 500 unités (Semaine 1)
- Ligne B - WEEKLY : 11 000 unités (Semaine 1)

---

### 2️⃣ Vérifier les Autres Pages

#### Page `/reports`
1. Ouvrir http://localhost:5173/reports
2. Si **page blanche**, faire ceci :
   - Appuyer sur **F12** (ouvrir DevTools)
   - Cliquer sur l'onglet **"Console"**
   - **Copier toutes les erreurs en rouge** et me les envoyer

#### Page `/settings` (Paramètres)
1. **Se déconnecter** de votre compte actuel
2. **Se reconnecter** avec :
   - Email : `admin@sews.com`
   - Mot de passe : `Admin@123`
3. Aller sur `/settings`
4. Si toujours "Access Denied", me le signaler

---

### 3️⃣ Configurer le Chat IA (Optionnel)

Le Chat IA nécessite une clé API Google Gemini.

#### Option A : Obtenir une Clé Gratuite
1. Aller sur https://makersuite.google.com/app/apikey
2. Se connecter avec un compte Google
3. Cliquer sur "Create API Key"
4. Copier la clé (format : `AIzaSy...`)

#### Option B : Créer le Fichier .env
1. Aller dans le **dossier frontend** (pas backend !)
2. Créer un fichier nommé `.env` (avec le point devant)
3. Y ajouter :
```
REACT_APP_GEMINI_API_KEY=AIzaSyVotreCleIci
REACT_APP_BACKEND_URL=http://localhost:8080
```
4. **Redémarrer le frontend** :
   - Fermer le terminal frontend (Ctrl+C)
   - Relancer : `npm start`

#### Option C : Ignorer pour l'instant
Si vous n'avez pas besoin du Chat IA maintenant, vous pouvez l'ignorer.

---

## 🔍 Debug : Si une Page est Blanche

### Étapes de Diagnostic

1. **Ouvrir DevTools** :
   - Appuyer sur **F12**
   - OU Clic droit → "Inspecter"

2. **Onglet Console** :
   - Regarder les messages rouges (erreurs)
   - Copier le texte complet des erreurs

3. **Onglet Network** :
   - Rafraîchir la page (F5)
   - Regarder les lignes rouges
   - Cliquer dessus → Onglet "Response"
   - Copier la réponse

4. **Me communiquer** :
   - Les erreurs de la Console
   - Les appels API qui échouent (status 400, 403, 404, 500)
   - L'URL de la page problématique

---

## ✅ Vérifications Rapides

### Backend Fonctionne ?
Ouvrir un terminal PowerShell :
```powershell
curl http://localhost:8080/api/auth/login -UseBasicParsing
```
- **Attendu** : Erreur 403 ou 400 (c'est normal, on n'a pas envoyé de credentials)
- **Problème** : "Impossible de se connecter" → Backend arrêté

### Login Fonctionne ?
```powershell
$body = '{"email":"admin@sews.com","password":"Admin@123"}'
curl http://localhost:8080/api/auth/login -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
```
- **Attendu** : JSON avec "token", "userId", etc.
- **Problème** : Erreur 401 → Mauvais mot de passe

### Objectives Fonctionnent ?
D'abord login (commande ci-dessus), puis :
```powershell
# Récupérer le token de la réponse précédente
$token = "VOTRE_TOKEN_ICI"
curl "http://localhost:8080/api/objectives?page=0&size=10" -Headers @{Authorization="Bearer $token"} -UseBasicParsing
```
- **Attendu** : JSON avec "totalElements":5, liste des 5 objectifs
- **Problème** : 403 → Token invalide

---

## 📋 Comptes de Test Disponibles

Le backend a créé automatiquement 3 comptes :

### 1. Admin (Tous les droits)
- **Email** : `admin@sews.com`
- **Password** : `Admin@123`
- **Accès** : Dashboard, Rapports, Utilisateurs, Paramètres, Objectives, Import, Planification

### 2. Manager (Gestion département)
- **Email** : `manager@sews.com`
- **Password** : `Manager@123`
- **Accès** : Dashboard, Rapports (son département), Objectives, Import

### 3. User (Consultation)
- **Email** : `user@sews.com`
- **Password** : `User@123`
- **Accès** : Dashboard, Rapports (son département), Objectives (lecture seule)

---

## 🎯 Résumé des Actions

### ✅ À Faire Maintenant
1. Rafraîchir `/objectives` → Vérifier que les 5 objectifs apparaissent
2. Tester `/reports` → Si blanc, F12 Console et me copier les erreurs
3. Tester `/settings` avec `admin@sews.com`

### 🟡 Optionnel
4. Configurer Gemini API pour le Chat IA

### 📞 Si Problème
5. M'envoyer les erreurs de la Console (F12)
6. M'envoyer les appels API qui échouent (Network tab)

---

## 📞 Besoin d'Aide ?

Si vous rencontrez d'autres problèmes :

1. **Copier** les erreurs complètes (Console + Network)
2. **Noter** l'URL de la page problématique
3. **Vérifier** si le backend tourne (commande curl ci-dessus)
4. **Me fournir** ces informations

---

## 🎉 Conclusion

**Le backend est 100% opérationnel !**

Les problèmes de la page Objectives sont corrigés. Pour les autres pages blanches, j'ai besoin de voir les erreurs dans la Console du navigateur pour vous aider davantage.

**Bon courage ! 🚀**

---

*Instructions créées le : 3 Août 2026, 19:25*  
*Backend : ✅ Opérationnel sur port 8080*  
*Objectives : ✅ 5 données de test créées*
