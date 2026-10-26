# 🚀 GUIDE DE TEST RAPIDE

## ⚡ TEST EN 3 MINUTES

### 1️⃣ Démarrer (30 secondes)

```bash
cd autoreport-frontend
npm run dev
```

Ouvrir : `http://localhost:5173`

---

### 2️⃣ Tester Export (1 minute)

#### Excel
1. Aller sur **Dashboard**
2. Cliquer **📊 Excel**
3. ✅ Fichier `.xlsx` téléchargé
4. Ouvrir → Vérifier 3 feuilles

#### PDF
1. Cliquer **📄 PDF**
2. ✅ Fichier `.pdf` téléchargé
3. Ouvrir → Vérifier :
   - Page 1 : Logo + Titre
   - Page 2 : KPI + Tableau
   - Page 3 : Graphiques

---

### 3️⃣ Tester Langues (30 secondes)

1. Regarder **en bas de la sidebar**
2. Cliquer **🌐 FR**
3. ✅ Interface passe en anglais
4. Cliquer **🌐 EN**
5. ✅ Retour en français

**Vérifier** :
- Navigation traduite
- Dashboard traduit
- Boutons traduits

---

### 4️⃣ Tester Thème (30 secondes)

1. Regarder **en bas de la sidebar**
2. Cliquer **🌙 Dark**
3. ✅ Interface devient sombre
4. Cliquer **☀️ Light**
5. ✅ Retour au mode clair

**Vérifier** :
- Fond sombre
- Texte clair
- Panels adaptés

---

### 5️⃣ Test Combiné (30 secondes)

1. Activer **mode sombre**
2. Passer en **anglais**
3. Télécharger **PDF**
4. ✅ Tout fonctionne ensemble !

---

## ✅ CHECKLIST RAPIDE

- [ ] Excel télécharge (3 feuilles)
- [ ] PDF télécharge (3 pages)
- [ ] FR/EN bascule
- [ ] Dark/Light bascule
- [ ] Sauvegarde après F5

---

## 🎯 POSITION DES BOUTONS

```
Dashboard (en haut à droite)
┌──────────────────────────────┐
│  [Date] → [Date] [Excel] [PDF] │
└──────────────────────────────┘

Sidebar (en bas)
┌──────────────┐
│  🌐 FR | 🌙  │ ← Langue + Thème
├──────────────┤
│  👤 Profil   │
│  🚪 Logout   │
└──────────────┘
```

---

## 🐛 SI ÇA NE MARCHE PAS

### Excel vide ?
→ Vérifier que des données existent dans Dashboard

### PDF blanc ?
→ Ouvrir Console (F12) → Chercher erreurs logo

### Langue ne change pas ?
→ F5 (actualiser) → Vérifier localStorage

### Thème ne change pas ?
→ F5 (actualiser) → Vérifier class 'dark' sur <html>

---

## 📦 FICHIERS À VÉRIFIER

```
✅ src/utils/excelGenerator.js
✅ src/utils/pdfGenerator.js
✅ src/context/LanguageContext.jsx
✅ src/context/ThemeContext.jsx
✅ src/locales/fr.json
✅ src/locales/en.json
✅ src/components/Layout.jsx
✅ src/pages/Dashboard.jsx
✅ tailwind.config.js (darkMode: 'class')
✅ src/index.css (styles .dark)
```

---

## 💡 ASTUCE DÉMONSTRATION

**Ordre recommandé** :
1. Montrer Dashboard normal
2. Télécharger PDF (ouvrir)
3. Télécharger Excel (ouvrir)
4. Passer en anglais
5. Activer mode sombre
6. Télécharger PDF en dark + EN

**Effet WOW garanti ! 🌟**

---

**Durée totale : 3 minutes**  
**Toutes les fonctionnalités testées ! ✅**
