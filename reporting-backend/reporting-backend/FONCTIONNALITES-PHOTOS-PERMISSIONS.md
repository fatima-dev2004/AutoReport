# Documentation : Photos de Profil et Permissions Granulaires

## 📋 Vue d'ensemble

Deux nouvelles fonctionnalités majeures ont été ajoutées au système de gestion des utilisateurs :

1. **Photos de profil** - Upload, stockage et récupération des photos d'utilisateurs
2. **Permissions granulaires** - Contrôle d'accès fin pour chaque fonctionnalité

---

## 📸 1. Photos de Profil

### 1.1 Fonctionnalités

- Upload de photos de profil (JPG, JPEG, PNG)
- Validation automatique (taille, format, dimensions)
- Stockage sur disque avec noms uniques
- Récupération publique des photos
- Suppression des photos

### 1.2 Configuration

Dans `application.properties` :

```properties
# Répertoire de stockage des photos
app.upload.photos-dir=uploads/photos

# Taille maximale des fichiers (5MB)
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

### 1.3 Validation

Le système valide automatiquement :
- **Format** : JPG, JPEG, PNG uniquement
- **Taille** : Maximum 5 MB
- **Dimensions** : Minimum 100x100 pixels, Maximum 4096x4096 pixels

### 1.4 API Endpoints

#### Upload d'une photo

```http
POST /api/admin/users/{id}/photo
Content-Type: multipart/form-data
Authorization: Bearer {token}

Form Data:
- photo: (fichier image)
```

**Réponse réussie (200 OK) :**
```json
{
  "message": "Photo uploadee avec succes",
  "photoUrl": "/api/users/photos/user-123-1625678901234.jpg"
}
```

**Erreur (400 Bad Request) :**
```json
{
  "error": "Format d'image non supporte. Utilisez JPG, JPEG ou PNG."
}
```

#### Récupération d'une photo

```http
GET /api/users/photos/{filename}
```

**Note** : Endpoint public, pas d'authentification requise.

**Réponse** : Image binaire avec Content-Type approprié (image/jpeg ou image/png)

#### Suppression d'une photo

```http
DELETE /api/admin/users/{id}/photo
Authorization: Bearer {token}
```

**Réponse réussie (200 OK) :**
```json
{
  "message": "Photo supprimee avec succes"
}
```

### 1.5 Exemple d'utilisation (Frontend)

#### Upload avec formulaire HTML

```html
<form id="uploadForm" enctype="multipart/form-data">
  <input type="file" name="photo" accept="image/jpeg,image/png" />
  <button type="submit">Upload Photo</button>
</form>

<script>
document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  const userId = 123; // ID de l'utilisateur
  
  const response = await fetch(`/api/admin/users/${userId}/photo`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    body: formData
  });
  
  const result = await response.json();
  console.log(result.photoUrl);
});
</script>
```

#### Affichage de la photo

```html
<img src="/api/users/photos/user-123-1625678901234.jpg" 
     alt="Photo de profil"
     style="width: 100px; height: 100px; border-radius: 50%;" />
```

---

## 🔐 2. Permissions Granulaires

### 2.1 Liste des Permissions

Le système propose 20 permissions différentes :

#### Gestion des Utilisateurs
- `CREATE_USERS` - Créer des utilisateurs
- `EDIT_USERS` - Modifier des utilisateurs
- `DELETE_USERS` - Supprimer des utilisateurs
- `VIEW_USERS` - Consulter la liste des utilisateurs
- `MANAGE_PERMISSIONS` - Gérer les permissions

#### Import et Données
- `IMPORT_EXCEL` - Importer des fichiers Excel
- `DELETE_RECORDS` - Supprimer des enregistrements de production
- `EDIT_RECORDS` - Modifier des enregistrements

#### Rapports
- `GENERATE_REPORTS` - Générer des rapports
- `VIEW_REPORTS` - Consulter des rapports
- `DOWNLOAD_REPORTS` - Télécharger des rapports
- `DELETE_REPORTS` - Supprimer des rapports
- `SCHEDULE_REPORTS` - Planifier des rapports automatiques

#### Dashboard et Analytics
- `VIEW_DASHBOARD` - Accéder au tableau de bord
- `EXPORT_DASHBOARD` - Exporter les données du dashboard

#### Configuration Système
- `VIEW_LOGS` - Consulter les logs système
- `MANAGE_SETTINGS` - Modifier les paramètres système
- `VIEW_SYSTEM_STATS` - Voir les statistiques système

#### Email
- `SEND_EMAILS` - Envoyer des emails manuellement
- `MANAGE_EMAIL_SETTINGS` - Gérer les paramètres d'email

### 2.2 Permissions par Défaut selon le Rôle

#### ADMIN
- **Toutes les permissions** (accès complet)

#### MANAGER
- `IMPORT_EXCEL`
- `GENERATE_REPORTS`
- `VIEW_REPORTS`
- `DOWNLOAD_REPORTS`
- `VIEW_DASHBOARD`
- `DELETE_REPORTS`

#### USER
- `VIEW_REPORTS`
- `VIEW_DASHBOARD`

### 2.3 API Endpoints

#### Obtenir les permissions d'un utilisateur

```http
GET /api/admin/users/{id}/permissions
Authorization: Bearer {token}
```

**Réponse (200 OK) :**
```json
[
  "IMPORT_EXCEL",
  "GENERATE_REPORTS",
  "VIEW_REPORTS",
  "VIEW_DASHBOARD"
]
```

#### Définir les permissions d'un utilisateur

```http
PUT /api/admin/users/{id}/permissions
Authorization: Bearer {token}
Content-Type: application/json

{
  "permissions": [
    "IMPORT_EXCEL",
    "GENERATE_REPORTS",
    "VIEW_REPORTS"
  ]
}
```

**Réponse (200 OK) :**
```json
{
  "message": "Permissions mises a jour avec succes",
  "permissionsCount": "3"
}
```

#### Ajouter une permission

```http
POST /api/admin/users/{id}/permissions/DOWNLOAD_REPORTS
Authorization: Bearer {token}
```

**Réponse (200 OK) :**
```json
{
  "message": "Permission ajoutee avec succes",
  "permission": "DOWNLOAD_REPORTS"
}
```

#### Retirer une permission

```http
DELETE /api/admin/users/{id}/permissions/DELETE_REPORTS
Authorization: Bearer {token}
```

**Réponse (200 OK) :**
```json
{
  "message": "Permission retiree avec succes",
  "permission": "DELETE_REPORTS"
}
```

#### Obtenir toutes les permissions disponibles

```http
GET /api/admin/users/permissions/available
Authorization: Bearer {token}
```

**Réponse (200 OK) :**
```json
[
  {
    "name": "CREATE_USERS",
    "description": "Créer des utilisateurs"
  },
  {
    "name": "IMPORT_EXCEL",
    "description": "Importer des fichiers Excel"
  },
  ...
]
```

### 2.4 Utilisation dans le Code

#### Vérifier une permission

```java
User user = userRepository.findById(userId).orElseThrow();

if (user.hasPermission(Permission.IMPORT_EXCEL)) {
    // Autoriser l'import
} else {
    throw new AccessDeniedException("Permission requise: IMPORT_EXCEL");
}
```

#### Ajouter/Retirer des permissions

```java
User user = userRepository.findById(userId).orElseThrow();

// Ajouter
user.addPermission(Permission.GENERATE_REPORTS);

// Retirer
user.removePermission(Permission.DELETE_REPORTS);

userRepository.save(user);
```

### 2.5 Exemple d'utilisation (Frontend)

#### Interface de gestion des permissions

```javascript
// Récupérer les permissions disponibles
async function loadAvailablePermissions() {
  const response = await fetch('/api/admin/users/permissions/available', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
}

// Récupérer les permissions d'un utilisateur
async function getUserPermissions(userId) {
  const response = await fetch(`/api/admin/users/${userId}/permissions`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
}

// Mettre à jour les permissions
async function updatePermissions(userId, permissions) {
  const response = await fetch(`/api/admin/users/${userId}/permissions`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ permissions })
  });
  return await response.json();
}

// Exemple d'interface
const availablePermissions = await loadAvailablePermissions();
const userPermissions = await getUserPermissions(123);

// Afficher des checkboxes
availablePermissions.forEach(perm => {
  const checked = userPermissions.includes(perm.name);
  console.log(`☑ ${perm.name}: ${perm.description} [${checked ? 'Activé' : 'Désactivé'}]`);
});

// Sauvegarder les changements
await updatePermissions(123, [
  'IMPORT_EXCEL',
  'GENERATE_REPORTS',
  'VIEW_DASHBOARD'
]);
```

---

## 🗄️ 3. Modifications de la Base de Données

### 3.1 Table `users` (modifiée)

Nouvelles colonnes ajoutées :

```sql
ALTER TABLE users ADD COLUMN profile_photo_url VARCHAR(500);
ALTER TABLE users ADD COLUMN department VARCHAR(100);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN position VARCHAR(100);
```

### 3.2 Table `user_permissions` (nouvelle)

Table de jointure pour stocker les permissions :

```sql
CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL,
    permission VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, permission)
);
```

---

## 🧪 4. Tests

### 4.1 Tester l'upload de photo

#### Avec curl

```bash
curl -X POST "http://localhost:8080/api/admin/users/1/photo" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "photo=@/path/to/photo.jpg"
```

#### Avec Postman

1. Méthode : **POST**
2. URL : `http://localhost:8080/api/admin/users/1/photo`
3. Headers : `Authorization: Bearer YOUR_TOKEN`
4. Body : **form-data**
   - Key : `photo` (type: File)
   - Value : Sélectionner une image

### 4.2 Tester les permissions

#### Avec curl

```bash
# Obtenir les permissions d'un utilisateur
curl -X GET "http://localhost:8080/api/admin/users/1/permissions" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Définir les permissions
curl -X PUT "http://localhost:8080/api/admin/users/1/permissions" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissions": ["IMPORT_EXCEL", "GENERATE_REPORTS", "VIEW_REPORTS"]
  }'

# Ajouter une permission
curl -X POST "http://localhost:8080/api/admin/users/1/permissions/DOWNLOAD_REPORTS" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Retirer une permission
curl -X DELETE "http://localhost:8080/api/admin/users/1/permissions/DELETE_REPORTS" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📦 5. Structure des Fichiers

### Nouveaux fichiers créés

```
src/main/java/com/sews/reporting/
├── entity/
│   ├── Permission.java          (NOUVEAU - Enum des permissions)
│   └── User.java                (MODIFIÉ - Ajout photo + permissions)
├── service/
│   └── UserPhotoService.java    (NOUVEAU - Gestion des photos)
├── controller/
│   ├── UserController.java      (MODIFIÉ - Endpoints photo + permissions)
│   └── PhotoController.java     (NOUVEAU - Servir les photos)
└── dto/
    ├── UpdateUserRequest.java   (NOUVEAU - DTO mise à jour utilisateur)
    └── UserPermissionsRequest.java (NOUVEAU - DTO permissions)
```

### Configuration modifiée

```
src/main/resources/
└── application.properties       (MODIFIÉ - Ajout config photos)

src/main/java/com/sews/reporting/config/
└── SecurityConfig.java          (MODIFIÉ - Accès public aux photos)
```

---

## 🚀 6. Démarrage

### 6.1 Créer le répertoire de photos

```bash
mkdir -p uploads/photos
```

Ou sous Windows :
```cmd
mkdir uploads\photos
```

### 6.2 Démarrer le backend

```bash
mvn spring-boot:run
```

Ou utiliser le script :
```bash
./start-backend.bat
```

### 6.3 Tester avec l'utilisateur admin par défaut

- **Email** : `admin@sews.com`
- **Mot de passe** : `Admin@123`

Cet utilisateur a automatiquement **toutes les permissions**.

---

## 🔍 7. Vérification des Permissions

### 7.1 Dans le Frontend

```javascript
// Récupérer l'utilisateur connecté
const currentUser = await fetch('/api/auth/me', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// Vérifier une permission
function hasPermission(permission) {
  return currentUser.permissions.includes(permission);
}

// Exemple : Afficher le bouton d'import uniquement si autorisé
if (hasPermission('IMPORT_EXCEL')) {
  document.getElementById('importBtn').style.display = 'block';
}
```

### 7.2 Dans le Backend (futur)

Pour sécuriser un endpoint avec une permission spécifique :

```java
@PostMapping("/import")
@PreAuthorize("hasAuthority('IMPORT_EXCEL')")
public ResponseEntity<?> importExcel(@RequestParam MultipartFile file) {
    // Code d'import
}
```

---

## ⚠️ 8. Sécurité

### 8.1 Photos

- ✅ Validation du format de fichier
- ✅ Limitation de taille (5 MB)
- ✅ Validation des dimensions
- ✅ Noms de fichiers uniques (évite les collisions)
- ✅ Stockage hors du classpath (uploads/)
- ✅ Accès public mais pas de listing de répertoire

### 8.2 Permissions

- ✅ Seuls les ADMIN peuvent gérer les permissions
- ✅ Les permissions sont stockées en base de données
- ✅ Attribution automatique selon le rôle
- ✅ Vérification côté serveur uniquement

### 8.3 Recommandations

1. En production, stocker les photos sur un CDN (AWS S3, Azure Blob, etc.)
2. Implémenter des quotas par utilisateur
3. Scanner les images uploadées avec un antivirus
4. Ajouter une authentification pour l'accès aux photos si nécessaire
5. Logger toutes les modifications de permissions

---

## 📊 9. Migrations de Données

Si vous avez des utilisateurs existants, leurs permissions seront automatiquement attribuées au premier login grâce à `@PrePersist` dans l'entité `User`.

Pour forcer la mise à jour :

```sql
-- Réinitialiser toutes les permissions
DELETE FROM user_permissions;

-- Les permissions seront recréées automatiquement
-- au prochain accès de chaque utilisateur
```

---

## 🎯 10. Prochaines Étapes

### Frontend à implémenter

1. **Page de gestion des utilisateurs**
   - Liste avec photos de profil
   - Bouton "Upload Photo" pour chaque utilisateur
   - Interface de gestion des permissions (checkboxes)

2. **Page de profil utilisateur**
   - Afficher la photo
   - Permettre de changer sa propre photo
   - Afficher ses permissions

3. **Contrôle d'accès dans l'interface**
   - Cacher/afficher les boutons selon les permissions
   - Afficher un message si l'utilisateur n'a pas accès

### Backend à améliorer

1. Ajouter des annotations `@PreAuthorize` sur les endpoints
2. Implémenter un système d'audit des permissions
3. Ajouter des notifications lors des changements de permissions
4. Permettre la création de "rôles personnalisés" avec des ensembles de permissions

---

## 📞 Support

Pour toute question :
- Consulter les logs : `logs/application.log`
- Vérifier la configuration : `application.properties`
- Tester les endpoints avec Postman ou curl

---

**Version** : 2.0  
**Date** : 11 juillet 2026  
**Auteur** : Système de Reporting SEWS
