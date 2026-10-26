# Intégration SIGIP - Architecture Extensible

## 📋 Vue d'ensemble

Ce projet implémente une **architecture extensible** permettant l'intégration future avec le système SIGIP (DB2 IBM i) tout en fonctionnant actuellement avec des données simulées.

Conforme au **CDC Section 8 - Évolutions possibles** : "Connexion au système SIGIP pour synchronisation automatique des données"

---

## 🏗️ Architecture

### Interface principale : `ExternalDataSource`

```
┌─────────────────────────────────────┐
│   ExternalDataSource (Interface)   │
├─────────────────────────────────────┤
│ + getSourceName()                   │
│ + testConnection()                  │
│ + fetchProductionData()             │
│ + synchronizeData()                 │
│ + isAvailable()                     │
└─────────────────────────────────────┘
              ▲
              │ implements
      ┌───────┴───────┐
      │               │
┌─────┴────────┐  ┌──┴──────────────┐
│ MockSigip    │  │ SigipDB2        │
│ DataSource   │  │ DataSource      │
│ (dev/demo)   │  │ (production)    │
└──────────────┘  └─────────────────┘
```

---

## 📂 Structure des fichiers

```
src/main/java/com/sews/reporting/
├── integration/
│   ├── ExternalDataSource.java           # Interface principale
│   ├── ExternalDataSourceException.java  # Exception custom
│   ├── MockSigipDataSource.java         # Implémentation MOCK (active par défaut)
│   └── SigipDB2DataSource.java          # Implémentation DB2 (stub, à implémenter)
├── service/
│   └── IntegrationService.java          # Service de gestion des intégrations
└── controller/
    └── IntegrationController.java       # API REST pour admin
```

---

## 🔧 Implémentations

### 1. MockSigipDataSource (Développement)

**Profils actifs** : `dev`, `demo`, `default`

**Caractéristiques** :
- Génère des données de production **réalistes** et aléatoires
- Simule des événements exceptionnels (pannes, pics de défauts)
- Simule les délais réseau
- **Aucune dépendance externe** requise

**Utilisation** :
```java
@Profile({"dev", "demo", "default"})
@Service
public class MockSigipDataSource implements ExternalDataSource {
    // Génère 4 lignes x N jours de données
    // Production : 800-1200 unités/jour
    // Défauts : 2-5% normalement, pics occasionnels
}
```

### 2. SigipDB2DataSource (Production)

**Profil actif** : `production`

**État** : **STUB** (squelette prêt, implémentation à compléter)

**Configuration requise** dans `application-production.properties` :
```properties
sigip.db2.host=hostname_ibm_i
sigip.db2.port=50000
sigip.db2.database=SIGIP
sigip.db2.username=${SIGIP_USER}
sigip.db2.password=${SIGIP_PASSWORD}
sigip.db2.schema=PRODUCTION
```

**Dépendance Maven à ajouter** (quand prêt pour prod) :
```xml
<dependency>
    <groupId>com.ibm.db2</groupId>
    <artifactId>jcc</artifactId>
    <version>11.5.8.0</version>
</dependency>
```

**URL JDBC DB2** :
```
jdbc:as400://hostname:port/database
ou
jdbc:db2://hostname:port/database
```

---

## 🚀 API REST

### Endpoints Admin (ADMIN uniquement)

#### 1. Lister les sources de données
```http
GET /api/admin/integrations
```

**Réponse** :
```json
{
  "sources": [
    {
      "name": "SIGIP Mock (Développement)",
      "type": "SIGIP DB2 IBM i (Simulé)",
      "available": true,
      "metadata": {
        "host": "localhost (mock)",
        "database": "SIGIP_DEV",
        "status": "CONNECTED (Simulé)",
        "enabled": true
      }
    }
  ],
  "activeSource": "SIGIP Mock (Développement)",
  "totalSources": 1
}
```

#### 2. Tester la connexion
```http
GET /api/admin/integrations/test
```

**Réponse** :
```json
{
  "success": true,
  "source": "SIGIP Mock (Développement)",
  "systemType": "SIGIP DB2 IBM i (Simulé)",
  "durationMs": 102,
  "message": "Connexion établie avec succès"
}
```

#### 3. Synchroniser les données
```http
POST /api/admin/integrations/sync?start=2024-01-01&end=2024-01-31
```

**Réponse** :
```json
{
  "success": true,
  "source": "SIGIP Mock (Développement)",
  "recordsImported": 124,
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  },
  "durationMs": 356,
  "message": "124 enregistrements importés avec succès"
}
```

#### 4. Informations système
```http
GET /api/admin/integrations/info
```

---

## 🔄 Flux de Synchronisation

```
┌──────────┐                  ┌──────────────┐                 ┌──────────┐
│  Admin   │                  │ Integration  │                 │  SIGIP   │
│   UI     │                  │   Service    │                 │ (Mock)   │
└────┬─────┘                  └──────┬───────┘                 └────┬─────┘
     │                               │                              │
     │ POST /sync?start=...&end=...  │                              │
     ├──────────────────────────────>│                              │
     │                               │ getActiveDataSource()        │
     │                               ├────────┐                     │
     │                               │        │                     │
     │                               │<───────┘                     │
     │                               │                              │
     │                               │ fetchProductionData(start,end)
     │                               ├─────────────────────────────>│
     │                               │                              │
     │                               │  List<ProductionRecord>      │
     │                               │<─────────────────────────────┤
     │                               │                              │
     │                               │ deleteExistingRecords()      │
     │                               ├────────┐                     │
     │                               │        │                     │
     │                               │<───────┘                     │
     │                               │                              │
     │                               │ saveAll(records)             │
     │                               ├────────┐                     │
     │                               │        │                     │
     │                               │<───────┘                     │
     │                               │                              │
     │  { recordsImported: 124 }     │                              │
     │<──────────────────────────────┤                              │
     │                               │                              │
```

---

## 🎯 Avantages de l'architecture

### ✅ Extensibilité
- Nouvelle source de données = nouvelle implémentation de `ExternalDataSource`
- Exemples possibles : SAP, Oracle, CSV FTP, API REST, etc.

### ✅ Testabilité
- Mock intégré pour développement et tests
- Pas besoin de DB2 pour développer/tester

### ✅ Découplage
- Le code métier ne dépend **que** de l'interface
- Changement de source = changement de profil Spring

### ✅ Production-ready
- Le stub DB2 contient toute la structure nécessaire
- Implémentation SQL commentée et prête à être activée

---

## 📝 TODO pour migration SIGIP réelle

### Étape 1 : Configuration
1. Créer `application-production.properties`
2. Ajouter les paramètres de connexion DB2
3. Créer variables d'environnement : `SIGIP_USER`, `SIGIP_PASSWORD`

### Étape 2 : Dépendances
```xml
<dependency>
    <groupId>com.ibm.db2</groupId>
    <artifactId>jcc</artifactId>
    <version>11.5.8.0</version>
</dependency>
```

### Étape 3 : Implémentation DB2
Dans `SigipDB2DataSource.java`, décommenter et adapter :
- La méthode `testConnection()`
- La méthode `fetchProductionData()` avec la vraie requête SQL
- La configuration du `DataSource`

### Étape 4 : Mapping des colonnes
Adapter le mapping entre les colonnes DB2 SIGIP et l'entité `ProductionRecord` :
```java
ProductionRecord record = ProductionRecord.builder()
    .productionDate(rs.getDate("PROD_DATE").toLocalDate())
    .line(rs.getString("LINE_CODE"))
    .product(rs.getString("PRODUCT_CODE"))
    .quantity(rs.getDouble("QUANTITY"))
    .defects(rs.getInt("DEFECTS"))
    .operator(rs.getString("OPERATOR_ID"))
    .shift(rs.getString("SHIFT_CODE"))
    .build();
```

### Étape 5 : Tests
1. Tester la connexion : `GET /api/admin/integrations/test`
2. Synchroniser une petite période : `POST /api/admin/integrations/sync?start=2024-01-01&end=2024-01-02`
3. Vérifier les données dans la BDD locale
4. Planifier des synchronisations automatiques (Spring `@Scheduled`)

---

## 🔐 Sécurité

- ❌ **Jamais** stocker les credentials en dur
- ✅ Utiliser des variables d'environnement : `${SIGIP_USER}`, `${SIGIP_PASSWORD}`
- ✅ Endpoints protégés (ADMIN uniquement)
- ✅ Les métadonnées retournées ne contiennent **pas** de mots de passe
- ✅ Connexion via pool (HikariCP) avec timeout

---

## 📊 Métriques et Monitoring

### Logs
- Chaque synchronisation est loggée
- Erreurs de connexion tracées
- Durée de synchronisation mesurée

### Audit
- Toutes les actions d'intégration sont auditées via `AuditLogService`
- Type : `IMPORT_DATA` ou `SYSTEM_CONFIG`

### Exemple de logs
```
2024-01-15 10:30:15 INFO  IntegrationService - Source de données active : SIGIP Mock (Développement)
2024-01-15 10:30:15 INFO  IntegrationService - Synchronisation depuis SIGIP Mock : 2024-01-01 à 2024-01-31
2024-01-15 10:30:15 INFO  MockSigipDataSource - 124 enregistrements SIGIP Mock générés
2024-01-15 10:30:16 INFO  MockSigipDataSource - Synchronisation SIGIP Mock terminée : 124 enregistrements importés
```

---

## 💡 Présentation pour la soutenance

### Points clés à mentionner

1. **Architecture extensible** : "Nous avons conçu une interface `ExternalDataSource` qui permet d'intégrer n'importe quelle source de données externe. Actuellement, nous utilisons une implémentation Mock pour le développement, mais le système est prêt pour la connexion DB2 IBM i réelle."

2. **Démo du Mock** : "Voici comment nous testons la connexion et synchronisons les données depuis notre simulation SIGIP. Les données générées sont réalistes avec des variations, des anomalies simulées, etc."

3. **Code production-ready** : "Le stub `SigipDB2DataSource` contient déjà toute la structure SQL et JDBC nécessaire. Il suffit d'ajouter la dépendance DB2 et de configurer les paramètres de connexion."

4. **Séparation dev/prod** : "Grâce aux profils Spring, nous utilisons automatiquement le Mock en développement et le vrai DB2 en production, sans changer une ligne de code métier."

---

## 🔗 Fichiers associés

- `ExternalDataSource.java` - Interface principale
- `MockSigipDataSource.java` - Implémentation de développement
- `SigipDB2DataSource.java` - Stub production (à compléter)
- `IntegrationService.java` - Service de gestion
- `IntegrationController.java` - API REST
- `COMPLETUDE-CDC.md` - Analyse complète par rapport au CDC
