# 🧪 TEST - Téléchargement PDF Dashboard

## ⚡ Test Rapide (2 minutes)

### 1️⃣ Ouvrir l'application

**URL** : `http://localhost:5173`

### 2️⃣ Se connecter

```
Email: admin@sews.com
Mot de passe: admin123
```

### 3️⃣ Aller sur le Dashboard

Menu → **Tableau de bord**

### 4️⃣ Sélectionner une période

- **Date début** : `04/07/2026` (ou -30 jours)
- **Date fin** : `03/08/2026` (ou aujourd'hui)

### 5️⃣ Cliquer "Télécharger le rapport"

Bouton bleu avec icône 📥 : **"Télécharger le rapport"**

### 6️⃣ Attendre la génération

Toast : "Génération du PDF en cours..."

### 7️⃣ PDF téléchargé ! ✅

Fichier : `Rapport_Dashboard_2026-07-04_2026-08-03.pdf`

---

## 📄 Vérifier le PDF

### Page 1 : Page de Garde

```
✅ Bande sombre en haut
✅ Logo SEWS (ou texte "AutoReport")
✅ Titre "Rapport de Production"
✅ Sous-titre "Rapport Dashboard"
✅ Date de génération
✅ Période analysée avec dates
✅ Ligne décorative ambre
✅ Footer "AutoReport © 2024"
```

### Page 2 : Contenu

```
✅ Titre "Rapport de Production"
✅ Ligne décorative ambre

✅ Section "Indicateurs généraux"
   ├─ Carte Production totale (ambre)
   ├─ Carte Nombre de défauts (rouge)
   ├─ Carte Taux de qualité (teal)
   └─ Carte Taux de défaut (rouge)

✅ Section "Production par ligne"
   └─ Tableau avec colonnes:
      ├─ Ligne (D, B, A...)
      ├─ Production (unités)
      └─ Défauts (nombre)

✅ Footer avec date et copyright
```

### Page 3 : Graphiques

```
✅ Titre "Graphiques de production"

✅ Graphique "Production journalière"
   └─ Courbe bleue avec remplissage
   └─ Axes avec dates et quantités

✅ Graphique "Production par ligne"
   └─ Barres vertes (teal)
   └─ Axes avec lignes et quantités

✅ Footer avec date et copyright
```

---

## 🎨 Qualité Visuelle

### Design
- [x] Couleurs cohérentes avec l'application
- [x] Textes lisibles (bonnes tailles)
- [x] Espacement professionnel
- [x] Graphiques haute résolution
- [x] Tableaux bien formatés

### Branding
- [x] Logo visible
- [x] Couleur ambre (AutoReport)
- [x] Footer sur toutes les pages
- [x] Nom de fichier descriptif

---

## 📊 Exemple de Résultat Attendu

### Valeurs dans le PDF

```
Page de Garde:
┌────────────────────────────────────┐
│  Rapport de Production             │
│  ══════════════════════            │
│                                    │
│  Rapport Dashboard                 │
│  Généré le 3 août 2026            │
│                                    │
│  Période analysée                  │
│  04/07/2026 — 03/08/2026          │
└────────────────────────────────────┘

Indicateurs:
┌──────────────────────────────────────┐
│ Production totale : 970410 unités    │
│ Nombre de défauts : 373 unités       │
│ Taux de qualité : 99.96 %            │
│ Taux de défaut : 0.04 %              │
└──────────────────────────────────────┘

Production par ligne:
┌───────┬────────────┬─────────┐
│ Ligne │ Production │ Défauts │
├───────┼────────────┼─────────┤
│   D   │   369686   │   136   │
│   B   │   231045   │    96   │
│   A   │   138627   │    36   │
└───────┴────────────┴─────────┘
```

---

## 🐛 Si Problème

### PDF ne se télécharge pas

**1. Vérifier la Console (F12)**
```javascript
// Rechercher les erreurs
// Si "Cannot find module 'html2canvas'"
→ npm install html2canvas
```

**2. Vérifier les données**
```javascript
// Le Dashboard doit avoir des données
// Si vide : Importer un fichier Excel d'abord
```

**3. Vérifier le navigateur**
```
// Autoriser les téléchargements
// Paramètres → Téléchargements → Autoriser
```

### Logo ne s'affiche pas

**Normal !** Le logo est affiché depuis `/src/assets/sews-logo.png`.
Si le fichier n'existe pas, le texte "AutoReport" s'affiche à la place.

### Graphiques flous

**Cause** : Zoom du navigateur

**Solution** : Remettre le zoom à 100% (Ctrl+0)

### PDF trop lourd (> 5 MB)

**Cause** : Beaucoup de données

**Solution** : Réduire la période ou compresser les images

---

## 🎓 Pour la Soutenance

### Démonstration (30 secondes)

1. "Voici le Dashboard avec les indicateurs de production"
2. "Je sélectionne une période à analyser"
3. "Je clique sur Télécharger le rapport"
4. Montrer le PDF téléchargé
5. Ouvrir le PDF :
   - "Page de garde professionnelle avec logo"
   - "Indicateurs clés avec design coloré"
   - "Tableau de production par ligne"
   - "Graphiques capturés en haute qualité"
6. "Le PDF est prêt à être envoyé ou imprimé !"

### Phrase clé
> "J'ai développé un générateur de PDF professionnel qui capture les graphiques en temps réel, ajoute une page de garde avec logo, et formate les données avec un design cohérent pour une présentation de qualité."

### Points forts
- ✅ **Design professionnel** (page de garde, couleurs, typographie)
- ✅ **Graphiques dynamiques** (capturés depuis le DOM)
- ✅ **Données formatées** (tableaux, KPI cards)
- ✅ **Branding** (logo, couleurs entreprise)
- ✅ **Prêt à l'emploi** (PDF imprimable/envoyable)

---

## 📸 Captures d'Écran Attendues

### Avant : Dashboard Web
```
╔══════════════════════════════════════════╗
║  Tableau de bord                    [📥] ║
╠══════════════════════════════════════════╣
║ ┌────┐ ┌────┐ ┌────┐ ┌────┐            ║
║ │Prod│ │Défa│ │Qual│ │Défa│            ║
║ │Tot │ │uts │ │ité │ │ut% │            ║
║ └────┘ └────┘ └────┘ └────┘            ║
║                                          ║
║ ┌─────────────────────────────────────┐ ║
║ │  Production journalière (courbe)    │ ║
║ └─────────────────────────────────────┘ ║
║                                          ║
║ ┌─────────────────────────────────────┐ ║
║ │  Production par ligne (barres)      │ ║
║ └─────────────────────────────────────┘ ║
╚══════════════════════════════════════════╝
```

### Après : PDF Téléchargé
```
📄 Rapport_Dashboard_2026-07-04_2026-08-03.pdf

Page 1: Page de garde ✨
Page 2: Indicateurs + Tableaux 📊
Page 3: Graphiques 📈
```

---

## ✅ Checklist de Test

### Fonctionnel
- [ ] Bouton "Télécharger" visible
- [ ] Toast "Génération en cours" affiché
- [ ] PDF téléchargé (vérifier dossier Téléchargements)
- [ ] Nom de fichier contient les dates
- [ ] PDF s'ouvre sans erreur

### Contenu
- [ ] Page de garde présente
- [ ] Logo ou texte AutoReport visible
- [ ] Dates correctes (période + génération)
- [ ] KPI cards avec valeurs
- [ ] Tableau production par ligne rempli
- [ ] Graphiques visibles et lisibles

### Qualité
- [ ] Textes nets (pas flous)
- [ ] Couleurs cohérentes
- [ ] Mise en page professionnelle
- [ ] Graphiques haute résolution
- [ ] Footer sur chaque page

---

**Prêt à tester ! 🚀**

Temps de test : **2 minutes**  
Fichier résultat : **PDF professionnel** 📄✨
