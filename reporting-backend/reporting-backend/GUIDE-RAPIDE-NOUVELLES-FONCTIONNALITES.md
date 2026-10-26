# Guide Rapide : Tester les Nouvelles Fonctionnalités

## 🚀 Démarrage en 3 minutes

### Étape 1 : Préparer l'environnement

```cmd
cd "c:\Users\pc\Desktop\works\SPRING BOOT 2024\projet de stage\reporting-backend"
mkdir uploads\photos
```

### Étape 2 : Démarrer le backend

```cmd
.\start-backend.bat
```

Le backend démarre sur **http://localhost:8080**

### Étape 3 : Se connecter

Utilisez les identifiants par défaut :
- **Email** : `admin@sews.com`
- **Mot de passe** : `Admin@123`

---

## 📸 Test 1 : Upload de Photo de Profil

### Avec Postman

1. **Obtenir le token JWT**

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@sews.com",
  "password": "Admin@123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@sews.com",
    "fullName": "Administrateur",
    "role": "ADMIN"
  }
}
```

➡️ **Copier le token**

2. **Upload une photo**

```
POST http://localhost:8080/api/admin/users/1/photo
Authorization: Bearer {VOTRE_TOKEN}
```

Dans Postman :
- Onglet **Body** → **form-data**
- Key : `photo` (type : **File**)
- Value : Sélectionner une image JPG ou PNG

**Réponse attendue :**
```json
{
  "message": "Photo uploadee avec succes",
  "photoUrl": "/api/users/photos/user-1-1783722635467.jpg"
}
```

3. **Voir la photo dans le navigateur**

Ouvrir : `http://localhost:8080/api/users/photos/user-1-1783722635467.jpg`

✅ **La photo s'affiche !**

---

## 🔐 Test 2 : Gestion des Permissions

### Créer un nouvel utilisateur

```
POST http://localhost:8080/api/admin/users
Authorization: Bearer {VOTRE_TOKEN}
Content-Type: application/json

{
  "email": "manager@sews.com",
  "password": "Manager@123",
  "fullName": "Manager Test",
  "role": "MANAGER"
}
```

**Réponse :**
```json
{
  "id": 2,
  "email": "manager@sews.com",
  "fullName": "Manager Test",
  "role": "MANAGER",
  "permissions": [
    "IMPORT_EXCEL",
    "GENERATE_REPORTS",
    "VIEW_REPORTS",
    "DOWNLOAD_REPORTS",
    "VIEW_DASHBOARD",
    "DELETE_REPORTS"
  ]
}
```

➡️ **Les permissions par défaut sont automatiquement attribuées selon le rôle !**

### Voir toutes les permissions disponibles

```
GET http://localhost:8080/api/admin/users/permissions/available
Authorization: Bearer {VOTRE_TOKEN}
```

**Réponse :**
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
  {
    "name": "GENERATE_REPORTS",
    "description": "Générer des rapports"
  }
  ...
]
```

### Modifier les permissions d'un utilisateur

```
PUT http://localhost:8080/api/admin/users/2/permissions
Authorization: Bearer {VOTRE_TOKEN}
Content-Type: application/json

{
  "permissions": [
    "IMPORT_EXCEL",
    "VIEW_REPORTS",
    "VIEW_DASHBOARD"
  ]
}
```

**Réponse :**
```json
{
  "message": "Permissions mises a jour avec succes",
  "permissionsCount": "3"
}
```

### Vérifier les permissions

```
GET http://localhost:8080/api/admin/users/2/permissions
Authorization: Bearer {VOTRE_TOKEN}
```

**Réponse :**
```json
[
  "IMPORT_EXCEL",
  "VIEW_REPORTS",
  "VIEW_DASHBOARD"
]
```

✅ **Les permissions ont été modifiées !**

### Ajouter une permission

```
POST http://localhost:8080/api/admin/users/2/permissions/DOWNLOAD_REPORTS
Authorization: Bearer {VOTRE_TOKEN}
```

**Réponse :**
```json
{
  "message": "Permission ajoutee avec succes",
  "permission": "DOWNLOAD_REPORTS"
}
```

### Retirer une permission

```
DELETE http://localhost:8080/api/admin/users/2/permissions/DELETE_REPORTS
Authorization: Bearer {VOTRE_TOKEN}
```

**Réponse :**
```json
{
  "message": "Permission retiree avec succes",
  "permission": "DELETE_REPORTS"
}
```

---

## 🧪 Test 3 : Scénario Complet

### Créer un utilisateur avec photo et permissions personnalisées

#### 1. Créer l'utilisateur

```
POST http://localhost:8080/api/admin/users
Authorization: Bearer {VOTRE_TOKEN}
Content-Type: application/json

{
  "email": "operateur@sews.com",
  "password": "Operateur@123",
  "fullName": "Opérateur Production",
  "role": "USER"
}
```

Réponse : `id: 3`

#### 2. Upload sa photo

```
POST http://localhost:8080/api/admin/users/3/photo
Authorization: Bearer {VOTRE_TOKEN}
Body: form-data avec photo
```

#### 3. Lui donner des permissions spécifiques

```
PUT http://localhost:8080/api/admin/users/3/permissions
Authorization: Bearer {VOTRE_TOKEN}
Content-Type: application/json

{
  "permissions": [
    "IMPORT_EXCEL",
    "VIEW_DASHBOARD"
  ]
}
```

#### 4. Ajouter des métadonnées

```
PUT http://localhost:8080/api/admin/users/3
Authorization: Bearer {VOTRE_TOKEN}
Content-Type: application/json

{
  "fullName": "Opérateur Production",
  "department": "Production",
  "phoneNumber": "+212 600 123 456",
  "position": "Opérateur Ligne A"
}
```

#### 5. Récupérer l'utilisateur complet

```
GET http://localhost:8080/api/admin/users/3
Authorization: Bearer {VOTRE_TOKEN}
```

**Réponse complète :**
```json
{
  "id": 3,
  "email": "operateur@sews.com",
  "fullName": "Opérateur Production",
  "role": "USER",
  "enabled": true,
  "profilePhotoUrl": "/api/users/photos/user-3-1783722890123.jpg",
  "permissions": [
    "IMPORT_EXCEL",
    "VIEW_DASHBOARD"
  ],
  "department": "Production",
  "phoneNumber": "+212 600 123 456",
  "position": "Opérateur Ligne A",
  "createdAt": "2026-07-11T00:15:30"
}
```

✅ **Utilisateur complet créé avec photo et permissions !**

---

## 📋 Checklist de Validation

### Photos de Profil

- [ ] Upload d'une photo JPG → ✅ Succès
- [ ] Upload d'une photo PNG → ✅ Succès
- [ ] Upload d'un fichier PDF → ❌ Erreur (format non supporté)
- [ ] Upload d'une image > 5 MB → ❌ Erreur (trop grand)
- [ ] Récupération de la photo dans le navigateur → ✅ Affichage correct
- [ ] Suppression de la photo → ✅ Fichier supprimé

### Permissions

- [ ] Créer un ADMIN → ✅ Toutes les permissions
- [ ] Créer un MANAGER → ✅ 6 permissions par défaut
- [ ] Créer un USER → ✅ 2 permissions par défaut
- [ ] Modifier les permissions → ✅ Mise à jour réussie
- [ ] Ajouter une permission → ✅ Permission ajoutée
- [ ] Retirer une permission → ✅ Permission retirée
- [ ] Lister les permissions disponibles → ✅ 20 permissions affichées

### Métadonnées Utilisateur

- [ ] Ajouter département → ✅ Enregistré
- [ ] Ajouter téléphone → ✅ Enregistré
- [ ] Ajouter poste → ✅ Enregistré

---

## 🐛 Résolution de Problèmes

### Erreur : "Cannot upload file"

**Cause** : Le répertoire `uploads/photos` n'existe pas

**Solution** :
```cmd
mkdir uploads\photos
```

### Erreur : "File too large"

**Cause** : Image > 5 MB

**Solution** : Réduire la taille de l'image ou modifier `application.properties` :
```properties
spring.servlet.multipart.max-file-size=10MB
```

### Erreur : "Unauthorized" lors de l'upload

**Cause** : Token JWT expiré ou invalide

**Solution** : Se reconnecter pour obtenir un nouveau token

### Photo ne s'affiche pas dans le navigateur

**Cause** : Chemin incorrect ou fichier supprimé

**Solution** : Vérifier que le fichier existe dans `uploads/photos/`

---

## 📊 Exemple d'Interface Frontend

### Composant React : Photo de Profil

```jsx
import React, { useState } from 'react';

function UserPhoto({ userId, currentPhotoUrl }) {
  const [photo, setPhoto] = useState(currentPhotoUrl);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);
    
    const formData = new FormData();
    formData.append('photo', file);

    try {
      const response = await fetch(`/api/admin/users/${userId}/photo`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      });

      const result = await response.json();
      
      if (response.ok) {
        setPhoto(result.photoUrl);
        alert('Photo uploadée avec succès !');
      } else {
        alert('Erreur : ' + result.error);
      }
    } catch (error) {
      alert('Erreur de connexion au serveur');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="user-photo">
      {photo && (
        <img 
          src={photo} 
          alt="Photo de profil"
          style={{ width: 100, height: 100, borderRadius: '50%' }}
        />
      )}
      
      <input 
        type="file" 
        accept="image/jpeg,image/png"
        onChange={handleUpload}
        disabled={uploading}
      />
      
      {uploading && <p>Upload en cours...</p>}
    </div>
  );
}

export default UserPhoto;
```

### Composant React : Gestion des Permissions

```jsx
import React, { useState, useEffect } from 'react';

function PermissionsManager({ userId }) {
  const [available, setAvailable] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [userId]);

  const loadData = async () => {
    const token = localStorage.getItem('token');
    
    // Charger les permissions disponibles
    const availRes = await fetch('/api/admin/users/permissions/available', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const availData = await availRes.json();
    setAvailable(availData);
    
    // Charger les permissions de l'utilisateur
    const userRes = await fetch(`/api/admin/users/${userId}/permissions`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const userData = await userRes.json();
    setSelected(userData);
    
    setLoading(false);
  };

  const togglePermission = (permName) => {
    setSelected(prev => 
      prev.includes(permName)
        ? prev.filter(p => p !== permName)
        : [...prev, permName]
    );
  };

  const save = async () => {
    const token = localStorage.getItem('token');
    
    const response = await fetch(`/api/admin/users/${userId}/permissions`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ permissions: selected })
    });

    if (response.ok) {
      alert('Permissions mises à jour !');
    }
  };

  if (loading) return <p>Chargement...</p>;

  return (
    <div className="permissions-manager">
      <h3>Permissions</h3>
      
      {available.map(perm => (
        <label key={perm.name}>
          <input
            type="checkbox"
            checked={selected.includes(perm.name)}
            onChange={() => togglePermission(perm.name)}
          />
          {perm.description}
        </label>
      ))}
      
      <button onClick={save}>Enregistrer</button>
    </div>
  );
}

export default PermissionsManager;
```

---

## 🎯 Prochaines Actions

1. **Tester** toutes les fonctionnalités avec Postman
2. **Intégrer** dans le frontend
3. **Documenter** pour l'équipe
4. **Former** les utilisateurs

---

## 📞 Aide Rapide

### Commandes Utiles

```cmd
# Démarrer le backend
.\start-backend.bat

# Compiler
mvn clean compile

# Voir les logs
type logs\application.log

# Créer le répertoire photos
mkdir uploads\photos

# Lister les photos uploadées
dir uploads\photos
```

### URLs Importantes

- **Backend** : http://localhost:8080
- **Login** : http://localhost:8080/api/auth/login
- **Photos** : http://localhost:8080/api/users/photos/{filename}
- **Admin Users** : http://localhost:8080/api/admin/users

### Identifiants par Défaut

- **Email** : `admin@sews.com`
- **Mot de passe** : `Admin@123`
- **Rôle** : ADMIN (toutes les permissions)

---

**Félicitations ! Vous pouvez maintenant gérer les photos et permissions ! 🎉**
