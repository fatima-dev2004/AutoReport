# 🐛 Problème: Création d'Objectif Échoue

## ❌ Erreur
```
Column 'max_defects' cannot be found in production_objectives
```

## ✅ Vérifications Faites

1. **Colonne existe** dans la base ✅
   ```sql
   mysql> DESCRIBE production_objectives;
   -- max_defects | int | NO | | NULL
   ```

2. **Entité Java correcte** ✅
   - `ProductionObjective.java` a bien le champ `maxDefects`

3. **Backend démarré** ✅
   - Port 8080 actif

## 🔍 Cause Probable

Le **frontend envoie les données dans le mauvais format** ou avec des noms de champs incorrects.

## 🔧 Solution

### Test 1: Vérifier le JSON envoyé depuis le Frontend

Dans le navigateur, ouvrez **DevTools** (F12) > **Network** > **XHR** et regardez la requête POST vers `/api/objectives`.

Le JSON doit ressembler à ça :

```json
{
  "line": "Ligne B",
  "periodType": "WEEKLY",
  "startDate": "2026-08-06",
  "endDate": "2026-08-21",
  "targetQuantity": 45,
  "targetQualityRate": 95,
  "maxDefects": 5,
  "active": true,
  "notes": ""
}
```

### Test 2: Test Direct avec cURL

Testez l'endpoint directement :

```bash
curl -X POST http://localhost:8080/api/objectives \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -d '{
    "line": "Ligne Test",
    "periodType": "DAILY",
    "startDate": "2026-08-09",
    "endDate": "2026-08-10",
    "targetQuantity": 100,
    "targetQualityRate": 95.0,
    "maxDefects": 5,
    "active": true,
    "notes": "Test"
  }'
```

### Test 3: Vérifier le Code Frontend

Dans votre fichier frontend (probablement `Objectives.jsx` ou similaire), cherchez la fonction qui envoie les données :

```javascript
// ❌ MAUVAIS - noms en français ou mal écrits
const data = {
  ligne: formData.line,  // ❌ "ligne" au lieu de "line"
  quantite_cible: formData.targetQuantity,  // ❌ snake_case
  max_defauts: formData.maxDefects  // ❌ français
};

// ✅ BON - noms exacts en anglais camelCase
const data = {
  line: formData.line,
  periodType: formData.periodType,
  startDate: formData.startDate,
  endDate: formData.endDate,
  targetQuantity: parseInt(formData.targetQuantity),
  targetQualityRate: parseFloat(formData.targetQualityRate),
  maxDefects: parseInt(formData.maxDefects),
  active: true,
  notes: formData.notes || ""
};
```

## 🎯 Solution Rapide

**Dans votre fichier frontend Objectives.jsx**, trouvez la fonction `handleSubmit` ou `createObjective` et assurez-vous que le JSON envoyé correspond EXACTEMENT à ceci :

```javascript
const handleCreateObjective = async (formData) => {
  try {
    const token = localStorage.getItem('token');
    
    const payload = {
      line: formData.line,                                    // ✅
      periodType: formData.periodType,                        // ✅
      startDate: formData.startDate,                          // ✅
      endDate: formData.endDate,                              // ✅
      targetQuantity: parseInt(formData.targetQuantity),      // ✅
      targetQualityRate: parseFloat(formData.targetQualityRate), // ✅
      maxDefects: parseInt(formData.maxDefects),              // ✅
      active: true,                                           // ✅
      notes: formData.notes || ""                             // ✅
    };
    
    console.log('Envoi payload:', JSON.stringify(payload, null, 2));
    
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
    
    console.log('Objectif créé:', response.data);
    // Rafraîchir la liste, fermer modal, etc.
    
  } catch (error) {
    console.error('Erreur création objectif:', error.response?.data || error);
    alert('Erreur: ' + (error.response?.data?.message || error.message));
  }
};
```

## 📋 Checklist Debug

- [ ] Ouvrir DevTools (F12)
- [ ] Aller dans Network > XHR
- [ ] Créer un objectif
- [ ] Regarder la requête POST `/api/objectives`
- [ ] Vérifier le JSON dans "Request Payload"
- [ ] Comparer avec le format correct ci-dessus
- [ ] Corriger le code frontend si nécessaire

## 🔍 Si Le Problème Persiste

Envoyez-moi:
1. Le JSON exact envoyé (visible dans DevTools > Network)
2. Le code de votre fonction de création (handleSubmit ou similaire)
3. L'erreur complète du backend

---

**90% du temps, c'est juste un nom de champ mal écrit dans le frontend ! 😊**
