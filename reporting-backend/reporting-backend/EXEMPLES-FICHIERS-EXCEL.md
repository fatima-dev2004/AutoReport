# 📊 Exemples de Fichiers Excel Compatibles

## 🎯 Objectif

Ce document présente différents exemples de fichiers Excel qui peuvent être importés avec le nouveau module d'import flexible.

---

## ✅ Exemple 1 : Format Standard

```
| Production_Date | Line    | Quantity | Defects |
|-----------------|---------|----------|---------|
| 2026-07-01      | Ligne A | 15000    | 150     |
| 2026-07-01      | Ligne B | 20000    | 200     |
| 2026-07-02      | Ligne A | 16000    | 160     |
| 2026-07-02      | Ligne B | 21000    | 210     |
```

**Résultat :** ✅ Import réussi - 4 lignes importées

---

## ✅ Exemple 2 : Format avec Accents

```
| Date | Ligne | Quantité | Défauts |
|------|-------|----------|---------|
| 01/07/2026 | A | 15000 | 150 |
| 02/07/2026 | B | 20000 | 200 |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

---

## ✅ Exemple 3 : Format Court

```
| date_prod | ligne | qty | nc |
|-----------|-------|-----|-----|
| 2026-07-01 | A | 15000 | 150 |
| 2026-07-02 | B | 20000 | 200 |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

**Note :** `nc` = Non Conforme (défauts)

---

## ✅ Exemple 4 : Format avec Espaces

```
| Date Production | Ligne Production | Production | Defauts |
|-----------------|------------------|------------|---------|
| 09-07-2026      | Ligne A          | 15 000     | 150     |
| 10-07-2026      | Ligne B          | 20 000     | 200     |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

**Note :** Les espaces dans les nombres (15 000) sont automatiquement gérés

---

## ✅ Exemple 5 : Dates en Format US

```
| Production_Date | Line | Quantity | Defects |
|-----------------|------|----------|---------|
| 07/09/2026      | A    | 15000    | 150     |
| 07/10/2026      | B    | 20000    | 200     |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

**Format :** MM/dd/yyyy (format américain)

---

## ⚠️ Exemple 6 : Avec Erreurs Mineures

```
| Date | Ligne | Quantité | Défauts |
|------|-------|----------|---------|
| 2026-07-01 | A | 15000 | 150 |
|  |  |  |  |
| INVALID | B | 20000 | 200 |
| 2026-07-03 | C | -500 | 100 |
| 2026-07-04 | D | 18000 | 180 |
```

**Résultat :** ⚠️ Import partiel - 2 lignes importées, 3 lignes ignorées

**Erreurs :**
- Ligne 2 : Ligne vide (ignorée automatiquement)
- Ligne 3 : Format de date non reconnu : INVALID
- Ligne 4 : Quantite invalide : -500.0

---

## ✅ Exemple 7 : Valeurs en Format Texte

```
| Date | Ligne | Quantité | Défauts |
|------|-------|----------|---------|
| 2026-07-01 | A | "15000" | "150" |
| 2026-07-02 | B | "20000" | "200" |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

**Note :** Les valeurs entre guillemets sont automatiquement converties en nombres

---

## ✅ Exemple 8 : Format Excel Date

```
| Production_Date | Line | Quantity | Defects |
|-----------------|------|----------|---------|
| 45113           | A    | 15000    | 150     |
| 45114           | B    | 20000    | 200     |
```

**Résultat :** ✅ Import réussi - 2 lignes importées

**Note :** 45113 = Format numérique Excel pour une date (converti automatiquement)

---

## ✅ Exemple 9 : Mélange de Formats

```
| date | ligne_production | prod | rebuts |
|------|------------------|------|--------|
| 2026-07-01 | Ligne A | 15000 | 150 |
| 01/07/2026 | Ligne B | 20 000 | 200 |
| 09-07-2026 | Ligne C | "18000" | 180 |
```

**Résultat :** ✅ Import réussi - 3 lignes importées

**Note :** Différents formats de date et de nombre dans le même fichier !

---

## ❌ Exemple 10 : Colonnes Manquantes

```
| Date | Ligne | Quantity |
|------|-------|----------|
| 2026-07-01 | A | 15000 |
```

**Résultat :** ❌ Erreur

**Message :** 
```
Colonnes manquantes. Colonnes detectees : Date=Date, Ligne=Ligne, Quantite=Quantity. 
Veuillez verifier que le fichier contient : Date, Ligne, Quantite, Defauts
```

---

## 📝 Règles d'Import

### Colonnes Obligatoires (au moins une variante de chaque)

1. **Date de Production**
   - Variantes : Production_Date, Date, Date Production, date_prod, etc.
   - Formats acceptés : Excel Date, yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy, MM/dd/yyyy

2. **Ligne de Production**
   - Variantes : Line, Ligne, Production_Line, ligne_production, etc.
   - Type : Texte (obligatoire, ne peut pas être vide)

3. **Quantité Produite**
   - Variantes : Quantity, Production, Quantité, qty, prod, etc.
   - Type : Nombre (doit être ≥ 0)

4. **Nombre de Défauts**
   - Variantes : Defects, Défauts, rebuts, nc, non_conforme, etc.
   - Type : Nombre entier (doit être ≥ 0)

### Validation Automatique

✅ **Accepté :**
- Lignes vides (ignorées)
- Cellules numériques ou texte
- Espaces dans les nombres
- Formats de date multiples
- Accents et caractères spéciaux

❌ **Rejeté :**
- Valeurs négatives (quantité ou défauts)
- Dates invalides (ex: 32/13/2026)
- Ligne de production vide
- Format de fichier autre que .xlsx

### Comportement en cas d'Erreur

- Les lignes **valides** sont importées
- Les lignes **invalides** sont ignorées
- Un **rapport d'erreurs** détaillé est fourni
- L'import **continue** malgré les erreurs

---

## 🎯 Conseils pour Préparer vos Fichiers

### ✅ Bonnes Pratiques

1. **Utilisez des en-têtes clairs**
   - Date, Ligne, Quantité, Défauts (ou variantes)

2. **Format de date cohérent**
   - Privilégiez : yyyy-MM-dd ou dd/MM/yyyy

3. **Valeurs numériques propres**
   - Sans symboles (€, unités, etc.)
   - Utilisez des points ou virgules pour les décimales

4. **Nommage des lignes cohérent**
   - "Ligne A", "Ligne B" plutôt que "A", "B"

### ❌ À Éviter

1. **Formats personnalisés complexes**
   - Évitez les formules Excel dans les cellules de données
   
2. **Cellules fusionnées**
   - Peuvent causer des erreurs de lecture

3. **Plusieurs en-têtes**
   - Une seule ligne d'en-tête au début

4. **Colonnes supplémentaires non documentées**
   - Seules les 4 colonnes obligatoires sont lues

---

## 🧪 Tester vos Fichiers

### Endpoint API

```http
POST http://localhost:8080/api/reports/import
Content-Type: multipart/form-data
Authorization: Bearer <votre-token-jwt>

file: [votre-fichier.xlsx]
```

### Réponse Attendue

```json
{
  "success": true,
  "filename": "production_juillet.xlsx",
  "importedAt": "2026-07-09T17:30:00",
  "totalRows": 100,
  "recordsImported": 95,
  "rowsSkipped": 5,
  "message": "Import termine avec succes. 95 ligne(s) importee(s), 5 ligne(s) ignoree(s), 5 erreur(s) detectee(s).",
  "errors": [
    { "rowNumber": 15, "error": "Date vide" },
    { "rowNumber": 23, "error": "Quantite invalide : -100" }
  ],
  "kpi": {
    "totalProduction": 145000.0,
    "totalDefects": 2900,
    "qualityRate": 98.0,
    "defectRate": 2.0,
    "periodStart": "2026-07-01",
    "periodEnd": "2026-07-31"
  }
}
```

---

## 📞 Support

En cas de problème avec l'import :

1. Vérifiez que le fichier contient les 4 colonnes obligatoires
2. Consultez le rapport d'erreurs retourné par l'API
3. Corrigez les lignes problématiques identifiées
4. Réessayez l'import

Le système est conçu pour être **tolérant aux erreurs** et **informatif** en cas de problème.

---

**Date de création :** 09 juillet 2026  
**Version du module :** 2.0.0
