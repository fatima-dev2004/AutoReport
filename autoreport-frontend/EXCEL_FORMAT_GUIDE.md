# Guide du Format Excel pour AutoReport System

## 📋 Format Requis

Le fichier Excel doit contenir les colonnes suivantes (respectez l'orthographe exacte) :

| Colonne   | Type     | Format              | Obligatoire | Description                          |
|-----------|----------|---------------------|-------------|--------------------------------------|
| `date`    | Date     | YYYY-MM-DD          | ✅ Oui      | Date de production                   |
| `line`    | Texte    | Texte libre         | ✅ Oui      | Nom de la ligne de production        |
| `quantity`| Nombre   | Entier positif      | ✅ Oui      | Quantité produite                    |
| `defects` | Nombre   | Entier positif      | ❌ Non      | Nombre de défauts constatés          |

## ✅ Exemple de Fichier Valide

```
| date       | line    | quantity | defects |
|------------|---------|----------|---------|
| 2024-01-10 | LINE-A  | 1500     | 12      |
| 2024-01-10 | LINE-B  | 1800     | 8       |
| 2024-01-10 | LINE-C  | 2100     | 15      |
| 2024-01-11 | LINE-A  | 1600     | 10      |
| 2024-01-11 | LINE-B  | 1750     | 6       |
| 2024-01-11 | LINE-C  | 2050     | 12      |
| 2024-01-12 | LINE-A  | 1550     | 14      |
| 2024-01-12 | LINE-B  | 1900     | 9       |
| 2024-01-12 | LINE-C  | 2200     | 11      |
```

## ❌ Erreurs Communes à Éviter

### 1. Nom de colonne incorrect
```
❌ Date (majuscule)      → ✅ date (minuscule)
❌ Line Name             → ✅ line
❌ qty                   → ✅ quantity
❌ defect                → ✅ defects
```

### 2. Format de date incorrect
```
❌ 10/01/2024            → ✅ 2024-01-10
❌ 10-01-2024            → ✅ 2024-01-10
❌ 2024/01/10            → ✅ 2024-01-10
❌ January 10, 2024      → ✅ 2024-01-10
```

### 3. Valeurs invalides
```
❌ quantity: "1500 unités" → ✅ quantity: 1500
❌ quantity: 1500.5        → ✅ quantity: 1500 (entier)
❌ quantity: -100          → ✅ quantity: 100 (positif)
❌ quantity: (vide)        → ✅ quantity: 0
❌ defects: "aucun"        → ✅ defects: 0
```

### 4. Colonnes manquantes
```
❌ Fichier sans colonne "date"
❌ Fichier sans colonne "line"
❌ Fichier sans colonne "quantity"
✅ Fichier sans colonne "defects" (optionnelle)
```

## 📝 Règles de Validation

### Côté Frontend
1. Extension : `.xlsx` ou `.xls` uniquement
2. Taille maximale : 10 MB
3. Feedback immédiat en cas de problème

### Côté Backend
1. Vérification des colonnes obligatoires
2. Validation du format de chaque cellule
3. Rapport d'erreurs détaillé avec numéros de lignes

## 🎯 Exemple Complet pour Tests

### Fichier de Production Quotidienne (50 lignes)

```excel
date       | line    | quantity | defects
-----------|---------|----------|--------
2024-01-01 | LINE-A  | 1500     | 12
2024-01-01 | LINE-B  | 1800     | 8
2024-01-01 | LINE-C  | 2100     | 15
2024-01-01 | LINE-D  | 1900     | 10
2024-01-02 | LINE-A  | 1600     | 10
2024-01-02 | LINE-B  | 1750     | 6
2024-01-02 | LINE-C  | 2050     | 12
2024-01-02 | LINE-D  | 1850     | 8
2024-01-03 | LINE-A  | 1550     | 14
2024-01-03 | LINE-B  | 1900     | 9
2024-01-03 | LINE-C  | 2200     | 11
2024-01-03 | LINE-D  | 1950     | 13
2024-01-04 | LINE-A  | 1700     | 11
2024-01-04 | LINE-B  | 1850     | 7
2024-01-04 | LINE-C  | 2150     | 16
2024-01-04 | LINE-D  | 2000     | 9
2024-01-05 | LINE-A  | 1650     | 13
2024-01-05 | LINE-B  | 1820     | 8
2024-01-05 | LINE-C  | 2080     | 14
2024-01-05 | LINE-D  | 1920     | 11
2024-01-06 | LINE-A  | 1580     | 12
2024-01-06 | LINE-B  | 1780     | 10
2024-01-06 | LINE-C  | 2020     | 15
2024-01-06 | LINE-D  | 1880     | 7
2024-01-07 | LINE-A  | 1620     | 9
2024-01-07 | LINE-B  | 1840     | 6
2024-01-07 | LINE-C  | 2110     | 13
2024-01-07 | LINE-D  | 1940     | 12
2024-01-08 | LINE-A  | 1590     | 11
2024-01-08 | LINE-B  | 1800     | 8
2024-01-08 | LINE-C  | 2090     | 14
2024-01-08 | LINE-D  | 1910     | 10
2024-01-09 | LINE-A  | 1640     | 10
2024-01-09 | LINE-B  | 1860     | 7
2024-01-09 | LINE-C  | 2130     | 12
2024-01-09 | LINE-D  | 1970     | 9
2024-01-10 | LINE-A  | 1600     | 13
2024-01-10 | LINE-B  | 1830     | 9
2024-01-10 | LINE-C  | 2070     | 16
2024-01-10 | LINE-D  | 1930     | 11
2024-01-11 | LINE-A  | 1570     | 12
2024-01-11 | LINE-B  | 1790     | 8
2024-01-11 | LINE-C  | 2040     | 14
2024-01-11 | LINE-D  | 1900     | 10
2024-01-12 | LINE-A  | 1630     | 11
2024-01-12 | LINE-B  | 1850     | 6
2024-01-12 | LINE-C  | 2120     | 15
2024-01-12 | LINE-D  | 1960     | 8
2024-01-13 | LINE-A  | 1610     | 10
2024-01-13 | LINE-B  | 1820     | 7
```

## 📊 Résultat Attendu

Après import de ce fichier, le système devrait calculer :

**KPIs :**
- Production totale : ~95 000 unités
- Défauts totaux : ~520 unités
- Taux de qualité : ~99.5%
- Taux de défaut : ~0.5%

**Graphiques :**
- Production journalière : courbe montrant les variations par jour
- Production par ligne : barres comparant LINE-A, LINE-B, LINE-C, LINE-D

## 🔍 Débogage

### Le fichier ne s'importe pas ?

1. **Vérifiez les noms de colonnes** :
   - Ouvrez le fichier Excel
   - Vérifiez la première ligne (header)
   - Les noms doivent être exactement : `date`, `line`, `quantity`, `defects`

2. **Vérifiez le format des dates** :
   - Clic droit sur une cellule → Format de cellule
   - Choisir "Personnalisé"
   - Utiliser le format : `yyyy-mm-dd`

3. **Vérifiez les types de données** :
   - `quantity` et `defects` doivent être des nombres
   - Pas de texte, pas de formules

4. **Vérifiez l'extension** :
   - Le fichier doit se terminer par `.xlsx` ou `.xls`
   - Pas `.csv`, pas `.txt`

### Messages d'erreur courants

**"Colonne 'date' manquante"**
→ Ajoutez une colonne nommée exactement `date`

**"Colonne 'line' manquante"**
→ Ajoutez une colonne nommée exactement `line`

**"Colonne 'quantity' manquante"**
→ Ajoutez une colonne nommée exactement `quantity`

**"Ligne X : format de date invalide"**
→ Changez le format de la date en `YYYY-MM-DD`

**"Ligne X : quantity doit être un nombre"**
→ Retirez tout texte de la cellule quantity

**"Le fichier est trop volumineux"**
→ Le fichier doit faire moins de 10 MB

## 💡 Conseils

1. **Utilisez Excel ou LibreOffice Calc** pour créer le fichier
2. **Testez avec un petit fichier** (10 lignes) avant un gros import
3. **Conservez une copie** du fichier original avant modification
4. **Vérifiez l'historique** après import pour voir le statut
5. **Consultez les erreurs détaillées** en cliquant sur l'icône "👁️" dans l'historique

## 🎓 Pour la Soutenance

Préparez **3 fichiers de test** :

1. **Fichier valide** (50 lignes) - pour montrer un import réussi
2. **Fichier avec erreurs** (colonnes mal nommées) - pour montrer la validation
3. **Fichier volumineux** (500 lignes) - pour montrer la performance

---

**Besoin d'aide ?** Consultez la section "Format du fichier Excel requis" dans l'application (`/import`).
