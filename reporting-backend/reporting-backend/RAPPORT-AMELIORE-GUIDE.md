# 📊 Guide - Rapport PDF Amélioré avec Page de Garde

**Date:** 20 juillet 2026  
**Version:** 2.0

---

## 🎯 Nouvelles Fonctionnalités

### 1. Page de Garde Professionnelle (Cover Page)

**Contenu de la page de garde:**
- ✅ Logo SEWS (si configuré dans les paramètres)
- ✅ Nom de l'entreprise
- ✅ Titre du rapport personnalisé
- ✅ Période d'analyse
- ✅ Résumé exécutif avec indicateurs clés
- ✅ Date et heure de génération
- ✅ Nom de la personne qui a généré le rapport

### 2. Page d'Analyse avec Graphiques

**Contenu de la page d'analyse:**
- ✅ 4 cartes d'indicateurs colorées:
  - Production totale (bleu)
  - Défauts constatés (rouge)
  - Taux de qualité (vert)
  - Taux de défaut (orange)
- ✅ Graphique à barres "Production par ligne"
- ✅ Détails par ligne de production (formaté)

### 3. Options Personnalisables

L'utilisateur peut maintenant choisir:
- Titre personnalisé du rapport
- Nom de la personne qui génère
- Inclure ou non la page de garde
- Inclure ou non les graphiques
- Ajouter des commentaires

---

## 🔧 Changements Techniques

### Nouveaux Fichiers Créés

**1. Template JasperReports Amélioré:**
```
src/main/resources/reports/production_report_enhanced.jrxml
```
- 2 pages avec mise en page professionnelle
- Support pour logo et paramètres personnalisés
- Graphiques intégrés

**2. DTOs pour les Options:**
```java
dto/ReportGenerationRequest.java    // Options de génération
dto/ChartDataDto.java               // Données pour graphiques
```

**3. Service Amélioré:**
```java
service/ReportGenerationService.java
- generateEnhancedReport()          // Nouvelle méthode
- generateEnhancedPdf()             // Template amélioré avec graphiques
```

**4. Controller Mis à Jour:**
```java
controller/ReportController.java
- POST /api/reports/generate/enhanced  // Nouveau endpoint
```

---

## 📡 API Endpoints

### Ancien Endpoint (Simple)
```http
POST /api/reports/generate?start=2026-07-01&end=2026-07-31
```
**Réponse:** Rapport simple sans page de garde

### Nouveau Endpoint (Amélioré)
```http
POST /api/reports/generate/enhanced
Content-Type: application/json

{
  "startDate": "2026-07-01",
  "endDate": "2026-07-31",
  "reportTitle": "Rapport de Production Juillet 2026",
  "generatedBy": "Fatima Benritounia",
  "comments": "Analyse mensuelle",
  "department": "Production",
  "reportPurpose": "Suivi mensuel",
  "includeCoverPage": true,
  "includeCharts": true,
  "includeDetailedData": true,
  "reportType": "PRODUCTION"
}
```

**Réponse:**
```json
{
  "id": 1,
  "title": "Rapport de Production Juillet 2026",
  "type": "PRODUCTION",
  "periodStart": "2026-07-01",
  "periodEnd": "2026-07-31",
  "totalProduction": 1386255,
  "totalDefects": 525,
  "qualityRate": 99.96,
  "generatedByEmail": "admin@sews.com",
  "generatedAt": "2026-07-20T10:30:00"
}
```

---

## 🎨 Structure du PDF Amélioré

### Page 1: Page de Garde

```
┌─────────────────────────────────────────┐
│                                         │
│           [LOGO SEWS]                   │
│                                         │
│         SEWS CABIND                     │
│       ──────────────                    │
│                                         │
│    RAPPORT DE PRODUCTION                │
│   Rapport Production Juillet 2026       │
│                                         │
│   Période : 01/07/2026 au 31/07/2026    │
│                                         │
│   ┌─────────────────────────────────┐   │
│   │    RÉSUMÉ EXÉCUTIF              │   │
│   │                                 │   │
│   │ Production totale: 1 386 255   │   │
│   │ Taux de qualité: 99.96 %       │   │
│   │ Défauts: 525 unités            │   │
│   └─────────────────────────────────┘   │
│                                         │
│   Généré le : 20/07/2026 à 10:30        │
│   Par : Fatima Benritounia              │
│                                         │
│   Document confidentiel                 │
└─────────────────────────────────────────┘
```

### Page 2: Analyses et Graphiques

```
┌─────────────────────────────────────────┐
│ 📊 INDICATEURS CLÉS                     │
│                                         │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐   │
│ │PRODUC││DÉFAUTS││QUALITÉ││DÉFAUT │   │
│ │ TION ││ CONSTA││  RATE ││ RATE  │   │
│ │      ││  TÉS  ││       ││       │   │
│ │1.386M││  525  ││99.96% ││ 0.04% │   │
│ └──────┘ └──────┘ └──────┘ └──────┘   │
│                                         │
│ 📈 PRODUCTION PAR LIGNE                 │
│                                         │
│     [GRAPHIQUE À BARRES]                │
│      ┃                                  │
│ 500K ┃█████                             │
│      ┃█████ ████                        │
│ 400K ┃█████ ████ ████                   │
│      ┃█████ ████ ████ ███               │
│ 300K ┃█████ ████ ████ ███               │
│      └─────┴────┴────┴───               │
│        D    C    B    A                 │
│                                         │
│ 📋 DÉTAILS PAR LIGNE                    │
│                                         │
│ ═══════════════════════════════════     │
│ Ligne D                                 │
│   • Production : 481 380 unités         │
│   • Défauts    : 175 unités             │
│   • Qualité    : 99.96 %                │
│ ───────────────────────────────────     │
│ Ligne C                                 │
│   • Production : 330 740 unités         │
│   • Défauts    : 150 unités             │
│   • Qualité    : 99.95 %                │
│ ...                                     │
└─────────────────────────────────────────┘
```

---

## 🔄 Workflow d'Utilisation

### Depuis le Frontend

**Étape 1:** Cliquer sur "Générer un Rapport"

**Étape 2:** Remplir le formulaire:
```
┌─────────────────────────────────────┐
│ Génération de Rapport               │
├─────────────────────────────────────┤
│                                     │
│ Période:                            │
│  Du: [01/07/2026] Au: [31/07/2026] │
│                                     │
│ Titre personnalisé:                 │
│  [Rapport Production Juillet]      │
│                                     │
│ Généré par:                         │
│  [Fatima Benritounia]              │
│                                     │
│ Options:                            │
│  ☑ Inclure page de garde           │
│  ☑ Inclure graphiques              │
│  ☑ Inclure données détaillées      │
│                                     │
│ Commentaires (optionnel):           │
│  [Rapport mensuel standard]        │
│                                     │
│     [Annuler]  [Générer]           │
└─────────────────────────────────────┘
```

**Étape 3:** Le rapport est généré avec:
- Page de garde si cochée
- Graphiques si cochés
- Logo de l'entreprise (depuis paramètres système)

**Étape 4:** Télécharger le PDF généré

---

## 💾 Persistance des Données

### Problème Résolu: Déconnexion

**Avant:**
- ❌ Les données configurées disparaissaient à la déconnexion
- ❌ Base H2 en mémoire (données perdues)

**Après:**
- ✅ Configuration sauvegardée en base de données
- ✅ Hibernate en mode `update` (conserve les données)
- ✅ Paramètres système persistent entre les sessions

**Comment ça marche:**

1. **Paramètres Système** sont stockés dans `system_settings`:
   - Logo de l'entreprise
   - Nom de l'entreprise
   - Configuration SMTP
   - Etc.

2. **Rapports Générés** sont stockés dans `reports`:
   - Titre personnalisé
   - Générateur
   - Période
   - Fichiers PDF/Excel

3. **Journal d'Activité** dans `audit_logs`:
   - Qui a généré le rapport
   - Quand
   - Avec quelles options

### Configuration Requise pour Production

Pour persister les données même après redémarrage du serveur:

**Option 1: Utiliser MySQL**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/reporting_db
spring.datasource.username=reporting_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

**Option 2: H2 avec fichier (pas mémoire)**
```properties
spring.datasource.url=jdbc:h2:file:./data/reporting_db
spring.jpa.hibernate.ddl-auto=update
```

---

## 📊 Exemple de Requête

### JavaScript (Frontend)

```javascript
// Générer un rapport amélioré
async function generateEnhancedReport() {
  const response = await fetch('http://localhost:8080/api/reports/generate/enhanced', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      startDate: '2026-07-01',
      endDate: '2026-07-31',
      reportTitle: 'Rapport Production Juillet 2026',
      generatedBy: 'Fatima Benritounia',
      comments: 'Rapport mensuel standard',
      department: 'Production',
      reportPurpose: 'Suivi mensuel',
      includeCoverPage: true,
      includeCharts: true,
      includeDetailedData: true,
      reportType: 'PRODUCTION'
    })
  });
  
  const report = await response.json();
  
  // Télécharger le PDF
  window.location.href = `http://localhost:8080/api/reports/${report.id}/download/pdf`;
}
```

### cURL (Test)

```bash
curl -X POST http://localhost:8080/api/reports/generate/enhanced \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "startDate": "2026-07-01",
    "endDate": "2026-07-31",
    "reportTitle": "Rapport Production Juillet 2026",
    "generatedBy": "Fatima Benritounia",
    "includeCoverPage": true,
    "includeCharts": true,
    "includeDetailedData": true
  }'
```

---

## 🎨 Personnalisation

### Logo de l'Entreprise

**Étape 1:** Aller dans Paramètres → Informations de l'Entreprise

**Étape 2:** Uploader le logo (PNG ou SVG, max 2 MB)

**Étape 3:** Le logo apparaîtra automatiquement sur:
- Page de garde des rapports
- En-têtes des pages suivantes

### Nom de l'Entreprise

Configurable dans Paramètres → Informations de l'Entreprise

Par défaut: "SEWS CABIND"

---

## 🐛 Dépannage

### Le logo n'apparaît pas

**Vérification:**
1. Aller dans Paramètres Système
2. Vérifier que `company.logo` a une valeur
3. Vérifier que le fichier existe sur le serveur

**Solution:**
- Re-uploader le logo
- Ou mettre le chemin complet du fichier

### Les graphiques ne s'affichent pas

**Cause possible:** Erreur dans le template JasperReports

**Solution:**
- Vérifier les logs du serveur
- Le système utilisera automatiquement le template simple en cas d'erreur

### Erreur "Template not found"

**Cause:** Le fichier `production_report_enhanced.jrxml` est manquant

**Solution:**
```bash
# Vérifier que le fichier existe
ls src/main/resources/reports/production_report_enhanced.jrxml

# Recompiler le projet
mvn clean compile
```

---

## 📈 Améliorations Futures

### À venir:
1. ⬜ Graphique de production journalière (évolution)
2. ⬜ Export en format Word (.docx)
3. ⬜ Envoi automatique par email avec page de garde
4. ⬜ Templates personnalisables par département
5. ⬜ Comparaison avec période précédente
6. ⬜ Prévisions basées sur l'historique

---

## ✅ Checklist de Test

### Tests à effectuer:

- [ ] Générer un rapport simple (ancien endpoint)
- [ ] Générer un rapport amélioré sans page de garde
- [ ] Générer un rapport amélioré avec page de garde
- [ ] Vérifier que le logo apparaît (si configuré)
- [ ] Vérifier que le nom d'entreprise apparaît
- [ ] Vérifier les graphiques
- [ ] Télécharger le PDF
- [ ] Télécharger l'Excel
- [ ] Vérifier l'audit log
- [ ] Se déconnecter et reconnecter → vérifier que les paramètres persistent

---

## 📞 Support

Pour toute question ou problème:
1. Consulter les logs: Terminal où tourne `mvn spring-boot:run`
2. Vérifier la base de données H2: http://localhost:8080/h2-console
3. Consulter l'audit log dans le frontend

---

**Version Backend:** 2.0  
**Date de mise à jour:** 20 juillet 2026  
**Auteur:** Équipe Développement SEWS
