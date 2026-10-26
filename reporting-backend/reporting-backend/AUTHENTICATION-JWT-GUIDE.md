# Guide d'Authentification JWT avec Refresh Token

## Vue d'ensemble

Le système d'authentification a été amélioré pour inclure :
- ✅ **Refresh tokens** pour des sessions prolongées
- ✅ **Déconnexion sécurisée** avec révocation des tokens
- ✅ **Blacklist JWT** pour invalider les tokens après logout
- ✅ **Nettoyage automatique** des tokens expirés

## Architecture

### Composants

1. **Access Token (JWT)**
   - Durée de vie : 24 heures (configurable via `app.jwt.expiration-ms`)
   - Utilisé pour toutes les requêtes API
   - Stocké côté client (localStorage/sessionStorage)

2. **Refresh Token**
   - Durée de vie : 7 jours (configurable via `app.jwt.refresh-expiration-ms`)
   - Stocké en base de données
   - Permet de renouveler l'access token sans redemander les credentials

3. **Blacklist**
   - Tokens révoqués après logout
   - Nettoyage automatique des tokens expirés
   - Implémentation en mémoire (pour production : utiliser Redis)

## Endpoints API

### 1. Login (POST /api/auth/login)

**Request:**
```json
{
  "email": "admin@sews.com",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "email": "admin@sews.com",
  "role": "ADMIN",
  "fullName": "Admin User",
  "expiresIn": 86400
}
```

**Nouveautés :**
- `refreshToken` : Token pour renouveler l'access token
- `expiresIn` : Durée de validité en secondes

### 2. Refresh Token (POST /api/auth/refresh)

Renouvelle l'access token sans redemander les credentials.

**Request:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "email": "admin@sews.com",
  "role": "ADMIN",
  "fullName": "Admin User",
  "expiresIn": 86400
}
```

**Cas d'erreur :**
- 401 : Token invalide, expiré ou révoqué

### 3. Logout (POST /api/auth/logout)

Déconnexion sécurisée avec révocation des tokens.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request (optionnel):**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "message": "Déconnexion réussie"
}
```

**Actions effectuées :**
1. Access token ajouté à la blacklist
2. Refresh token révoqué en base
3. Log d'audit créé

## Intégration Frontend

### 1. Stockage des tokens

```javascript
// Après login réussi
const response = await axios.post('/api/auth/login', credentials);
const { token, refreshToken, expiresIn } = response.data;

// Stocker les tokens
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);
localStorage.setItem('tokenExpiry', Date.now() + (expiresIn * 1000));
```

### 2. Intercepteur pour refresh automatique

```javascript
// Axios interceptor
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Si 401 et pas déjà tenté de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/auth/refresh', { refreshToken });
        
        const { token, expiresIn } = response.data;
        localStorage.setItem('accessToken', token);
        localStorage.setItem('tokenExpiry', Date.now() + (expiresIn * 1000));

        // Rejouer la requête originale avec le nouveau token
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

### 3. Déconnexion

```javascript
const logout = async () => {
  const accessToken = localStorage.getItem('accessToken');
  const refreshToken = localStorage.getItem('refreshToken');

  try {
    await axios.post('/api/auth/logout', 
      { refreshToken },
      { headers: { Authorization: `Bearer ${accessToken}` } }
    );
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    // Nettoyer le stockage local dans tous les cas
    localStorage.clear();
    window.location.href = '/login';
  }
};
```

### 4. Vérification proactive de l'expiration

```javascript
// Vérifier avant chaque requête si le token va expirer bientôt
const checkTokenExpiry = async () => {
  const expiry = localStorage.getItem('tokenExpiry');
  const now = Date.now();
  
  // Si le token expire dans moins de 5 minutes, le renouveler
  if (expiry && (now + 300000) > parseInt(expiry)) {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      const response = await axios.post('/api/auth/refresh', { refreshToken });
      
      const { token, expiresIn } = response.data;
      localStorage.setItem('accessToken', token);
      localStorage.setItem('tokenExpiry', Date.now() + (expiresIn * 1000));
    } catch (error) {
      console.error('Token refresh failed:', error);
      // Rediriger vers login
      logout();
    }
  }
};

// Appeler régulièrement (par exemple toutes les minutes)
setInterval(checkTokenExpiry, 60000);
```

## Configuration

### application.properties

```properties
# Access token : 24 heures (86400000 ms)
app.jwt.expiration-ms=86400000

# Refresh token : 7 jours (604800000 ms)
app.jwt.refresh-expiration-ms=604800000

# Secret JWT (minimum 256 bits pour HS256)
app.jwt.secret=YourSecretKeyHere
```

### Recommandations Production

1. **Redis pour la blacklist**
   ```java
   @Service
   public class RedisJwtBlacklistService {
       @Autowired
       private RedisTemplate<String, String> redisTemplate;
       
       public void blacklistToken(String token, LocalDateTime expiry) {
           long ttl = Duration.between(LocalDateTime.now(), expiry).getSeconds();
           redisTemplate.opsForValue().set("blacklist:" + token, "1", ttl, TimeUnit.SECONDS);
       }
   }
   ```

2. **HTTPS obligatoire** en production pour protéger les tokens

3. **Durées de vie adaptées** :
   - Access token : 15-30 minutes pour plus de sécurité
   - Refresh token : 7-30 jours selon le cas d'usage

4. **Rate limiting** sur /login et /refresh (voir tâche #10)

## Base de Données

### Table refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index pour améliorer les performances
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_expiry_date ON refresh_tokens(expiry_date);
```

## Nettoyage Automatique

Un job planifié nettoie les tokens expirés tous les jours à 3h :

```java
@Scheduled(cron = "0 0 3 * * ?")
public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
}
```

## Tests

### Test de login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sews.com","password":"Admin@123"}'
```

### Test de refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<your-refresh-token>"}'
```

### Test de logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<your-refresh-token>"}'
```

## Sécurité

✅ **Implémenté :**
- Refresh tokens stockés en base avec date d'expiration
- Blacklist des access tokens après logout
- Révocation des tokens en cas de déconnexion
- Nettoyage automatique des tokens expirés
- Validation complète des tokens à chaque requête
- Log d'audit pour login/logout

⚠️ **À améliorer pour production :**
- Utiliser Redis pour la blacklist (scalabilité multi-instances)
- Implémenter rate limiting (voir tâche #10)
- Rotation des refresh tokens à chaque usage
- Détection d'anomalies (plusieurs sessions simultanées, changement d'IP brutal)
- Déconnexion forcée de toutes les sessions d'un utilisateur

## Troubleshooting

### "Refresh token invalide ou expiré"
- Vérifier que le refresh token n'a pas été révoqué
- Vérifier la date d'expiration en base
- Forcer une nouvelle connexion

### "Token révoqué. Veuillez vous reconnecter."
- L'access token a été blacklisté après un logout
- Forcer une nouvelle connexion

### Les tokens ne sont pas nettoyés
- Vérifier que `@EnableScheduling` est présent sur `ReportingBackendApplication`
- Vérifier les logs pour les erreurs de tâche planifiée
