# Guide de Sécurité des Photos de Profil

## Problème Identifié

**Avant correction :** Les photos de profil étaient accessibles publiquement via `/api/users/photos/**` avec `permitAll()` dans SecurityConfig, ce qui posait un problème de sécurité :
- Accès non authentifié aux photos
- Possibilité d'énumération des photos
- Pas de contrôle d'accès
- Risque de fuite de données personnelles

## Solution Implémentée

✅ **Accès sécurisé** : Authentification obligatoire pour accéder aux photos  
✅ **Protection contre path traversal** : Validation stricte des noms de fichiers  
✅ **Vérification du répertoire** : Les fichiers doivent être dans le répertoire autorisé  
✅ **Logging des accès** : Traçabilité des consultations de photos  
✅ **Cache HTTP** : Optimisation des performances avec cache 1 heure  

## Endpoints

### 1. Récupération d'une photo (GET /api/users/photos/{filename})

**Sécurité :**
- ✅ Authentification requise (JWT token)
- ✅ Validation du nom de fichier (pas de `..`, `/`, `\`)
- ✅ Vérification que le fichier est dans le répertoire autorisé
- ✅ Log des tentatives d'accès suspects

**Exemple de requête :**
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/users/photos/user_123_profile.jpg
```

**Réponse :**
- 200 : Photo servie avec headers de cache
- 401 : Non authentifié
- 404 : Photo introuvable
- 400 : Nom de fichier invalide

### 2. Profil utilisateur courant (GET /api/profile)

Nouveau endpoint pour permettre à un utilisateur de consulter son propre profil.

**Exemple de requête :**
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/profile
```

**Réponse :**
```json
{
  "id": 5,
  "email": "john.doe@sews.com",
  "fullName": "John Doe",
  "role": "USER",
  "department": "Production",
  "position": "Opérateur",
  "profilePhotoUrl": "/api/users/photos/user_5_profile.jpg",
  "enabled": true,
  "createdAt": "2026-01-15T10:00:00",
  "lastLogin": "2026-08-03T14:30:00",
  "permissions": ["VIEW_DASHBOARD", "VIEW_REPORTS"]
}
```

### 3. URL de la photo du profil courant (GET /api/profile/photo-url)

**Exemple de requête :**
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/profile/photo-url
```

**Réponse :**
```json
{
  "photoUrl": "/api/users/photos/user_5_profile.jpg",
  "hasPhoto": "true"
}
```

### 4. Mise à jour du profil (PUT /api/profile)

Permet à l'utilisateur de modifier ses propres informations (hors email et rôle).

**Exemple de requête :**
```bash
curl -X PUT \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Smith","phoneNumber":"0612345678","position":"Chef d'\''équipe"}' \
  http://localhost:8080/api/profile
```

## Intégration Frontend

### 1. Affichage d'une photo avec authentification

```javascript
// Composant Avatar React
const Avatar = ({ userId, photoUrl }) => {
  const [imageSrc, setImageSrc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!photoUrl) {
      setLoading(false);
      return;
    }

    const loadImage = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(photoUrl, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Failed to load image');
        }

        const blob = await response.blob();
        const objectUrl = URL.createObjectURL(blob);
        setImageSrc(objectUrl);
        setLoading(false);
      } catch (err) {
        console.error('Error loading avatar:', err);
        setError(true);
        setLoading(false);
      }
    };

    loadImage();

    // Cleanup
    return () => {
      if (imageSrc) {
        URL.revokeObjectURL(imageSrc);
      }
    };
  }, [photoUrl]);

  if (loading) {
    return <div className="avatar-skeleton">Chargement...</div>;
  }

  if (error || !imageSrc) {
    return (
      <div className="avatar-placeholder">
        {userId ? userId.toString().charAt(0).toUpperCase() : '?'}
      </div>
    );
  }

  return <img src={imageSrc} alt="Avatar" className="avatar" />;
};
```

### 2. Alternative : Image avec token dans l'URL (non recommandé)

⚠️ **Déconseillé** car expose le token dans l'URL

```javascript
// NE PAS UTILISER EN PRODUCTION
const Avatar = ({ photoUrl }) => {
  const token = localStorage.getItem('accessToken');
  const authenticatedUrl = `${photoUrl}?token=${token}`;
  
  return <img src={authenticatedUrl} alt="Avatar" />;
};
```

### 3. Upload de photo via le profil

```javascript
const uploadProfilePhoto = async (file) => {
  const formData = new FormData();
  formData.append('photo', file);

  const token = localStorage.getItem('accessToken');
  const userId = getUserIdFromToken(token);

  try {
    const response = await axios.post(
      `/api/admin/users/${userId}/photo`,
      formData,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      }
    );

    console.log('Photo uploaded:', response.data);
    return response.data.photoUrl;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
};
```

### 4. Composant complet de gestion du profil

```javascript
const ProfileManager = () => {
  const [profile, setProfile] = useState(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await axios.get('/api/profile', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });
      setProfile(response.data);
    } catch (error) {
      console.error('Failed to load profile:', error);
    }
  };

  const handlePhotoUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validation
    if (!file.type.startsWith('image/')) {
      alert('Veuillez sélectionner une image');
      return;
    }

    if (file.size > 5 * 1024 * 1024) { // 5MB
      alert('L\'image ne doit pas dépasser 5MB');
      return;
    }

    setUploading(true);
    try {
      const photoUrl = await uploadProfilePhoto(file);
      setProfile({ ...profile, profilePhotoUrl: photoUrl });
      alert('Photo mise à jour avec succès');
    } catch (error) {
      alert('Erreur lors de l\'upload de la photo');
    } finally {
      setUploading(false);
    }
  };

  if (!profile) return <div>Chargement...</div>;

  return (
    <div className="profile-manager">
      <div className="profile-header">
        <Avatar userId={profile.id} photoUrl={profile.profilePhotoUrl} />
        <h2>{profile.fullName}</h2>
        <p>{profile.email}</p>
      </div>

      <div className="photo-upload">
        <label htmlFor="photo-input" className="upload-button">
          {uploading ? 'Upload en cours...' : 'Changer la photo'}
        </label>
        <input
          id="photo-input"
          type="file"
          accept="image/*"
          onChange={handlePhotoUpload}
          disabled={uploading}
          style={{ display: 'none' }}
        />
      </div>

      {/* Autres informations du profil */}
    </div>
  );
};
```

## Sécurité Backend

### Protection contre path traversal

```java
// Validation dans PhotoController
if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
    log.warn("Tentative de path traversal détectée : {}", filename);
    return ResponseEntity.badRequest().build();
}

// Vérification du répertoire
if (!filePath.startsWith(Paths.get(photosDirectory).toAbsolutePath())) {
    log.warn("Tentative d'accès à un fichier hors du répertoire autorisé");
    return ResponseEntity.badRequest().build();
}
```

### Logging des accès suspects

```java
// Dans PhotoController
log.warn("Tentative d'accès non autorisée à la photo : {}", filename);
log.warn("Tentative de path traversal détectée : {}", filename);
log.warn("Photo introuvable ou non lisible : {}", filename);
```

### Headers de sécurité

```java
return ResponseEntity.ok()
    .contentType(MediaType.parseMediaType(contentType))
    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache 1 heure
    .body(resource);
```

## Configuration

### application.properties

```properties
# Répertoire des photos de profil
app.upload.photos-dir=profile-photos

# Taille maximale des fichiers
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

### SecurityConfig

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/users/photos/**").authenticated() // ✅ SÉCURISÉ
    .requestMatchers("/api/profile/**").authenticated()
    // ...
)
```

## Tests de sécurité

### 1. Test d'accès non authentifié

```bash
# Devrait retourner 401
curl http://localhost:8080/api/users/photos/user_1_profile.jpg
```

### 2. Test de path traversal

```bash
# Devrait retourner 400
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/users/photos/../../../etc/passwd
```

### 3. Test d'accès authentifié

```bash
# Devrait retourner 200
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/users/photos/user_1_profile.jpg
```

## Migration Frontend

Si votre frontend utilisait les photos sans authentification :

**Avant :**
```html
<img src="/api/users/photos/user_1_profile.jpg" alt="Avatar" />
```

**Après :**
```javascript
// Utiliser le composant Avatar qui gère l'authentification
<Avatar userId={1} photoUrl="/api/users/photos/user_1_profile.jpg" />
```

## Bonnes Pratiques

1. **Toujours valider les noms de fichiers** pour éviter path traversal
2. **Logger les tentatives d'accès suspects** pour détecter les attaques
3. **Utiliser HTTPS** en production pour protéger les tokens
4. **Implémenter un cache HTTP** pour réduire les requêtes
5. **Limiter la taille des photos** pour éviter les abus
6. **Nettoyer les anciennes photos** lors de la suppression d'utilisateurs
7. **Utiliser des noms de fichiers hachés** pour éviter les collisions

## Améliorations Futures

- [ ] CDN pour servir les photos statiques
- [ ] Compression automatique des images
- [ ] Miniatures (thumbnails) générées automatiquement
- [ ] Détection de contenu inapproprié via IA
- [ ] Rotation automatique des images selon EXIF
- [ ] Watermarking pour la protection des photos
- [ ] Rate limiting sur l'upload de photos
