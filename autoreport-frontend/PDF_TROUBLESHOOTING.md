# 🔧 Dépannage PDF - Solutions Rapides

## ✅ Problème Résolu !

Le générateur PDF a été corrigé avec :
- ✅ Import jsPDF v2.x correct (`{ jsPDF }`)
- ✅ Import jspdf-autotable correct (`autoTable`)
- ✅ Logo remplacé par texte "AutoReport"  
- ✅ Couleurs en format RGB tableau
- ✅ Gestion robuste des données nulles

---

## 🧪 Test Maintenant !

### 1. Actualiser la page (F5)
```
http://localhost:5173
```

### 2. Dashboard → Télécharger
1. Se connecter
2. Aller au Dashboard
3. Cliquer "Télécharger le rapport"
4. ✅ Le PDF devrait se télécharger !

---

## 🐛 Si Erreur Persiste

### Erreur : "Cannot read property..."

**Ouvrir la Console (F12) et regarder l'erreur exacte**

#### Solution 1 : Données manquantes
```javascript
// Si data est null ou vide
// → Importer des données Excel d'abord !
```

#### Solution 2 : Recharger les dépendances
```powershell
cd autoreport-frontend
npm install
```

#### Solution 3 : Vider le cache
```
Ctrl + Shift + R (Hard Reload)
```

---

### Erreur : "html2canvas is not defined"

**Solution** :
```powershell
cd autoreport-frontend
npm install html2canvas
```

---

### Erreur : "jsPDF is not a constructor"

**Cause** : Mauvaise version de jsPDF

**Solution** :
```powershell
npm uninstall jspdf jspdf-autotable
npm install jspdf@^2.5.2 jspdf-autotable@^3.8.4
```

---

### PDF vide ou incomplet

**Cause** : Pas de données dans le Dashboard

**Solution** :
1. Menu → **Import Excel**
2. Charger un fichier de production
3. Retourner au Dashboard
4. Les données apparaissent
5. Retenter le téléchargement PDF

---

### Graphiques manquants dans le PDF

**Cause** : Les refs ne sont pas attachés

**Solution** : Vérifier dans `Dashboard.jsx` :
```javascript
// Ces lignes doivent être présentes :
const dailyChartRef = useRef(null);
const lineChartRef = useRef(null);

// Et dans le JSX :
<div ref={dailyChartRef}>...</div>
<div ref={lineChartRef}>...</div>
```

---

### PDF se télécharge mais ne s'ouvre pas

**Cause** : Fichier corrompu

**Solution** :
1. Supprimer le PDF téléchargé
2. Vider le cache du navigateur
3. Recharger la page (F5)
4. Retenter le téléchargement

---

## 📋 Checklist Diagnostic

### Backend
- [ ] Backend Spring Boot lancé sur `localhost:8080`
- [ ] Données présentes (importé Excel)
- [ ] Endpoint `/api/dashboard/kpis` répond

### Frontend
- [ ] Frontend Vite lancé sur `localhost:5173`
- [ ] Dashboard affiche des données
- [ ] Console sans erreurs (F12)
- [ ] Bouton "Télécharger" visible et actif

### Dépendances
- [ ] `jspdf` installé
- [ ] `jspdf-autotable` installé
- [ ] `html2canvas` installé
- [ ] `node_modules` présent

---

## 🔍 Commandes de Diagnostic

### Vérifier les versions npm
```powershell
cd autoreport-frontend
npm list jspdf jspdf-autotable html2canvas
```

**Résultat attendu** :
```
├── html2canvas@1.4.1
├── jspdf@2.5.2
└── jspdf-autotable@3.8.4
```

### Vérifier le serveur Vite
```powershell
# Doit afficher :
# Local:   http://localhost:5173/
netstat -ano | findstr "5173"
```

### Vérifier le backend
```powershell
# Doit afficher une connexion
netstat -ano | findstr "8080"
```

---

## 📞 Message d'Erreur Typique

### "Échec de la génération du rapport PDF"

**Étapes de résolution** :

1. **Ouvrir Console (F12)**
   - Regarder l'erreur exacte

2. **Si "data is null"**
   ```
   → Importer des données Excel
   → Retourner au Dashboard
   → Vérifier que les KPI cards affichent des valeurs
   ```

3. **Si "Cannot find module"**
   ```powershell
   npm install
   ```

4. **Si "html2canvas error"**
   ```javascript
   // Les graphiques doivent être visibles à l'écran
   → Scroller pour voir les graphiques
   → Retenter le téléchargement
   ```

5. **Si autre erreur**
   ```
   → Copier l'erreur complète
   → Chercher dans les fichiers modifiés
   ```

---

## ✅ Test de Validation

Après corrections, tester :

```
1. Dashboard chargé ✅
2. Données visibles ✅
3. Clic "Télécharger" ✅
4. Toast "Génération..." ✅
5. PDF téléchargé ✅
6. PDF s'ouvre ✅
7. 3 pages présentes ✅
8. Graphiques visibles ✅
```

---

## 🎯 Si Tout Échoue

### Plan B : Version Simplifiée (Sans Graphiques)

Modifier `Dashboard.jsx` :

```javascript
const handleDownload = async () => {
  setDownloading(true);
  try {
    // Générer seulement la base (sans graphiques)
    const doc = await generateDashboardPDF(data, range);
    
    // Sauvegarder directement
    savePDF(doc, `Rapport_Dashboard_${range.start}_${range.end}.pdf`);
    
    toast.success('Rapport PDF téléchargé !');
  } catch (err) {
    console.error(err);
    toast.error('Erreur: ' + err.message);
  } finally {
    setDownloading(false);
  }
};
```

Ceci génère un PDF avec :
- ✅ Page de garde
- ✅ KPI cards
- ✅ Tableau production
- ❌ Pas de graphiques (capturés)

---

## 📊 Résultat Attendu

### Structure PDF Finale

```
Page 1: ⭐ Page de garde
├─ Bande sombre avec "AutoReport"
├─ Titre "Rapport de Production"
├─ Dates
└─ Footer

Page 2: 📊 Contenu
├─ 4 KPI Cards colorés
├─ Tableau production par ligne
└─ Footer

Page 3: 📈 Graphiques
├─ Production journalière (courbe)
├─ Production par ligne (barres)
└─ Footer
```

---

**Le PDF devrait maintenant fonctionner ! 🎉**

Si problème persiste, copiez l'erreur exacte de la console et je vous aide ! 😊
