# Guide des Tests Unitaires

## Structure des Tests

```
src/test/java/com/sews/reporting/
├── controller/
│   └── AuthControllerTest.java          # Tests d'intégration du contrôleur d'authentification
├── security/
│   └── JwtUtilTest.java                 # Tests de la gestion JWT
└── service/
    ├── ExcelImportServiceTest.java      # Tests du service d'import Excel
    └── ReportGenerationServiceTest.java # Tests de génération de rapports
```

## Couverture des Tests

### ✅ ExcelImportServiceTest (8 tests)
- Import de fichiers Excel valides
- Détection de formats invalides
- Gestion des fichiers vides
- Gestion des erreurs de données
- Calcul automatique des KPI
- Détection de colonnes manquantes
- Validation robuste des données

### ✅ ReportGenerationServiceTest (7 tests)
- Génération de rapports avec données valides
- Génération de rapports améliorés avec page de garde
- Rapports vides (aucune donnée)
- Calcul précis du taux de qualité
- Agrégation multi-lignes de production
- Création des fichiers PDF et Excel
- Association correcte des métadonnées

### ✅ AuthControllerTest (6 tests)
- Login avec credentials valides
- Validation des formats d'email
- Validation des mots de passe
- Gestion des champs null
- Gestion des credentials invalides
- Vérification du Content-Type

### ✅ JwtUtilTest (9 tests)
- Génération de tokens JWT
- Extraction de l'email du token
- Extraction du rôle du token
- Validation de tokens valides
- Rejet de tokens avec mauvais email
- Rejet de tokens expirés
- Préservation des rôles
- Gestion des tokens invalides
- Unicité des tokens

## Exécution des Tests

### Exécuter tous les tests
```bash
mvn test
```

### Exécuter une classe de test spécifique
```bash
mvn test -Dtest=ExcelImportServiceTest
mvn test -Dtest=ReportGenerationServiceTest
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=JwtUtilTest
```

### Exécuter un test spécifique
```bash
mvn test -Dtest=ExcelImportServiceTest#testImportValidFile_ShouldSucceed
```

### Générer un rapport de couverture
```bash
mvn clean test jacoco:report
```
Le rapport sera disponible dans `target/site/jacoco/index.html`

## Configuration

Les tests utilisent une configuration dédiée définie dans :
- `src/test/resources/application-test.properties`

Caractéristiques :
- Base de données H2 en mémoire (testdb)
- JWT avec secret de test
- Emails désactivés
- Répertoire temporaire pour rapports
- Logging DEBUG activé

## Données de Test

### Fichiers Excel de test (à créer)
Les tests utilisent des fichiers Excel dans `src/test/resources/test-data/` :
- `production_valid.xlsx` - Données valides complètes
- `production_empty.xlsx` - Fichier vide
- `production_with_errors.xlsx` - Données avec quelques erreurs
- `production_missing_columns.xlsx` - Colonnes manquantes

**Note :** Ces fichiers doivent être créés manuellement ou via un script pour que tous les tests s'exécutent.

## Mocking

Les tests utilisent Mockito pour simuler les dépendances :
- `@Mock` : Création de mocks
- `@InjectMocks` : Injection automatique dans la classe testée
- `@MockBean` : Mocks pour les tests d'intégration Spring

## Bonnes Pratiques

1. **Isolation** : Chaque test est indépendant
2. **Nommage** : Format `test[Méthode]_[Condition]_Should[Résultat]`
3. **AAA Pattern** : Arrange, Act, Assert
4. **Cleanup** : Utilisation de `@BeforeEach` et `@TempDir`
5. **Assertions claires** : Messages explicites

## Ajout de Nouveaux Tests

Pour ajouter des tests :

1. Créer une nouvelle classe dans le bon package
2. Annoter avec `@ExtendWith(MockitoExtension.class)` ou `@WebMvcTest`
3. Suivre le pattern AAA (Arrange, Act, Assert)
4. Documenter ce qui est testé dans la Javadoc

## Statistiques

- **Total de tests** : 30+
- **Services testés** : 4
- **Couverture cible** : 70%+
- **Temps d'exécution** : < 30 secondes

## Problèmes Connus

Si certains tests échouent à cause des fichiers Excel manquants, vous pouvez :
1. Créer les fichiers manuellement avec Excel
2. Commenter temporairement les tests concernés
3. Utiliser une bibliothèque comme Apache POI pour générer les fichiers dans `@BeforeAll`

## Prochaines Étapes

Tests à ajouter (recommandé pour soutenance) :
- Tests pour DashboardService
- Tests pour UserService avec permissions
- Tests pour AuditLogService
- Tests pour EmailService (avec JavaMailSender mock)
- Tests de sécurité pour SecurityConfig
