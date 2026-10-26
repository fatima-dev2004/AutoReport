# 🔧 Correction Formulaire Objectifs - Frontend

## ❌ Problème Actuel

Le formulaire affiche les valeurs en **français** mais le backend attend des valeurs en **anglais** et dans un format spécifique.

---

## ✅ Mapping Correct Frontend → Backend

### Champ "Période"

**Ce que le formulaire affiche :**
- Quotidien
- Hebdomadaire  
- Mensuel
- Trimestriel
- Annuel

**Ce que le backend attend (ENUM) :**
```javascript
const periodMapping = {
  'Quotidien': 'DAILY',
  'Hebdomadaire': 'WEEKLY',
  'Mensuel': 'MONTHLY',
  'Trimestriel': 'QUARTERLY',
  'Annuel': 'YEARLY'
};
```

---

## 📝 Code Frontend à Corriger

### Option 1 : Select avec Valeurs Correctes

```jsx
<select name="periodType" value={formData.periodType} onChange={handleChange}>
  <option value="">Sélectionner période</option>
  <option value="DAILY">Quotidien</option>
  <option value="WEEKLY">Hebdomadaire</option>
  <option value="MONTHLY">Mensuel</option>
  <option value="QUARTERLY">Trimestriel</option>
  <option value="YEARLY">Annuel</option>
</select>
```

### Option 2 : Conversion Avant Envoi

```javascript
const handleSubmit = async (e) => {
  e.preventDefault();
  
  // Mapping français → anglais
  const periodMapping = {
    'Quotidien': 'DAILY',
    'Hebdomadaire': 'WEEKLY',
    'Mensuel': 'MONTHLY',
    'Trimestriel': 'QUARTERLY',
    'Annuel': 'YEARLY'
  };
  
  const payload = {
    line: formData.line,                                    // "Ligne A"
    periodType: periodMapping[formData.periode] || 'MONTHLY', // Convertir !
    startDate: formData.startDate,                          // "2026-08-09"
    endDate: formData.endDate,                              // "2026-08-31"
    targetQuantity: parseInt(formData.targetQuantity),      // 2345
    targetQualityRate: parseFloat(formData.targetQualityRate), // 95.0
    maxDefects: parseInt(formData.maxDefects),              // 5
    active: true,
    notes: formData.notes || ""
  };
  
  console.log('Payload envoyé:', payload);
  
  try {
    const response = await axios.post(
      'http://localhost:8080/api/objectives',
      payload,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    console.log('Succès:', response.data);
    // Fermer modal, rafraîchir liste, etc.
    
  } catch (error) {
    console.error('Erreur:', error.response?.data);
    alert('Erreur: ' + (error.response?.data?.message || error.message));
  }
};
```

---

## 🎯 JSON Attendu par le Backend

```json
{
  "line": "Ligne A",
  "periodType": "MONTHLY",
  "startDate": "2026-08-09",
  "endDate": "2026-08-31",
  "targetQuantity": 2345,
  "targetQualityRate": 95.0,
  "maxDefects": 5,
  "active": true,
  "notes": ""
}
```

### ⚠️ Points Importants

1. **`periodType`** (pas `periode` ni `period`) → MAJUSCULES anglaises
2. **`targetQuantity`** (camelCase) → nombre entier
3. **`targetQualityRate`** (camelCase) → nombre décimal
4. **`maxDefects`** (camelCase) → nombre entier
5. **`startDate`** / **`endDate`** → format "YYYY-MM-DD"
6. **`active`** → toujours `true` pour création

---

## 🔍 Comment Vérifier (DevTools)

1. **Ouvrir DevTools** (F12)
2. **Aller dans Network** > XHR
3. **Créer un objectif**
4. **Cliquer sur la requête** `POST /api/objectives`
5. **Regarder "Request Payload"**

**Bon payload :**
```json
{
  "periodType": "MONTHLY"  ✅
}
```

**Mauvais payload :**
```json
{
  "periodType": null  ❌
  // OU
  "periode": "Mensuel"  ❌
  // OU absent
}
```

---

## 📋 Checklist de Correction

### Dans le Composant React/Vue

- [ ] Nom du champ : `periodType` (pas `periode`)
- [ ] Valeurs du select : `DAILY`, `WEEKLY`, `MONTHLY`, etc. (pas français)
- [ ] State correctement initialisé
- [ ] onChange met bien à jour le state
- [ ] Conversion français→anglais si nécessaire

### Dans la Fonction handleSubmit

- [ ] Tous les champs présents dans le payload
- [ ] Bons noms (camelCase anglais)
- [ ] Types corrects (parseInt, parseFloat)
- [ ] Mapping période français→anglais si besoin
- [ ] Console.log du payload avant envoi

### Test

- [ ] DevTools Network ouvert
- [ ] Voir le payload JSON
- [ ] Vérifier `periodType` présent et = "MONTHLY" (ou autre)
- [ ] Si OK mais erreur persiste → problème backend
- [ ] Si absent/null → corriger frontend

---

## 🚀 Solution Rapide (Copier-Coller)

**Remplacez votre fonction handleSubmit par celle-ci :**

```javascript
const handleCreateObjective = async (formData) => {
  try {
    // Mapping période français → anglais
    const periodMapping = {
      'Quotidien': 'DAILY',
      'Hebdomadaire': 'WEEKLY',
      'Mensuel': 'MONTHLY',
      'Trimestriel': 'QUARTERLY',
      'Annuel': 'YEARLY'
    };
    
    // Construire le payload EXACT
    const payload = {
      line: formData.line || formData.ligne,
      periodType: formData.periodType || periodMapping[formData.periode] || 'MONTHLY',
      startDate: formData.startDate || formData.dateDebut,
      endDate: formData.endDate || formData.dateFin,
      targetQuantity: parseInt(formData.targetQuantity || formData.quantiteCible || 0),
      targetQualityRate: parseFloat(formData.targetQualityRate || formData.tauxQualiteCible || 95),
      maxDefects: parseInt(formData.maxDefects || formData.tauxDefautMax || 0),
      active: true,
      notes: formData.notes || ""
    };
    
    // LOG IMPORTANT pour debug
    console.log('📤 Payload envoyé au backend:', JSON.stringify(payload, null, 2));
    
    const token = localStorage.getItem('token');
    const response = await axios.post(
      'http://localhost:8080/api/objectives',
      payload,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    console.log('✅ Objectif créé:', response.data);
    alert('Objectif créé avec succès !');
    
    // Fermer modal, rafraîchir liste
    // ...
    
  } catch (error) {
    console.error('❌ Erreur création:', error.response?.data || error);
    const message = error.response?.data?.message || error.message;
    alert('Erreur: ' + message);
  }
};
```

---

## 📞 Si Ça Ne Marche Toujours Pas

Envoyez-moi :

1. **Le JSON du payload** (visible dans DevTools > Network)
2. **Le code de votre formulaire** (le JSX/HTML)
3. **Le code de handleSubmit** (la fonction d'envoi)
4. **L'erreur exacte** du backend

---

**90% des bugs viennent du mapping français/anglais ! 😊**
