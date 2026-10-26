# Guide de Migration - Authentification Sécurisée

## Résumé des Changements

Le système d'authentification a été mis à niveau pour utiliser :
- ✅ **httpOnly Cookies** au lieu de localStorage (protection XSS)
- ✅ **Refresh Tokens** pour le renouvellement automatique
- ✅ **Rotation des tokens** pour sécurité renforcée

## Changements Frontend Effectués

### 1. `src/api/client.js`
- ✅ Ajout de `withCredentials: true` pour envoyer les cookies
- ✅ Intercepteur intelligent pour refresh automatique du token
- ✅ Gestion de la file d'attente des requêtes pendant le refresh
- ✅ Pas de boucles infinies lors des erreurs 401

### 2. `src/api/auth.js`
- ✅ Ajout de `refreshToken()` pour renouveler le token
- ✅ Ajout de `logout()` pour invalider le refresh token serveur

### 3. `src/context/AuthContext.jsx`
- ✅ Utilisation de cookies pour le vrai token
- ✅ localStorage conservé uniquement pour les infos utilisateur (UI)
- ✅ Logout appelle le backend pour invalider le token

## Changements Backend Nécessaires

Voir le fichier `BACKEND_AUTH_REQUIREMENTS.md` pour les détails complets.

### Checklist Backend

- [ ] Créer l'entité `RefreshToken`
- [ ] Créer le repository `RefreshTokenRepository`
- [ ] Créer le service `RefreshTokenService`
- [ ] Modifier `/api/auth/login` pour créer un cookie httpOnly
- [ ] Créer `/api/auth/refresh` pour renouveler les tokens
- [ ] Créer `/api/auth/logout` pour invalider les tokens
- [ ] Configurer CORS avec `allowCredentials(true)`
- [ ] Ajouter les propriétés dans `application.properties`

## Flux d'Authentification Mis à Jour

### 1. Login
```
User → POST /api/auth/login (email, password)
Backend → Vérifie credentials
Backend → Génère access token (JWT)
Backend → Génère refresh token (UUID)
Backend → Stocke refresh token en base
Backend → Envoie refresh token en cookie httpOnly
Backend → Retourne { token, email, role, fullName }
Frontend → Stocke infos user en localStorage
Frontend → Stocke access token en localStorage (fallback)
Frontend → Les requêtes utilisent le cookie automatiquement
```

### 2. Requête API avec Token Expiré
```
User → POST /api/reports/generate
Frontend → Envoie requête avec cookie
Backend → Vérifie access token → EXPIRÉ (401)
Frontend → Intercepteur détecte 401
Frontend → POST /api/auth/refresh (cookie envoyé auto)
Backend → Vérifie refresh token du cookie
Backend → Token valide → Génère nouvel access token
Backend → Envoie nouveau refresh token en cookie
Backend → Retourne nouveau access token
Frontend → Réessaye la requête originale avec nouveau token
Backend → Succès → Retourne les données
```

### 3. Logout
```
User → Clique "Déconnexion"
Frontend → POST /api/auth/logout (cookie envoyé auto)
Backend → Trouve refresh token dans cookie
Backend → Supprime refresh token de la base
Backend → Envoie cookie vide (maxAge=0)
Frontend → Supprime localStorage
Frontend → Redirige vers /login
```

## Avantages de la Nouvelle Architecture

### Sécurité
- 🔒 **Protection XSS** : Les tokens ne sont plus accessibles en JavaScript
- 🔒 **Révocation serveur** : Les tokens peuvent être invalidés côté backend
- 🔒 **Rotation** : Les refresh tokens changent à chaque utilisation
- 🔒 **Expiration courte** : Access token expire en 1h (vs 24h avant)

### Expérience Utilisateur
- ⚡ **Refresh automatique** : L'utilisateur n'est jamais déconnecté brutalement
- ⚡ **Session longue** : Reste connecté 7 jours sans redemander credentials
- ⚡ **Transparent** : Le refresh se fait en arrière-plan

### Maintenance
- 🔧 **Audit** : Possibilité de voir tous les refresh tokens actifs
- 🔧 **Déconnexion globale** : Peut révoquer tous les tokens d'un utilisateur
- 🔧 **Nettoyage auto** : Les tokens expirés sont supprimés automatiquement

## Compatibilité

### Ancien Système (localStorage uniquement)
- ✅ Continue à fonctionner si backend non mis à jour
- ⚠️ Pas de refresh automatique
- ⚠️ Vulnérable XSS

### Nouveau Système (cookies + refresh)
- ✅ Nécessite backend mis à jour
- ✅ Refresh automatique
- ✅ Sécurité renforcée

## Migration Progressive

### Phase 1 : Frontend seul (ACTUEL)
```
✅ Frontend prêt pour cookies
✅ Fallback sur localStorage si pas de cookies
⚠️ Pas de refresh automatique tant que backend non mis à jour
```

### Phase 2 : Backend déployé
```
✅ Cookies httpOnly activés
✅ Refresh automatique fonctionnel
✅ Révocation serveur activée
```

### Phase 3 : Nettoyage (optionnel, après validation)
```
✅ Supprimer localStorage complètement
✅ Cookies uniquement
✅ Sécurité maximale
```

## Test de la Migration

### 1. Vérifier le Cookie (DevTools)

Après login, ouvrir Chrome DevTools → Application → Cookies → http://localhost:8080

Vous devriez voir :
```
Name: refreshToken
Value: uuid-random-string
Path: /api/auth
HttpOnly: ✓
Secure: - (✓ en production HTTPS)
SameSite: Lax
```

### 2. Tester le Refresh Automatique

1. Se connecter
2. Attendre 30 minutes (ou réduire l'expiration du token à 1 min pour le test)
3. Faire une action (générer un rapport)
4. Vérifier dans Network → Requête automatique à `/api/auth/refresh`
5. La requête originale est réessayée automatiquement

### 3. Tester la Déconnexion

1. Se connecter
2. Se déconnecter
3. Vérifier dans DevTools que le cookie `refreshToken` a disparu
4. Vérifier que localStorage est vide
5. Tentative de requête → 401 → Redirection login

## Dépannage

### Problème : "Refresh token manquant"
- ✅ Vérifier que `withCredentials: true` est bien dans `client.js`
- ✅ Vérifier que CORS backend a `allowCredentials(true)`
- ✅ Vérifier que frontend et backend sont sur le même domaine (ou CORS configuré)

### Problème : Cookie non envoyé
- ✅ Vérifier que l'URL backend dans `.env` correspond à celle des cookies
- ✅ Vérifier que le `Path` du cookie correspond aux routes appelées
- ✅ Désactiver les bloqueurs de cookies tiers

### Problème : Boucle infinie 401
- ✅ Le système a un flag `isRefreshing` pour éviter ça
- ✅ Vérifier que l'endpoint `/refresh` ne retourne pas 401 lui-même
- ✅ Vérifier que `originalRequest._retry` empêche les doubles tentatives

## Prochaines Étapes

1. [ ] Implémenter le backend selon `BACKEND_AUTH_REQUIREMENTS.md`
2. [ ] Tester le système complet avec Postman/cURL
3. [ ] Tester l'interface frontend
4. [ ] Documenter pour la soutenance
5. [ ] (Optionnel) Ajouter device tracking, rate limiting, etc.

## Pour la Soutenance

Points à mentionner :
- ✅ "Migration de localStorage vers httpOnly cookies pour sécurité XSS"
- ✅ "Implémentation refresh token pour UX fluide et révocation serveur"
- ✅ "Rotation automatique des tokens pour sécurité renforcée"
- ✅ "Conformité aux bonnes pratiques OAuth2/JWT"

Points de vigilance :
- ⚠️ Expliquer que localStorage est gardé temporairement pour compatibilité
- ⚠️ Mentionner que HTTPS est requis en production
- ⚠️ Préciser que c'est une migration progressive (pas de breaking change)
