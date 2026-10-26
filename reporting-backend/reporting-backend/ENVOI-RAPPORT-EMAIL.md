# 📧 Comment Envoyer un Rapport à un Email Spécifique

## 🎯 Fonctionnalité Ajoutée

Vous pouvez maintenant **envoyer un rapport existant à n'importe quel email**, sans avoir à ajouter cet email à la liste permanente des destinataires.

---

## 🔧 Comment Utiliser (Backend)

### Endpoint API Créé :
```
POST /api/reports/{id}/send-to?email=destinataire@example.com&recipientName=Nom (optionnel)
```

### Paramètres :
- **{id}** : ID du rapport (dans l'URL)
- **email** : Email du destinataire (obligatoire)
- **recipientName** : Nom du destinataire (optionnel)

### Permissions :
- **ADMIN** : Peut envoyer n'importe quel rapport
- **MANAGER** : Peut envoyer les rapports de son département

---

## 💻 Test avec PowerShell

### Test Automatique :
```powershell
cd reporting-backend
.\test-envoi-ponctuel.ps1
```

Le script va :
1. Se connecter en tant qu'admin
2. Lister les rapports disponibles
3. Vous demander l'email destinataire
4. Envoyer le rapport

### Test Manuel (avec curl) :
```powershell
# 1. Login
$body = '{"email":"admin@sews.com","password":"Admin@123"}'
$response = curl http://localhost:8080/api/auth/login -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
$token = ($response.Content | ConvertFrom-Json).token

# 2. Envoyer rapport ID 1 à un email
curl "http://localhost:8080/api/reports/1/send-to?email=fatimabenritounia37@gmail.com&recipientName=Fatima" `
  -Method POST `
  -Headers @{Authorization="Bearer $token"} `
  -UseBasicParsing
```

---

## 🌐 Utilisation depuis le Frontend (React)

### Code à ajouter dans la page Rapports :

```javascript
// Fonction d'envoi ponctuel
const sendReportToEmail = async (reportId, email, recipientName) => {
  try {
    const params = new URLSearchParams({
      email: email,
      ...(recipientName && { recipientName })
    });
    
    const response = await api.post(
      `/api/reports/${reportId}/send-to?${params}`,
      null,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    
    toast.success(response.data.message);
  } catch (error) {
    toast.error(error.response?.data?.message || 'Erreur envoi email');
  }
};

// Bouton dans la liste des rapports
<button onClick={() => {
  const email = prompt('Email destinataire :');
  const name = prompt('Nom destinataire (optionnel) :');
  if (email) {
    sendReportToEmail(report.id, email, name);
  }
}}>
  📧 Envoyer à un email
</button>
```

### Ou avec Modal :
```javascript
// Modal d'envoi
const [showSendModal, setShowSendModal] = useState(false);
const [selectedReport, setSelectedReport] = useState(null);
const [recipientEmail, setRecipientEmail] = useState('');
const [recipientName, setRecipientName] = useState('');

<Modal show={showSendModal} onHide={() => setShowSendModal(false)}>
  <Modal.Header>
    <Modal.Title>Envoyer le rapport par email</Modal.Title>
  </Modal.Header>
  <Modal.Body>
    <Form>
      <Form.Group>
        <Form.Label>Email destinataire *</Form.Label>
        <Form.Control
          type="email"
          value={recipientEmail}
          onChange={(e) => setRecipientEmail(e.target.value)}
          placeholder="exemple@sews.com"
          required
        />
      </Form.Group>
      <Form.Group className="mt-3">
        <Form.Label>Nom destinataire (optionnel)</Form.Label>
        <Form.Control
          type="text"
          value={recipientName}
          onChange={(e) => setRecipientName(e.target.value)}
          placeholder="Jean Dupont"
        />
      </Form.Group>
    </Form>
  </Modal.Body>
  <Modal.Footer>
    <Button variant="secondary" onClick={() => setShowSendModal(false)}>
      Annuler
    </Button>
    <Button 
      variant="primary" 
      onClick={() => {
        sendReportToEmail(selectedReport.id, recipientEmail, recipientName);
        setShowSendModal(false);
      }}
      disabled={!recipientEmail}
    >
      📧 Envoyer
    </Button>
  </Modal.Footer>
</Modal>
```

---

## 📋 Scénarios d'Utilisation

### Scénario 1 : Envoyer à un Manager Externe
```
1. Admin génère un rapport
2. Clique sur "Envoyer à un email"
3. Entre : manager.externe@partenaire.com
4. Le rapport PDF est envoyé par email
```

### Scénario 2 : Partager avec un Employé
```
1. Manager sélectionne un rapport de son département
2. Envoie à : employe@sews.com
3. L'employé reçoit le PDF par email (même s'il n'a pas accès à l'app)
```

### Scénario 3 : Envoyer à Plusieurs Personnes
```
Option 1 : Utiliser la liste permanente (Paramètres > Destinataires)
Option 2 : Envoyer manuellement à chaque personne avec cet endpoint
```

---

## ✅ Avantages

| Avec Liste Permanente | Avec Envoi Ponctuel |
|----------------------|---------------------|
| ✅ Envoi automatique | ✅ Envoi à la demande |
| ✅ Tous les rapports | ✅ Un rapport spécifique |
| ❌ Doit ajouter/retirer | ✅ Pas besoin d'ajouter |
| Bon pour : Rapports planifiés | Bon pour : Partage ponctuel |

---

## 🔒 Sécurité

- ✅ **Authentification requise** (JWT)
- ✅ **Permissions vérifiées** (ADMIN/MANAGER uniquement)
- ✅ **Logs d'audit** (Qui a envoyé quoi à qui)
- ✅ **Validation email** (format vérifié)
- ✅ **Fichier existe** (vérifié avant envoi)

---

## 📊 Logs d'Audit

Chaque envoi est enregistré dans la table `audit_logs` :
```
Action: SEND_EMAIL
Description: Envoi ponctuel de rapport
Details: Rapport: Production Juillet 2026, Destinataire: fatima@gmail.com
User: admin@sews.com
Timestamp: 2026-08-03 20:30:15
```

---

## 🎉 Résumé

**Avant :** Il fallait ajouter chaque email à la liste permanente dans Paramètres.

**Maintenant :** Vous pouvez envoyer n'importe quel rapport à n'importe quel email, directement, sans configuration préalable !

**Cas d'usage parfait :** 
- Partager un rapport avec un visiteur/auditeur externe
- Envoyer un rapport spécifique à un employé qui ne l'a pas demandé
- Transférer un rapport à un collègue pour validation

**Le backend est prêt, il faut maintenant ajouter le bouton dans le frontend React ! 🚀**
