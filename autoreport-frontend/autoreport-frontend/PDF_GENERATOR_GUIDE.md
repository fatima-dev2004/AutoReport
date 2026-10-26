# 📄 Guide du Générateur de PDF Professionnel

## 🎨 Fonctionnalités

Le générateur de PDF permet d'exporter le Dashboard avec un design professionnel comprenant :

### ✨ Page de Garde
- 🏢 **Logo SEWS** en haut
- 📊 **Titre principal** : "Rapport de Production"
- 📅 **Date de génération** automatique
- 🗓️ **Période analysée** (dates de début et fin)
- 🎨 **Design moderne** avec bandes de couleur et lignes décoratives

### 📊 Page de Contenu
1. **Indicateurs Clés (KPI Cards)**
   - Production totale (ambre)
   - Nombre de défauts (rouge)
   - Taux de qualité (teal)
   - Taux de défaut (rouge)
   - Format: Cartes colorées avec icônes et valeurs

2. **Tableau Production par Ligne**
   - Lignes de production (A, B, C, D...)
   - Quantités produites
   - Nombre de défauts
   - Design: Tableau avec en-têtes ambre et lignes alternées

### 📈 Page de Graphiques
1. **Production Journalière**
   - Graphique de type Area (courbe avec remplissage)
   - Capturé depuis le Dashboard en temps réel
   - Haute résolution (scale: 2)

2. **Production par Ligne**
   - Graphique de type Bar (barres verticales)
   - Capturé depuis le Dashboard en temps réel
   - Couleur teal

---

## 🔧 Utilisation

### Dans le Dashboard

```jsx
import { generateDashboardPDF, addChartToPDF, savePDF } from '../utils/pdfGenerator';

// Générer le PDF
const doc = await generateDashboardPDF(data, range);

// Ajouter les graphiques capturés
await addChartToPDF(doc, chartElement, 'Titre du graphique', yPosition);

// Sauvegarder
savePDF(doc, 'rapport.pdf');
```

### Depuis l'Interface

1. Ouvrir le **Dashboard**
2. Sélectionner la **période** (dates début/fin)
3. Cliquer sur **"Télécharger le rapport"**
4. Le PDF est généré avec :
   - Page de garde
   - Indicateurs
   - Tableaux
   - Graphiques capturés
5. Fichier téléchargé : `Rapport_Dashboard_YYYY-MM-DD_YYYY-MM-DD.pdf`

---

## 🎨 Personnalisation des Couleurs

Les couleurs sont définies dans `pdfGenerator.js` :

```javascript
const COLORS = {
  primary: '#F59E0B',    // Ambre (AutoReport)
  secondary: '#0D9488',  // Teal (qualité)
  dark: '#0F172A',       // Bleu très foncé
  light: '#F8FAFC',      // Blanc cassé
  border: '#E2E8F0',     // Gris clair
  text: '#334155',       // Gris foncé
  textMuted: '#64748B',  // Gris moyen
};
```

---

## 📦 Dépendances

```json
{
  "jspdf": "^4.2.1",           // Génération PDF
  "jspdf-autotable": "^5.0.8", // Tableaux stylisés
  "html2canvas": "^1.4.1"      // Capture de graphiques
}
```

---

## 🖼️ Structure du PDF

```
┌─────────────────────────────────────────┐
│ PAGE 1 : PAGE DE GARDE                  │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────┐    │
│ │   Bande Sombre (dark)           │    │
│ │   [LOGO SEWS]                   │    │
│ └─────────────────────────────────┘    │
│                                         │
│     Rapport de Production              │
│     ────────────────────               │
│                                         │
│     Rapport Dashboard                   │
│     Généré le 03/08/2026               │
│                                         │
│     Période analysée                    │
│     04/07/2026 — 03/08/2026            │
│     ─────────────                      │
│                                         │
│     AutoReport © 2024                   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ PAGE 2 : CONTENU                        │
├─────────────────────────────────────────┤
│ Rapport de Production                   │
│ ═══════════════════════════════════    │
│                                         │
│ Indicateurs généraux                    │
│ ─────────────────                      │
│ ┌────┐ ┌────┐ ┌────┐ ┌────┐          │
│ │Prod│ │Défa│ │Qual│ │Défa│          │
│ │Tot │ │uts │ │ité │ │ut% │          │
│ └────┘ └────┘ └────┘ └────┘          │
│                                         │
│ Production par ligne                    │
│ ─────────────────                      │
│ ┌─────────────────────────────────┐   │
│ │ Ligne │ Production │ Défauts    │   │
│ ├─────────────────────────────────┤   │
│ │   D   │  369686    │    136     │   │
│ │   B   │  231045    │     96     │   │
│ │   A   │  138627    │     36     │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ─────────────────────────────────────  │
│ Généré le 03/08/2026   AutoReport ©    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ PAGE 3 : GRAPHIQUES                     │
├─────────────────────────────────────────┤
│ Graphiques de production                │
│                                         │
│ Production journalière                  │
│ ─────────────────                      │
│ [GRAPHIQUE AREA CAPTURÉ]               │
│                                         │
│ Production par ligne                    │
│ ─────────────────                      │
│ [GRAPHIQUE BAR CAPTURÉ]                │
│                                         │
│ ─────────────────────────────────────  │
│ Généré le 03/08/2026   AutoReport ©    │
└─────────────────────────────────────────┘
```

---

## 🔍 Exemple de Résultat

### Page de Garde
```
╔═══════════════════════════════════════════╗
║ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ ║
║ ▓▓▓▓▓▓▓▓▓▓▓  [SEWS LOGO]  ▓▓▓▓▓▓▓▓▓▓▓ ║
║ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ ║
║                                           ║
║                                           ║
║        Rapport de Production             ║
║        ═════════════════════             ║
║                                           ║
║          Rapport Dashboard                ║
║        Généré le 3 août 2026             ║
║                                           ║
║                                           ║
║          Période analysée                 ║
║      04/07/2026 — 03/08/2026             ║
║          ─────────────                   ║
║                                           ║
║                                           ║
║                                           ║
║                                           ║
║   AutoReport © 2024 - Système de         ║
║   rapports automatisés                    ║
╚═══════════════════════════════════════════╝
```

### KPI Cards
```
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│▓▓▓▓▓▓▓▓▓│ │█████████│ │▒▒▒▒▒▒▒▒▒│ │█████████│
│         │ │         │ │         │ │         │
│Production│ │ Nombre  │ │ Taux de │ │ Taux de │
│ totale  │ │ défauts │ │ qualité │ │ défaut  │
│         │ │         │ │         │ │         │
│ 970410  │ │   373   │ │  99.96  │ │  0.04   │
│ unités  │ │ unités  │ │    %    │ │    %    │
│         │ │         │ │         │ │         │
└─────────┘ └─────────┘ └─────────┘ └─────────┘
 Ambre       Rouge       Teal        Rouge
```

---

## 🚀 Améliorations Futures

### Possibilités d'Extension

1. **Graphiques Supplémentaires**
   ```javascript
   // Ajouter un graphique de tendance
   await addChartToPDF(doc, trendChart, 'Tendance hebdomadaire', yPos);
   ```

2. **Sections Personnalisées**
   ```javascript
   // Ajouter une section commentaire
   addCommentSection(doc, 'Analyse de la période', yPos);
   ```

3. **Export Multi-Format**
   ```javascript
   // Exporter en PNG
   const canvas = await html2canvas(element);
   canvas.toBlob(blob => saveAs(blob, 'dashboard.png'));
   ```

4. **Watermark**
   ```javascript
   // Ajouter un filigrane
   doc.setTextColor(200, 200, 200);
   doc.setFontSize(50);
   doc.text('CONFIDENTIEL', pageWidth/2, pageHeight/2, {
     align: 'center',
     angle: 45,
   });
   ```

---

## 🐛 Dépannage

### Problème : Logo ne s'affiche pas

**Cause** : Chemin d'import incorrect

**Solution** :
```javascript
// Vérifier le chemin
import sewsLogo from '../assets/sews-logo.png';

// Ou utiliser un logo en base64
const logoBase64 = 'data:image/png;base64,iVBORw0KG...';
doc.addImage(logoBase64, 'PNG', x, y, width, height);
```

### Problème : Graphiques flous

**Cause** : Scale trop faible

**Solution** :
```javascript
// Augmenter le scale dans html2canvas
const canvas = await html2canvas(element, {
  scale: 3, // Au lieu de 2
  backgroundColor: '#ffffff',
});
```

### Problème : PDF trop lourd

**Cause** : Images haute résolution

**Solution** :
```javascript
// Compresser l'image
const imgData = canvas.toDataURL('image/jpeg', 0.7); // 70% qualité
```

### Problème : Tableaux coupés

**Cause** : Débordement de page

**Solution** :
```javascript
// Vérifier la position Y avant d'ajouter
if (yPosition > 250) {
  doc.addPage();
  yPosition = 20;
}
```

---

## 📖 API Reference

### `generateDashboardPDF(data, dateRange)`
Génère le PDF avec page de garde et contenu.

**Paramètres:**
- `data` : Objet contenant les KPIs et données
- `dateRange` : `{ start: 'YYYY-MM-DD', end: 'YYYY-MM-DD' }`

**Retourne:** Instance jsPDF

### `addChartToPDF(doc, element, title, yPosition)`
Capture et ajoute un graphique au PDF.

**Paramètres:**
- `doc` : Instance jsPDF
- `element` : Élément DOM du graphique
- `title` : Titre de la section
- `yPosition` : Position Y sur la page

**Retourne:** Nouvelle position Y

### `savePDF(doc, filename)`
Sauvegarde le PDF.

**Paramètres:**
- `doc` : Instance jsPDF
- `filename` : Nom du fichier (ex: 'rapport.pdf')

---

## ✅ Checklist de Qualité

- [x] Page de garde avec logo
- [x] Titre et date
- [x] KPI Cards colorés
- [x] Tableaux stylisés
- [x] Graphiques capturés en haute résolution
- [x] Footer sur chaque page
- [x] Design cohérent avec l'application
- [x] Nom de fichier descriptif
- [x] Format A4
- [x] Polices professionnelles

---

**Votre rapport PDF est maintenant professionnel et prêt pour la production ! 🎉**
