# 🚀 Quick Start - Module d'Import Excel

## 📋 En 5 Minutes

Guide rapide pour utiliser le nouveau module d'import Excel professionnel.

---

## 1️⃣ Prérequis

✅ Backend Spring Boot démarré sur `http://localhost:8080`  
✅ Token JWT valide (via `/api/auth/login`)  
✅ Fichier Excel `.xlsx` avec au moins 4 colonnes

---

## 2️⃣ Préparer votre Fichier Excel

### Colonnes Obligatoires (une variante de chaque)

| Colonne | Variantes Acceptées | Exemple |
|---------|-------------------|---------|
| **Date** | Production_Date, Date, date_prod, Date Production | 2026-07-09 |
| **Ligne** | Line, Ligne, ligne_production, Production_Line | Ligne A |
| **Quantité** | Quantity, Quantité, qty, Production, prod | 15000 |
| **Défauts** | Defects, Défauts, nc, rebuts, non_conforme | 150 |

### Exemple de Fichier Minimum

```excel
| Date       | Ligne   | Quantité | Défauts |
|------------|---------|----------|---------|
| 2026-07-01 | Ligne A | 15000    | 150     |
| 2026-07-02 | Ligne B | 20000    | 200     |
```

**Sauvegardez en `.xlsx` ✅**

---

## 3️⃣ Importer via API

### Commande cURL

```bash
curl -X POST http://localhost:8080/api/reports/import \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  -F "file=@production.xlsx"
```

### Réponse Attendue

```json
{
  "success": true,
  "filename": "production.xlsx",
  "recordsImported": 2,
  "rowsSkipped": 0,
  "message": "Import termine avec succes. 2 ligne(s) importee(s).",
  "kpi": {
    "totalProduction": 35000.0,
    "totalDefects": 350,
    "qualityRate": 99.0,
    "defectRate": 1.0,
    "periodStart": "2026-07-01",
    "periodEnd": "2026-07-02"
  },
  "errors": []
}
```

✅ **C'est fait !** Les données sont en base et les KPI sont calculés.

---

## 4️⃣ Vérifier les Résultats

### Dashboard

```bash
curl http://localhost:8080/api/dashboard/kpis \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

### Générer un Rapport

```bash
curl -X POST "http://localhost:8080/api/reports/generate?start=2026-07-01&end=2026-07-31" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

---

## 5️⃣ Gestion des Erreurs

### Si Import Partiel

```json
{
  "success": true,
  "recordsImported": 2,
  "rowsSkipped": 1,
  "errors": [
    {
      "rowNumber": 3,
      "error": "Format de date non reconnu : INVALID"
    }
  ]
}
```

**Action :** Corrigez la ligne 3 et réimportez

---

## 🎯 Cas d'Usage Courants

### Fichier avec Accents

```excel
| Date | Ligne | Quantité | Défauts |
|------|-------|----------|---------|
```

✅ **Fonctionne !** Les accents sont gérés automatiquement.

### Fichier avec Dates Françaises

```excel
| Date       |
|------------|
| 09/07/2026 |
```

✅ **Fonctionne !** Format dd/MM/yyyy reconnu automatiquement.

### Fichier avec Nombres en Texte

```excel
| Quantité |
|----------|
| "15000"  |
```

✅ **Fonctionne !** Conversion automatique en nombre.

---

## ⚠️ Erreurs Courantes

### Erreur 1 : Colonnes Manquantes

**Message :**
```
Colonnes manquantes. Colonnes detectees : Date=Date, Ligne=Ligne. 
Veuillez verifier que le fichier contient : Date, Ligne, Quantite, Defauts
```

**Solution :** Ajoutez les colonnes manquantes (Quantité et Défauts).

### Erreur 2 : Format de Fichier

**Message :**
```
Format invalide : seul .xlsx est accepte
```

**Solution :** Convertissez en `.xlsx` (pas `.xls` ou `.csv`).

### Erreur 3 : Valeurs Négatives

**Ligne ignorée :** `"Quantite invalide : -500.0"`

**Solution :** Les quantités et défauts doivent être ≥ 0.

---

## 📖 Documentation Complète

Pour plus de détails, consultez :

1. **`AMELIORATIONS-IMPORT-EXCEL.md`** - Guide complet (18 sections)
2. **`EXEMPLES-FICHIERS-EXCEL.md`** - 10 exemples de fichiers
3. **`RESUME-AMELIORATIONS.md`** - Synthèse des améliorations

---

## 💡 Astuces

### 1. Testez avec un Petit Fichier

Importez d'abord 2-3 lignes pour vérifier le format.

### 2. Consultez le Rapport d'Erreurs

L'API vous indique **exactement** quel(s) problème(s) et à quelle(s) ligne(s).

### 3. Les Lignes Vides sont OK

Elles sont automatiquement ignorées. Pas besoin de les supprimer.

### 4. Plusieurs Formats de Date Acceptés

Pas besoin de tout uniformiser. Le système gère automatiquement.

---

## ✅ Checklist Rapide

Avant d'importer :

- [ ] Fichier au format `.xlsx`
- [ ] 4 colonnes présentes (Date, Ligne, Quantité, Défauts)
- [ ] Au moins 1 ligne de données
- [ ] Backend démarré
- [ ] Token JWT valide

---

## 🎉 Vous êtes Prêt !

Le module d'import est conçu pour être :

- ✅ **Simple** : 4 colonnes obligatoires
- ✅ **Flexible** : accepte plusieurs noms
- ✅ **Robuste** : gère les erreurs
- ✅ **Rapide** : analyse instantanée

**Bon import ! 🚀**

---

**Questions ?** Consultez la documentation complète ou contactez l'équipe de développement.
