# 🧪 Guide de Test - Nouvelles Fonctionnalités

## 🚀 Démarrage Rapide

```bash
# 1. S'assurer que le backend est démarré
# (Dans le terminal backend)
mvn spring-boot:run

# 2. Démarrer le frontend
npm run dev

# 3. Ouvrir http://localhost:5173
```

---

## ✅ Tests des Nouvelles Fonctionnalités

### 1. 🤖 Chat IA (NOUVEAU)

#### Test Basique
1. **Se connecter** avec n'importe quel compte (admin/manager/employe)
2. **Cliquer** sur "Chat IA" dans le menu (4ème position)
3. **Vérifier** que la page s'affiche avec :
   - Message de bienvenue de l'IA
   - Votre avatar en haut à droite
   - Zone de saisie en bas
   - Suggestions rapides

#### Test Conversation
1. **Cliquer** sur une suggestion rapide, par exemple "Comment générer un rapport ?"
2. **Attendre** l'animation "L'IA réfléchit..."
3. **Vérifier** que la réponse s'affiche
4. **Taper** une nouvelle question : "Comment importer des données Excel ?"
5. **Envoyer** avec le bouton ou Entrée
6. **Vérifier** que l'IA répond correctement

#### Test Interface
1. **Vérifier** que les messages s'affichent :
   - Messages utilisateur : bulles bleues à droite
   - Messages IA : bulles grises à gauche
   - Horodatage sous chaque message
2. **Cliquer** "Nouvelle conversation"
3. **Vérifier** que l'historique est effacé

#### Test Responsive
1. **Réduire** la fenêtre du navigateur
2. **Vérifier** que l'interface s'adapte
3. **Tester** sur mobile (F12 → mode responsive)

---

### 2. 👥 Gestion Utilisateurs Améliorée

#### Test Accès
1. **Se connecter** en tant qu'ADMIN
2. **Cliquer** sur "Utilisateurs" dans le menu
3. **Vérifier** que la page affiche le tableau

#### Test Création Utilisateur
1. **Cliquer** sur "Nouvel Utilisateur"
2. **Vérifier** que le modal s'ouvre
3. **Remplir le formulaire** :
   ```
   Prénom: Jean
   Nom: Dupont
   Email: jean.dupont@sews.com
   Téléphone: +212 6 12 34 56 78
   Âge: 35
   Mot de passe: Test@1234
   Rôle: USER
   Statut: Actif ✓
   ```
4. **Cliquer** "Créer l'utilisateur"
5. **Vérifier** :
   - Toast de succès s'affiche
   - Modal se ferme
   - Nouveau user apparaît dans le tableau

#### Test Affichage Tableau
1. **Vérifier** que le tableau affiche :
   - Avatar avec initiale "J"
   - Nom "Jean Dupont"
   - Email avec icône 📧
   - Téléphone avec icône 📱
   - Âge "35 ans" avec icône 📅
   - Badge "Employé" (gris)
   - Statut "Actif" (vert)
   - **Droits d'accès** :
     - ✓ Dashboard
     - ✓ Rapports (lecture)

#### Test Modification
1. **Cliquer** sur l'icône "Modifier" (crayon)
2. **Vérifier** que le modal s'ouvre avec les données
3. **Modifier** le téléphone
4. **Cliquer** "Mettre à jour"
5. **Vérifier** le toast de succès

#### Test Activation/Désactivation
1. **Cliquer** sur l'icône cadenas
2. **Vérifier** :
   - Toast de confirmation
   - Statut change à "Désactivé" (rouge)
   - Droits d'accès restent affichés
3. **Cliquer** à nouveau pour réactiver

#### Test Recherche
1. **Taper** "jean" dans la barre de recherche
2. **Vérifier** que seul Jean Dupont s'affiche
3. **Effacer** la recherche
4. **Vérifier** que tous les users réapparaissent

#### Test Suppression
1. **Cliquer** sur l'icône poubelle
2. **Confirmer** la suppression
3. **Vérifier** :
   - Toast de succès
   - User disparaît du tableau

---

### 3. 📧 Envoi Rapport par Email (À FINALISER)

> **Note**: Cette fonctionnalité nécessite le backend configuré pour l'envoi d'emails

#### Test Préparatoire
1. **Aller** sur `/settings`
2. **Configurer** SMTP (ou utiliser config existante)
3. **Ajouter** des destinataires

#### Test Bouton Email
1. **Aller** sur `/reports`
2. **Trouver** un rapport dans la liste
3. **Vérifier** présence de 3 boutons :
   - 📄 Télécharger PDF
   - 📊 Télécharger Excel
   - 📧 **Envoyer par Email** (nouveau)

#### Test Modal Email (quand implémenté)
1. **Cliquer** sur l'icône email
2. **Vérifier** que le modal s'ouvre avec :
   - Liste des destinataires prédéfinis
   - Champ pour ajouter d'autres emails
   - Zone de texte pour message personnalisé
   - Aperçu du rapport
3. **Sélectionner** des destinataires
4. **Ajouter** un message
5. **Cliquer** "Envoyer"
6. **Vérifier** :
   - Toast "Email envoyé"
   - Modal se ferme

#### Test Audit Log
1. **Aller** sur `/audit`
2. **Filtrer** par type "Emails"
3. **Vérifier** que l'envoi est enregistré

---

## 🎯 Scénario de Test Complet

### Scénario : Admin Crée un Manager et Teste le Chat

**Durée estimée : 10 minutes**

#### Étape 1 : Connexion Admin
```
1. Ouvrir http://localhost:5173
2. Email: admin@sews.com
3. Password: Admin@123
4. Cliquer "Se connecter"
✓ Redirection vers Dashboard
```

#### Étape 2 : Créer un Nouveau Manager
```
1. Cliquer "Utilisateurs" dans le menu
2. Cliquer "Nouvel Utilisateur"
3. Remplir :
   - Prénom: Marie
   - Nom: Martin
   - Email: marie.martin@sews.com
   - Téléphone: +212 6 98 76 54 32
   - Âge: 42
   - Mot de passe: Manager@2024
   - Rôle: MANAGER
   - Actif: ✓
4. Cliquer "Créer l'utilisateur"
✓ Toast succès
✓ Marie apparaît dans le tableau
✓ Badge "Responsable" (bleu)
✓ Droits : Dashboard, Import Excel, Rapports
```

#### Étape 3 : Vérifier les Droits
```
1. Localiser Marie dans le tableau
2. Lire la colonne "Droits d'accès"
✓ Affiche :
  - ✓ Dashboard
  - ✓ Import Excel
  - ✓ Rapports
```

#### Étape 4 : Tester le Chat IA
```
1. Cliquer "Chat IA" dans le menu
2. Lire le message de bienvenue
✓ Affiche "Bonjour [votre nom] !"
3. Cliquer sur suggestion "Comment générer un rapport ?"
✓ Animation "L'IA réfléchit..."
✓ Réponse s'affiche
4. Taper : "Comment ajouter un utilisateur ?"
5. Cliquer Send
✓ Réponse intelligente
6. Cliquer "Nouvelle conversation"
✓ Historique effacé
✓ Nouveau message de bienvenue
```

#### Étape 5 : Se Déconnecter et Tester Marie
```
1. Cliquer sur avatar en bas
2. Cliquer "Se déconnecter"
3. Se connecter avec marie.martin@sews.com / Manager@2024
4. Vérifier menu visible :
   ✓ Tableau de bord
   ✓ Rapports
   ✓ Import Excel
   ✓ Chat IA
   ✗ PAS Utilisateurs
   ✗ PAS Audit
   ✗ PAS Paramètres
5. Tester Chat IA
✓ Fonctionne pour Marie aussi
```

#### Étape 6 : Audit Log
```
1. Se reconnecter en ADMIN
2. Aller sur "Journal d'activité"
3. Vérifier événements enregistrés :
   ✓ Connexion admin
   ✓ Création utilisateur (Marie Martin)
   ✓ Déconnexion admin
   ✓ Connexion Marie
4. Filtrer par type "Connexions"
✓ Affiche seulement les connexions
5. Exporter CSV
✓ Téléchargement démarre
```

---

## 🐛 Problèmes Potentiels & Solutions

### Problème 1 : Chat IA ne répond pas
**Symptômes** : Cliquer Send ne fait rien

**Solutions** :
```bash
# Vérifier console navigateur (F12)
# S'il y a une erreur, vérifier :
1. Fichier AIChat.jsx existe dans src/pages/
2. Import dans App.jsx est correct
3. Route /chat est bien définie
4. Redémarrer le serveur dev : Ctrl+C puis npm run dev
```

### Problème 2 : Modal utilisateur ne s'ouvre pas
**Symptômes** : Cliquer "Nouvel Utilisateur" ne fait rien

**Solutions** :
```bash
# Vérifier :
1. Fichier Modal.jsx existe dans src/components/
2. Import { useToast } fonctionne
3. Console navigateur pour erreurs
4. Essayer de vider le cache : Ctrl+Shift+R
```

### Problème 3 : Droits d'accès ne s'affichent pas
**Symptômes** : Colonne droits vide

**Solutions** :
```bash
# Vérifier :
1. Les utilisateurs ont bien un rôle (ADMIN/MANAGER/USER)
2. Backend retourne le champ "role"
3. Consolelog users pour voir les données
```

### Problème 4 : Toast n'apparaît pas
**Symptômes** : Pas de notification après action

**Solutions** :
```bash
# Vérifier :
1. ToastProvider est dans main.jsx
2. useToast() est appelé correctement
3. Console pour erreurs
4. Vérifier src/components/Toast.jsx existe
```

---

## 📊 Checklist de Test

### Tests Fonctionnels
- [ ] Login fonctionne (admin/manager/user)
- [ ] Dashboard affiche KPIs
- [ ] Import Excel valide fichier
- [ ] Rapports filtrent et paginent
- [ ] **Chat IA répond aux questions** ⭐
- [ ] **Modal utilisateur s'ouvre** ⭐
- [ ] **Créer utilisateur fonctionne** ⭐
- [ ] **Tableau affiche droits d'accès** ⭐
- [ ] **Recherche filtre users** ⭐
- [ ] Activer/Désactiver user fonctionne
- [ ] Modifier user fonctionne
- [ ] Supprimer user fonctionne
- [ ] Audit log enregistre actions
- [ ] Profil modifiable
- [ ] Paramètres sauvegardent
- [ ] Logout fonctionne

### Tests Visuels
- [ ] Design cohérent partout
- [ ] Animations fluides
- [ ] Avatars s'affichent
- [ ] Badges colorés corrects
- [ ] Icônes visibles
- [ ] Pas d'éléments cassés
- [ ] Responsive fonctionne

### Tests Performance
- [ ] Chargement rapide
- [ ] Pas de lag
- [ ] Scroll fluide
- [ ] Recherche réactive (debounce)
- [ ] Pagination instantanée

---

## 🎓 Pour la Démo

### Préparation (30 min avant)
```bash
# 1. Nettoyer base de données
- Garder 3 comptes : admin, manager, employe
- Supprimer users de test précédents

# 2. Préparer fichier Excel
- Production derniers 30 jours
- Au moins 50 lignes
- Format correct

# 3. Générer quelques rapports
- 3-4 rapports avec différentes périodes
- Vérifier téléchargement PDF/Excel

# 4. Tester une fois tout le parcours
- Login → Dashboard → Import → Chat → Users → Logout
- Chronométrer : doit tenir en 12 min max
```

### Ordre de Démonstration
```
1. Login (30 sec)
2. Dashboard (1 min)
3. Import Excel (1 min)
4. Chat IA ⭐ (2 min) - POINT FORT
5. Gestion Users ⭐ (2 min) - POINT FORT
6. Rapports + Filtres (1 min)
7. Audit Log (30 sec)
8. Profil (30 sec)
9. Paramètres (30 sec)
Total : ~9 min → Garde 3 min de marge
```

---

## ✅ Validation Finale

**Tout fonctionne ?** Cochez cette checklist :

- [ ] ✅ Chat IA répond intelligemment
- [ ] ✅ Users créés avec 8 champs
- [ ] ✅ Droits d'accès affichés par rôle
- [ ] ✅ Tableau utilisateurs complet
- [ ] ✅ Recherche filtre users
- [ ] ✅ Toasts s'affichent partout
- [ ] ✅ Aucune erreur console critique
- [ ] ✅ Design professionnel partout
- [ ] ✅ Responsive fonctionne
- [ ] ✅ Documenté et compréhensible

**Si tout est coché → PRÊT POUR LA SOUTENANCE ! 🎉**

---

*Guide créé pour AutoReport System - SEWS CABIND MAROC 2024*
