# 📥 Guide Frontend - Téléchargement Rapport depuis Dashboard

**Date:** 20 juillet 2026  
**Version:** 2.0

---

## 🎯 Nouveaux Endpoints pour Dashboard

### 1. Téléchargement Direct (Recommandé)

**Endpoint:**
```
POST /api/dashboard/download-report
```

**Description:** Génère ET télécharge immédiatement le rapport PDF

**Paramètres:**
- `start` (optionnel) - Date de début (format: YYYY-MM-DD)
- `end` (optionnel) - Date de fin (défaut: aujourd'hui)
- `enhanced` (optionnel) - true/false (défaut: true pour rapport amélioré avec page de garde)
- `title` (optionnel) - Titre personnalisé

**Réponse:** Fichier PDF en téléchargement direct

---

### 2. Génération puis Téléchargement (Deux étapes)

**Endpoint:**
```
POST /api/dashboard/generate-report
```

**Description:** Génère le rapport et retourne l'ID pour téléchargement ultérieur

**Paramètres:** Identiques à `download-report`

**Réponse:**
```json
{
  "success": true,
  "message": "Rapport généré avec succès",
  "reportId": 1,
  "title": "Rapport Dashboard - 20/07/2026",
  "downloadUrl": "/api/reports/1/download/pdf",
  "downloadUrlExcel": "/api/reports/1/download/excel"
}
```

---

## 💻 Implémentation Frontend

### Option 1: Téléchargement Direct (Simple)

**HTML - Bouton dans le Dashboard:**
```html
<button onclick="downloadReport()" class="btn btn-primary">
  📥 Télécharger Rapport PDF
</button>
```

**JavaScript:**
```javascript
async function downloadReport() {
  const token = localStorage.getItem('token');
  
  // Utiliser les dates actuelles du dashboard
  const startDate = document.getElementById('startDate').value; // Ex: "2026-07-01"
  const endDate = document.getElementById('endDate').value;     // Ex: "2026-07-31"
  
  // Construire l'URL avec paramètres
  const params = new URLSearchParams({
    start: startDate,
    end: endDate,
    enhanced: 'true',  // Rapport amélioré avec page de garde
    title: `Rapport Production ${endDate}`
  });
  
  try {
    const response = await fetch(`http://localhost:8080/api/dashboard/download-report?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Erreur lors de la génération du rapport');
    }
    
    // Créer un blob et télécharger
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport_production_${endDate}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    // Notification de succès
    showNotification('Rapport téléchargé avec succès!', 'success');
    
  } catch (error) {
    console.error('Erreur:', error);
    showNotification('Erreur lors du téléchargement', 'error');
  }
}
```

---

### Option 2: Génération + Téléchargement (Deux étapes)

**Avantage:** Permet de sauvegarder le rapport dans l'historique avant téléchargement

**JavaScript:**
```javascript
async function generateAndDownloadReport() {
  const token = localStorage.getItem('token');
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;
  
  try {
    // Étape 1: Générer le rapport
    showLoading('Génération du rapport en cours...');
    
    const params = new URLSearchParams({
      start: startDate,
      end: endDate,
      enhanced: 'true',
      title: `Rapport Dashboard - ${new Date().toLocaleDateString('fr-FR')}`
    });
    
    const generateResponse = await fetch(`http://localhost:8080/api/dashboard/generate-report?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!generateResponse.ok) {
      throw new Error('Erreur lors de la génération');
    }
    
    const data = await generateResponse.json();
    
    // Étape 2: Télécharger le PDF généré
    const downloadUrl = `http://localhost:8080${data.downloadUrl}`;
    
    const downloadResponse = await fetch(downloadUrl, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!downloadResponse.ok) {
      throw new Error('Erreur lors du téléchargement');
    }
    
    const blob = await downloadResponse.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport_${data.reportId}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    hideLoading();
    showNotification(`Rapport #${data.reportId} téléchargé!`, 'success');
    
    // Option: Rediriger vers l'historique des rapports
    // window.location.href = '/reports';
    
  } catch (error) {
    console.error('Erreur:', error);
    hideLoading();
    showNotification('Erreur lors du traitement', 'error');
  }
}
```

---

## 🎨 Exemple d'Interface UI

### Bouton dans le Dashboard

```html
<div class="dashboard-actions">
  <!-- Bouton principal -->
  <button 
    onclick="downloadReport()" 
    class="btn btn-primary"
    title="Télécharger le rapport avec page de garde">
    <i class="fas fa-download"></i>
    Télécharger Rapport PDF
  </button>
  
  <!-- Bouton secondaire (optionnel) -->
  <button 
    onclick="downloadSimpleReport()" 
    class="btn btn-secondary"
    title="Télécharger rapport simple sans page de garde">
    <i class="fas fa-file-pdf"></i>
    Rapport Simple
  </button>
  
  <!-- Bouton Excel (optionnel) -->
  <button 
    onclick="downloadExcel()" 
    class="btn btn-success"
    title="Télécharger en Excel">
    <i class="fas fa-file-excel"></i>
    Excel
  </button>
</div>
```

**CSS:**
```css
.dashboard-actions {
  display: flex;
  gap: 10px;
  margin: 20px 0;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-success {
  background: #28a745;
  color: white;
}
```

---

## 📱 Version Complète avec Modal d'Options

### HTML
```html
<!-- Bouton qui ouvre le modal -->
<button onclick="openReportModal()" class="btn btn-primary">
  <i class="fas fa-download"></i>
  Générer Rapport
</button>

<!-- Modal -->
<div id="reportModal" class="modal" style="display: none;">
  <div class="modal-content">
    <span class="close" onclick="closeReportModal()">&times;</span>
    
    <h2>📊 Générer un Rapport</h2>
    
    <div class="form-group">
      <label>Période:</label>
      <div class="date-range">
        <input type="date" id="modalStartDate" class="form-control">
        <span>au</span>
        <input type="date" id="modalEndDate" class="form-control">
      </div>
    </div>
    
    <div class="form-group">
      <label>Titre personnalisé (optionnel):</label>
      <input type="text" id="modalTitle" class="form-control" 
             placeholder="Rapport Production Juillet 2026">
    </div>
    
    <div class="form-group">
      <label>
        <input type="checkbox" id="modalEnhanced" checked>
        Inclure page de garde et graphiques
      </label>
    </div>
    
    <div class="modal-actions">
      <button onclick="closeReportModal()" class="btn btn-secondary">
        Annuler
      </button>
      <button onclick="generateReportFromModal()" class="btn btn-primary">
        <i class="fas fa-download"></i>
        Télécharger PDF
      </button>
    </div>
  </div>
</div>
```

### JavaScript
```javascript
function openReportModal() {
  // Pré-remplir avec les dates actuelles
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;
  
  document.getElementById('modalStartDate').value = startDate;
  document.getElementById('modalEndDate').value = endDate;
  document.getElementById('reportModal').style.display = 'block';
}

function closeReportModal() {
  document.getElementById('reportModal').style.display = 'none';
}

async function generateReportFromModal() {
  const token = localStorage.getItem('token');
  const startDate = document.getElementById('modalStartDate').value;
  const endDate = document.getElementById('modalEndDate').value;
  const title = document.getElementById('modalTitle').value;
  const enhanced = document.getElementById('modalEnhanced').checked;
  
  const params = new URLSearchParams({
    start: startDate,
    end: endDate,
    enhanced: enhanced.toString()
  });
  
  if (title) {
    params.append('title', title);
  }
  
  try {
    showLoading('Génération en cours...');
    
    const response = await fetch(`http://localhost:8080/api/dashboard/download-report?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!response.ok) throw new Error('Erreur génération');
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport_${endDate}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    closeReportModal();
    hideLoading();
    showNotification('Rapport téléchargé avec succès!', 'success');
    
  } catch (error) {
    console.error(error);
    hideLoading();
    showNotification('Erreur lors du téléchargement', 'error');
  }
}

// Utilitaires
function showLoading(message) {
  // Afficher un spinner ou message de chargement
  console.log('Loading:', message);
}

function hideLoading() {
  // Cacher le spinner
}

function showNotification(message, type) {
  // Afficher notification (toast)
  alert(message); // Remplacer par un vrai système de notification
}
```

---

## 🔄 Fonction pour Rapport Simple (sans page de garde)

```javascript
async function downloadSimpleReport() {
  const token = localStorage.getItem('token');
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;
  
  const params = new URLSearchParams({
    start: startDate,
    end: endDate,
    enhanced: 'false'  // Rapport simple
  });
  
  try {
    const response = await fetch(`http://localhost:8080/api/dashboard/download-report?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!response.ok) throw new Error('Erreur');
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport_simple_${endDate}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    showNotification('Rapport simple téléchargé!', 'success');
    
  } catch (error) {
    console.error(error);
    showNotification('Erreur', 'error');
  }
}
```

---

## 📋 Récapitulatif des Endpoints

| Endpoint | Méthode | Description | Utilisation |
|----------|---------|-------------|-------------|
| `/api/dashboard/kpis` | GET | Données du dashboard | Affichage dashboard |
| `/api/dashboard/download-report` | POST | Télécharger PDF direct | **Bouton télécharger** |
| `/api/dashboard/generate-report` | POST | Générer + retourner ID | Historique + télécharger |
| `/api/reports/{id}/download/pdf` | GET | Télécharger PDF existant | Depuis historique |
| `/api/reports/{id}/download/excel` | GET | Télécharger Excel | Option Excel |

---

## ✅ Checklist d'Implémentation

### Étape 1: Bouton Simple
- [ ] Ajouter bouton "Télécharger Rapport" dans dashboard
- [ ] Implémenter fonction `downloadReport()`
- [ ] Tester téléchargement direct

### Étape 2: Options Avancées (Optionnel)
- [ ] Créer modal avec options
- [ ] Ajouter champs: période, titre, type
- [ ] Implémenter génération depuis modal

### Étape 3: Tests
- [ ] Tester avec période personnalisée
- [ ] Tester rapport amélioré (enhanced=true)
- [ ] Tester rapport simple (enhanced=false)
- [ ] Vérifier que le PDF contient les bonnes données
- [ ] Vérifier page de garde (logo, titre, etc.)

---

## 🎯 Recommandation

**Pour la version simple et rapide:**
Utilisez **Option 1** avec le bouton direct qui appelle `/api/dashboard/download-report`

**Pour plus de contrôle:**
Utilisez le **Modal avec options** pour permettre à l'utilisateur de personnaliser le rapport

---

**Version:** 2.0  
**Date:** 20 juillet 2026  
**Backend Status:** ✅ Prêt et fonctionnel
