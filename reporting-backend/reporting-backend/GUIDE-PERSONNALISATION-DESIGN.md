# 🎨 Guide Complet : Personnaliser le Design des Rapports

## 📋 Vue d'Ensemble

| Type | Outil | Difficulté | Fichier |
|------|-------|-----------|---------|
| **PDF** | JasperReports (.jrxml) | Facile avec éditeur | `production_report_enhanced.jrxml` |
| **Excel** | Apache POI (Java) | Moyen | `ReportGenerationService.java` |

---

## 📄 PERSONNALISATION PDF

### 🛠️ Option 1 : Jaspersoft Studio (Recommandé)

**C'est un éditeur visuel drag-and-drop gratuit !**

#### Installation :
1. Télécharger : https://community.jaspersoft.com/downloads
2. Installer Jaspersoft Studio
3. Ouvrir le fichier :
   ```
   reporting-backend/src/main/resources/reports/production_report_enhanced.jrxml
   ```

#### Ce Que Vous Pouvez Faire :
- ✅ Changer les couleurs (drag color picker)
- ✅ Déplacer les éléments (drag & drop)
- ✅ Redimensionner textes/images
- ✅ Ajouter nouveaux éléments
- ✅ Modifier les polices
- ✅ Ajouter des graphiques
- ✅ Prévisualiser en temps réel

#### Workflow :
```
1. Ouvrir .jrxml dans Jaspersoft Studio
2. Modifier visuellement
3. Sauvegarder (Ctrl+S)
4. Recompiler backend : mvn clean package -Dmaven.test.skip=true
5. Redémarrer backend
6. Générer nouveau rapport → Design mis à jour !
```

---

### ✏️ Option 2 : Modifier le XML Directement

#### Fichier :
```
reporting-backend/src/main/resources/reports/production_report_enhanced.jrxml
```

#### Exemples de Modifications Rapides :

##### 1. Changer Couleur de Fond En-Tête

**Actuel (Bleu) :**
```xml
<rectangle>
    <reportElement x="0" y="0" width="535" height="200" backcolor="#667EEA"/>
</rectangle>
```

**Changer en Vert :**
```xml
<rectangle>
    <reportElement x="0" y="0" width="535" height="200" backcolor="#10B981"/>
</rectangle>
```

**Codes couleurs populaires :**
- Bleu SEWS : `#667EEA`
- Vert : `#10B981`
- Rouge : `#EF4444`
- Violet : `#8B5CF6`
- Gris foncé : `#1F2937`

##### 2. Changer Police

**Cherchez :**
```xml
<font size="22" isBold="true" fontName="Arial" forecolor="#FFFFFF"/>
```

**Modifiez :**
```xml
<font size="24" isBold="true" fontName="Helvetica" forecolor="#000000"/>
```

##### 3. Modifier Texte

**Cherchez :**
```xml
<staticText>
    <text><![CDATA[AutoReport SYSTEM]]></text>
</staticText>
```

**Remplacez par :**
```xml
<staticText>
    <text><![CDATA[SEWS Production Analytics]]></text>
</staticText>
```

##### 4. Ajuster Marges

**En haut du fichier :**
```xml
<jasperReport 
    pageWidth="595" 
    pageHeight="842" 
    leftMargin="30"      <!-- ← Changez ici -->
    rightMargin="30"     <!-- ← Changez ici -->
    topMargin="30"       <!-- ← Changez ici -->
    bottomMargin="30"    <!-- ← Changez ici -->
>
```

---

## 📊 PERSONNALISATION EXCEL

### 📝 Fichier à Modifier :
```
reporting-backend/src/main/java/com/sews/reporting/service/ReportGenerationService.java
```

### 🎨 Styles Disponibles :

#### 1. Couleur de Fond

```java
CellStyle style = workbook.createCellStyle();
style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
```

**Couleurs disponibles :**
- `IndexedColors.BLUE`
- `IndexedColors.GREEN`
- `IndexedColors.RED`
- `IndexedColors.YELLOW`
- `IndexedColors.GREY_25_PERCENT`
- `IndexedColors.LIGHT_GREEN`

#### 2. Police et Taille

```java
Font font = workbook.createFont();
font.setBold(true);
font.setFontHeightInPoints((short) 14);
font.setFontName("Arial");
font.setColor(IndexedColors.WHITE.getIndex());
style.setFont(font);
```

#### 3. Bordures

```java
style.setBorderBottom(BorderStyle.THIN);
style.setBorderTop(BorderStyle.THIN);
style.setBorderLeft(BorderStyle.THIN);
style.setBorderRight(BorderStyle.THIN);
```

#### 4. Alignement

```java
style.setAlignment(HorizontalAlignment.CENTER);
style.setVerticalAlignment(VerticalAlignment.CENTER);
```

#### 5. Largeur Colonnes

```java
sheet.setColumnWidth(0, 5000);  // 5000 unités
sheet.setColumnWidth(1, 3000);
// OU auto-size :
sheet.autoSizeColumn(0);
```

### 📚 Exemple Complet

Voir le fichier **`EXEMPLE-DESIGN-EXCEL.java`** pour un exemple complet avec :
- ✅ En-tête coloré
- ✅ KPIs avec fond vert
- ✅ Tableau avec bordures
- ✅ Colonnes dimensionnées

---

## 🚀 Workflow Complet de Personnalisation

### Pour PDF :

```bash
1. Ouvrir Jaspersoft Studio
2. File → Open → production_report_enhanced.jrxml
3. Modifier design visuellement
4. Sauvegarder
5. cd reporting-backend
6. mvn clean package -Dmaven.test.skip=true
7. Redémarrer backend
8. Générer rapport test → Vérifier design
```

### Pour Excel :

```bash
1. Ouvrir ReportGenerationService.java dans votre IDE
2. Trouver la méthode generateExcelReport()
3. Modifier les styles (voir exemples ci-dessus)
4. Sauvegarder
5. mvn clean package -Dmaven.test.skip=true
6. Redémarrer backend
7. Générer rapport test → Vérifier design
```

---

## 💡 Conseils Pro

### Design PDF :
- ✅ Utilisez Jaspersoft Studio pour éviter erreurs XML
- ✅ Prévisualisez avant de compiler
- ✅ Gardez une copie de backup du .jrxml original
- ✅ Testez sur différentes tailles de données

### Design Excel :
- ✅ Testez les couleurs (certaines peuvent être différentes dans Excel)
- ✅ Utilisez `autoSizeColumn()` pour ajustement automatique
- ✅ Ajoutez des commentaires dans votre code
- ✅ Créez une méthode séparée pour les styles

---

## 🎨 Templates Pré-faits

### Template 1 : Corporate Blue (Actuel)
- En-tête bleu (#667EEA)
- Police Arial
- Design professionnel

### Template 2 : Modern Green
```
Couleurs :
- En-tête : #10B981 (Vert)
- Accents : #059669 (Vert foncé)
- Fond : #F0FDF4 (Vert très clair)
```

### Template 3 : Executive Dark
```
Couleurs :
- En-tête : #1F2937 (Gris foncé)
- Accents : #6B7280 (Gris moyen)
- Fond : #F3F4F6 (Gris clair)
```

---

## 📞 Ressources

### Documentation :
- **JasperReports** : https://community.jaspersoft.com/documentation
- **Apache POI** : https://poi.apache.org/components/spreadsheet/
- **Codes couleurs** : https://htmlcolorcodes.com/

### Tutoriels :
- **Jaspersoft Studio** : https://www.youtube.com/results?search_query=jaspersoft+studio+tutorial
- **Apache POI Excel** : https://www.baeldung.com/java-microsoft-excel

---

## ✅ Checklist Personnalisation

### Avant de Commencer :
- [ ] Backup des fichiers originaux
- [ ] Jaspersoft Studio installé (pour PDF)
- [ ] IDE Java ouvert (pour Excel)

### Modifications PDF :
- [ ] Couleurs changées
- [ ] Logo mis à jour
- [ ] Polices modifiées
- [ ] Textes personnalisés
- [ ] Preview test OK

### Modifications Excel :
- [ ] Styles définis
- [ ] Couleurs appliquées
- [ ] Bordures ajoutées
- [ ] Colonnes dimensionnées
- [ ] Test génération OK

### Déploiement :
- [ ] Code compilé sans erreurs
- [ ] Backend redémarré
- [ ] Rapport test généré
- [ ] Design validé
- [ ] Documentation mise à jour

---

**Vous avez maintenant tout pour créer des rapports au design professionnel et personnalisé ! 🎨✨**
