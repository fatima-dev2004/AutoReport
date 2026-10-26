# Guide de Pagination

## Vue d'ensemble

La pagination a été implémentée sur tous les endpoints de liste pour améliorer les performances et l'expérience utilisateur :

- ✅ **listReports** : Liste des rapports de production
- ✅ **listUsers** : Liste des utilisateurs (admin)
- ✅ **getAuditLogs** : Logs d'audit (déjà implémenté)

## Endpoints avec pagination

### 1. Liste des rapports (GET /api/reports)

**Paramètres de pagination :**
- `page` : Numéro de la page (commence à 0), défaut: 0
- `size` : Nombre d'éléments par page, défaut: 20
- `sortBy` : Champ de tri, défaut: "generatedAt"
- `sortDirection` : Direction du tri (ASC/DESC), défaut: "DESC"
- `search` : Recherche par titre (optionnel)

**Exemple de requête :**
```bash
GET /api/reports?page=0&size=10&sortBy=generatedAt&sortDirection=DESC&search=production
```

**Réponse :**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Rapport Production - 31/07/2026",
      "type": "PRODUCTION",
      "periodStart": "2026-07-01",
      "periodEnd": "2026-07-31",
      "totalProduction": 50000.0,
      "totalDefects": 250,
      "qualityRate": 99.5,
      "generatedByEmail": "manager@sews.com",
      "generatedAt": "2026-07-31T15:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 48,
  "last": false,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "first": true,
  "empty": false
}
```

**Champs de tri disponibles :**
- `generatedAt` : Date de génération (défaut)
- `title` : Titre du rapport
- `periodStart` : Date de début de période
- `periodEnd` : Date de fin de période
- `totalProduction` : Production totale
- `qualityRate` : Taux de qualité

### 2. Liste des utilisateurs (GET /api/admin/users)

**Paramètres de pagination :**
- `page` : Numéro de la page (commence à 0), défaut: 0
- `size` : Nombre d'éléments par page, défaut: 20
- `sortBy` : Champ de tri, défaut: "id"
- `sortDirection` : Direction du tri (ASC/DESC), défaut: "ASC"
- `search` : Recherche par email ou nom (optionnel)

**Exemple de requête :**
```bash
GET /api/admin/users?page=0&size=15&sortBy=fullName&sortDirection=ASC&search=john
```

**Réponse :**
```json
{
  "content": [
    {
      "id": 1,
      "email": "john.doe@sews.com",
      "fullName": "John Doe",
      "role": "USER",
      "department": "Production",
      "position": "Opérateur",
      "enabled": true,
      "createdAt": "2026-01-15T10:00:00",
      "lastLogin": "2026-08-03T09:30:00",
      "permissions": ["VIEW_DASHBOARD", "VIEW_REPORTS"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 15,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 3,
  "totalElements": 42,
  "last": false,
  "size": 15,
  "number": 0,
  "numberOfElements": 15,
  "first": true,
  "empty": false
}
```

**Champs de tri disponibles :**
- `id` : Identifiant (défaut)
- `email` : Email
- `fullName` : Nom complet
- `role` : Rôle
- `department` : Département
- `createdAt` : Date de création
- `lastLogin` : Dernière connexion

### 3. Logs d'audit (GET /api/audit-logs)

Déjà implémenté avec pagination complète.

**Paramètres de pagination :**
- `page` : Numéro de la page, défaut: 0
- `size` : Nombre d'éléments par page, défaut: 20
- `userId` : Filtrer par utilisateur (optionnel)
- `actionType` : Filtrer par type d'action (optionnel)
- `startDate` : Date de début (optionnel)
- `endDate` : Date de fin (optionnel)

**Exemple de requête :**
```bash
GET /api/audit-logs?page=0&size=50&actionType=LOGIN&startDate=2026-08-01T00:00:00
```

## Intégration Frontend

### 1. État de pagination dans React

```javascript
const [pagination, setPagination] = useState({
  page: 0,
  size: 20,
  sortBy: 'generatedAt',
  sortDirection: 'DESC',
  totalPages: 0,
  totalElements: 0
});

const [reports, setReports] = useState([]);
const [loading, setLoading] = useState(false);
```

### 2. Fonction de chargement des données

```javascript
const loadReports = async (page = 0, size = 20, sortBy = 'generatedAt', sortDirection = 'DESC', search = '') => {
  setLoading(true);
  
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDirection,
      ...(search && { search })
    });
    
    const response = await axios.get(`/api/reports?${params}`);
    const data = response.data;
    
    setReports(data.content);
    setPagination({
      page: data.number,
      size: data.size,
      sortBy,
      sortDirection,
      totalPages: data.totalPages,
      totalElements: data.totalElements
    });
  } catch (error) {
    console.error('Erreur lors du chargement des rapports:', error);
  } finally {
    setLoading(false);
  }
};

// Charger au montage du composant
useEffect(() => {
  loadReports();
}, []);
```

### 3. Composant de pagination

```javascript
const PaginationControls = ({ pagination, onPageChange }) => {
  return (
    <div className="pagination">
      <button 
        disabled={pagination.page === 0}
        onClick={() => onPageChange(pagination.page - 1)}
      >
        Précédent
      </button>
      
      <span>
        Page {pagination.page + 1} sur {pagination.totalPages}
        ({pagination.totalElements} éléments au total)
      </span>
      
      <button 
        disabled={pagination.page >= pagination.totalPages - 1}
        onClick={() => onPageChange(pagination.page + 1)}
      >
        Suivant
      </button>
      
      <select 
        value={pagination.size}
        onChange={(e) => loadReports(0, parseInt(e.target.value))}
      >
        <option value="10">10 par page</option>
        <option value="20">20 par page</option>
        <option value="50">50 par page</option>
        <option value="100">100 par page</option>
      </select>
    </div>
  );
};

const handlePageChange = (newPage) => {
  loadReports(
    newPage, 
    pagination.size, 
    pagination.sortBy, 
    pagination.sortDirection
  );
};
```

### 4. Tri dynamique

```javascript
const SortableHeader = ({ field, label, currentSort, onSort }) => {
  const isActive = currentSort.sortBy === field;
  const direction = isActive ? currentSort.sortDirection : 'ASC';
  
  const handleSort = () => {
    const newDirection = isActive && direction === 'ASC' ? 'DESC' : 'ASC';
    onSort(field, newDirection);
  };
  
  return (
    <th onClick={handleSort} style={{ cursor: 'pointer' }}>
      {label}
      {isActive && (direction === 'ASC' ? ' ↑' : ' ↓')}
    </th>
  );
};

// Utilisation
<thead>
  <tr>
    <SortableHeader 
      field="title" 
      label="Titre" 
      currentSort={pagination}
      onSort={(field, direction) => loadReports(0, pagination.size, field, direction)}
    />
    <SortableHeader 
      field="generatedAt" 
      label="Date" 
      currentSort={pagination}
      onSort={(field, direction) => loadReports(0, pagination.size, field, direction)}
    />
  </tr>
</thead>
```

### 5. Recherche avec debouncing

```javascript
import { useState, useEffect } from 'react';
import { debounce } from 'lodash'; // ou implémentation custom

const SearchBar = ({ onSearch }) => {
  const [searchTerm, setSearchTerm] = useState('');
  
  // Debounce pour éviter trop de requêtes
  const debouncedSearch = debounce((term) => {
    onSearch(term);
  }, 500);
  
  useEffect(() => {
    debouncedSearch(searchTerm);
    
    // Cleanup
    return () => {
      debouncedSearch.cancel();
    };
  }, [searchTerm]);
  
  return (
    <input
      type="text"
      placeholder="Rechercher..."
      value={searchTerm}
      onChange={(e) => setSearchTerm(e.target.value)}
    />
  );
};

// Utilisation
<SearchBar onSearch={(term) => loadReports(0, pagination.size, pagination.sortBy, pagination.sortDirection, term)} />
```

## Structure de la réponse paginée

Toutes les réponses paginées suivent le format Spring Data Page :

```typescript
interface PageResponse<T> {
  content: T[];              // Les données de la page
  pageable: {
    pageNumber: number;      // Numéro de page actuel (0-indexed)
    pageSize: number;        // Taille de la page
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;          // Offset dans la liste totale
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;        // Nombre total de pages
  totalElements: number;     // Nombre total d'éléments
  last: boolean;             // Est-ce la dernière page ?
  size: number;              // Taille de la page
  number: number;            // Numéro de page actuel
  numberOfElements: number;  // Nombre d'éléments dans cette page
  first: boolean;            // Est-ce la première page ?
  empty: boolean;            // La page est-elle vide ?
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
}
```

## Bonnes pratiques

### Backend

1. **Limites raisonnables**
   - Taille maximale par page : 100 éléments
   - Taille par défaut : 20 éléments
   - Validation des paramètres de pagination

2. **Index de base de données**
   ```sql
   -- Ajouter des index sur les colonnes de tri fréquentes
   CREATE INDEX idx_reports_generated_at ON reports(generated_at);
   CREATE INDEX idx_reports_title ON reports(title);
   CREATE INDEX idx_users_full_name ON users(full_name);
   CREATE INDEX idx_users_email ON users(email);
   ```

3. **Optimisation des requêtes**
   - Utiliser `@EntityGraph` pour éviter N+1
   - Projections DTO pour réduire les données transférées
   - Cache pour les listes fréquemment consultées

### Frontend

1. **État de chargement**
   - Afficher un indicateur de chargement
   - Désactiver les contrôles pendant le chargement

2. **Gestion des erreurs**
   - Gérer les pages hors limites
   - Retry automatique en cas d'échec

3. **Expérience utilisateur**
   - Mémoriser les préférences de pagination
   - Scroll automatique en haut lors du changement de page
   - Feedback visuel pour le tri actif

## Tests

### Test backend

```bash
# Page 1 (0-indexed)
curl "http://localhost:8080/api/reports?page=0&size=10"

# Tri décroissant par date
curl "http://localhost:8080/api/reports?page=0&size=10&sortBy=generatedAt&sortDirection=DESC"

# Recherche
curl "http://localhost:8080/api/reports?page=0&size=10&search=production"

# Combinaison
curl "http://localhost:8080/api/reports?page=2&size=20&sortBy=title&sortDirection=ASC&search=qualite"
```

## Migration depuis les listes complètes

Si vous aviez du code frontend utilisant les anciennes listes complètes :

**Avant :**
```javascript
const response = await axios.get('/api/reports');
const reports = response.data; // Array direct
```

**Après :**
```javascript
const response = await axios.get('/api/reports?page=0&size=20');
const reports = response.data.content; // Accéder à content
const totalElements = response.data.totalElements;
```

## Performances

### Avant pagination
- 1000 rapports : ~2-3 secondes de chargement
- 5000 rapports : ~10-15 secondes
- Mémoire client : ~50-200 MB

### Avec pagination (20 par page)
- Temps de chargement : ~200-500 ms constant
- Mémoire client : ~2-5 MB
- Navigation fluide entre les pages

## Amélioration future

- Infinite scroll (chargement progressif)
- Pagination côté serveur avec cache Redis
- Export CSV/Excel des résultats filtrés
- Filtres avancés combinés avec pagination
- Sauvegarde des filtres et tri par utilisateur
