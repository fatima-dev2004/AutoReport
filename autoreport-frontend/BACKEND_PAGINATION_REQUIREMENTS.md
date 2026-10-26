# Exigences Backend pour la Pagination et Recherche Serveur

Ce document détaille les changements nécessaires côté backend Spring Boot pour supporter la pagination et la recherche côté serveur des rapports.

## ⚠️ Important

Le frontend a été mis à jour pour utiliser la pagination serveur au lieu de charger tous les rapports puis filtrer côté client. Le backend doit maintenant implémenter les endpoints avec pagination.

## Problème Actuel

```java
// AVANT : Retourne TOUS les rapports (ne passe pas à l'échelle)
@GetMapping("/api/reports")
public List<Report> getAllReports(@RequestParam(required = false) String search) {
    if (search != null && !search.isEmpty()) {
        return reportRepository.findByTitleContainingIgnoreCase(search);
    }
    return reportRepository.findAll();
}
```

## Solution : Pagination avec Spring Data

### 1. Modifier le Repository

```java
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Pagination simple
    Page<Report> findAll(Pageable pageable);
    
    // Recherche avec pagination
    Page<Report> findByTitleContainingIgnoreCase(String search, Pageable pageable);
    
    // Recherche multi-critères avec pagination (Specification API)
    Page<Report> findAll(Specification<Report> spec, Pageable pageable);
    
    // Filtres pour les dropdowns
    @Query("SELECT DISTINCT r.generatedByEmail FROM Report r ORDER BY r.generatedByEmail")
    List<String> findDistinctUsers();
    
    @Query("SELECT DISTINCT r.type FROM Report r ORDER BY r.type")
    List<String> findDistinctTypes();
}
```

### 2. Créer une Specification pour les Filtres Dynamiques

```java
package com.autoreport.specification;

import com.autoreport.entity.Report;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ReportSpecification {
    
    public static Specification<Report> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("generatedByEmail")), pattern),
                cb.like(cb.lower(root.get("type")), pattern)
            );
        };
    }
    
    public static Specification<Report> hasUserEmail(String userEmail) {
        return (root, query, cb) -> {
            if (userEmail == null || userEmail.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("generatedByEmail"), userEmail);
        };
    }
    
    public static Specification<Report> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }
    
    public static Specification<Report> periodStartAfter(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("periodStart"), startDate);
        };
    }
    
    public static Specification<Report> periodEndBefore(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("periodEnd"), endDate);
        };
    }
}
```

### 3. Créer un DTO pour la Réponse Paginée

```java
package com.autoreport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    private boolean first;
    private boolean last;
    
    // Constructeur depuis Spring Page
    public PageResponse(org.springframework.data.domain.Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.size = page.getSize();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
```

### 4. Créer un DTO pour les Filtres Disponibles

```java
package com.autoreport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportFiltersResponse {
    private List<String> users;
    private List<String> types;
}
```

### 5. Modifier le Controller

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private ReportRepository reportRepository;
    
    /**
     * Liste les rapports avec pagination et filtres
     * 
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @param search Recherche textuelle (titre, email, type)
     * @param userEmail Filtrer par email utilisateur
     * @param type Filtrer par type de rapport
     * @param startDate Filtrer par date de début (ISO format)
     * @param endDate Filtrer par date de fin (ISO format)
     * @param sortBy Champ de tri (default: generatedAt)
     * @param sortDir Direction du tri (asc/desc, default: desc)
     * @return Page de rapports
     */
    @GetMapping
    public ResponseEntity<PageResponse<Report>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Créer le Pageable
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Construire la specification dynamique
        Specification<Report> spec = Specification
            .where(ReportSpecification.hasSearch(search))
            .and(ReportSpecification.hasUserEmail(userEmail))
            .and(ReportSpecification.hasType(type))
            .and(ReportSpecification.periodStartAfter(startDate))
            .and(ReportSpecification.periodEndBefore(endDate));
        
        // Exécuter la requête
        Page<Report> reportPage = reportRepository.findAll(spec, pageable);
        
        // Retourner la réponse
        return ResponseEntity.ok(new PageResponse<>(reportPage));
    }
    
    /**
     * Récupère les filtres disponibles (utilisateurs et types uniques)
     * 
     * @return Listes des utilisateurs et types disponibles
     */
    @GetMapping("/filters")
    public ResponseEntity<ReportFiltersResponse> getFilters() {
        List<String> users = reportRepository.findDistinctUsers();
        List<String> types = reportRepository.findDistinctTypes();
        return ResponseEntity.ok(new ReportFiltersResponse(users, types));
    }
}
```

### 6. Configuration (si nécessaire)

Assurez-vous que JPA est bien configuré dans `application.properties` :

```properties
# Pagination par défaut
spring.data.web.pageable.default-page-size=10
spring.data.web.pageable.max-page-size=100

# Pour éviter les problèmes de lazy loading
spring.jpa.open-in-view=false
```

## Format de Réponse API

### GET /api/reports

**Paramètres de requête :**
- `page` (int, défaut: 0) - Numéro de page (0-indexed)
- `size` (int, défaut: 10) - Taille de la page
- `search` (string, optionnel) - Recherche textuelle
- `userEmail` (string, optionnel) - Filtrer par email
- `type` (string, optionnel) - Filtrer par type
- `startDate` (date, optionnel) - Date début (YYYY-MM-DD)
- `endDate` (date, optionnel) - Date fin (YYYY-MM-DD)
- `sortBy` (string, défaut: "generatedAt") - Champ de tri
- `sortDir` (string, défaut: "desc") - Direction (asc/desc)

**Réponse :**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Rapport Production - Semaine 01",
      "type": "WEEKLY",
      "periodStart": "2024-01-01",
      "periodEnd": "2024-01-07",
      "totalProduction": 15000,
      "qualityRate": 98.5,
      "generatedByEmail": "admin@autoreport.com",
      "generatedAt": "2024-01-08T10:30:00"
    }
  ],
  "totalElements": 245,
  "totalPages": 25,
  "currentPage": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

### GET /api/reports/filters

**Réponse :**
```json
{
  "users": [
    "admin@autoreport.com",
    "manager@autoreport.com",
    "user@autoreport.com"
  ],
  "types": [
    "DAILY",
    "WEEKLY",
    "MONTHLY",
    "CUSTOM"
  ]
}
```

## Exemples d'Appels

### 1. Première page (10 rapports)
```
GET /api/reports?page=0&size=10
```

### 2. Recherche "production"
```
GET /api/reports?page=0&size=10&search=production
```

### 3. Filtrer par utilisateur
```
GET /api/reports?page=0&size=10&userEmail=admin@autoreport.com
```

### 4. Filtrer par type
```
GET /api/reports?page=0&size=10&type=WEEKLY
```

### 5. Filtrer par période
```
GET /api/reports?page=0&size=10&startDate=2024-01-01&endDate=2024-01-31
```

### 6. Combinaison de filtres
```
GET /api/reports?page=0&size=10&search=production&userEmail=admin@autoreport.com&type=WEEKLY&startDate=2024-01-01&endDate=2024-12-31&sortBy=generatedAt&sortDir=desc
```

## Avantages de cette Approche

✅ **Performance**
- Ne charge que les données nécessaires (10 rapports au lieu de tous)
- Les requêtes SQL sont optimisées avec LIMIT et OFFSET
- Les index sur les colonnes filtrées améliorent la vitesse

✅ **Scalabilité**
- Fonctionne même avec des milliers de rapports
- La base de données gère le filtrage (plus efficace que JavaScript)
- Réduit la charge réseau (moins de données transférées)

✅ **Expérience Utilisateur**
- Chargement instantané des pages
- Filtres réactifs
- Navigation fluide entre les pages

✅ **Flexibilité**
- Tri dynamique sur n'importe quelle colonne
- Filtres combinables
- Taille de page configurable

## Optimisations Supplémentaires (Optionnel)

### 1. Index de Base de Données

```sql
-- Index pour améliorer les performances de recherche
CREATE INDEX idx_report_generated_by ON reports(generated_by_email);
CREATE INDEX idx_report_type ON reports(type);
CREATE INDEX idx_report_period ON reports(period_start, period_end);
CREATE INDEX idx_report_generated_at ON reports(generated_at DESC);
CREATE INDEX idx_report_title ON reports(title);
```

### 2. Cache pour les Filtres

```java
@Service
public class ReportService {
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Cacheable("reportFilters")
    public ReportFiltersResponse getFilters() {
        return new ReportFiltersResponse(
            reportRepository.findDistinctUsers(),
            reportRepository.findDistinctTypes()
        );
    }
}
```

### 3. DTO pour Optimiser le Transfert

```java
// Créer un ReportSummaryDTO sans tous les détails
@Data
public class ReportSummaryDTO {
    private Long id;
    private String title;
    private String type;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalProduction;
    private Double qualityRate;
    private String generatedByEmail;
    private LocalDateTime generatedAt;
    
    // Constructeur ou MapStruct pour conversion
}
```

## Test de l'Implémentation

### Test Unitaire (JUnit + Mockito)

```java
@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetReportsWithPagination() throws Exception {
        mockMvc.perform(get("/api/reports")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.currentPage").value(0));
    }
    
    @Test
    void testGetReportsWithSearch() throws Exception {
        mockMvc.perform(get("/api/reports")
                .param("search", "production"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
```

### Test Manuel avec cURL

```bash
# Test pagination
curl "http://localhost:8080/api/reports?page=0&size=5"

# Test recherche
curl "http://localhost:8080/api/reports?search=production"

# Test filtres
curl "http://localhost:8080/api/reports?userEmail=admin@autoreport.com&type=WEEKLY"

# Test filtres disponibles
curl "http://localhost:8080/api/reports/filters"
```

## Migration Progressive

Si vous avez déjà un système en production :

1. **Créer les nouveaux endpoints** sans casser les anciens
2. **Tester** avec Postman/cURL
3. **Déployer** le backend
4. **Déployer** le frontend (compatible avec les nouveaux endpoints)
5. **Monitorer** les performances
6. **(Optionnel)** Supprimer les anciens endpoints après validation

## Pour la Soutenance

Points à mentionner :
- ✅ "Migration de pagination client vers pagination serveur pour scalabilité"
- ✅ "Utilisation de Spring Data Specification pour filtres dynamiques"
- ✅ "Performance : charge seulement 10 rapports au lieu de tous"
- ✅ "Supporte des milliers de rapports sans ralentissement"

Démonstration :
- Montrer la différence de taille de réponse (Network tab DevTools)
- Tester avec beaucoup de données pour montrer la fluidité
- Combiner plusieurs filtres pour montrer la flexibilité
