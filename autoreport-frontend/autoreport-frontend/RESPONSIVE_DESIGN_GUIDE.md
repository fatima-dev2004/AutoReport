# Guide de Responsive Design - AutoReport

Ce document explique comment le design responsive est implémenté dans AutoReport et comment maintenir cette responsivité.

## 📱 Breakpoints Tailwind

Le projet utilise les breakpoints standard de Tailwind CSS :

- **sm** : `640px` - Téléphones en paysage
- **md** : `768px` - Tablettes
- **lg** : `1024px` - Petits ordinateurs portables
- **xl** : `1280px` - Ordinateurs de bureau
- **2xl** : `1536px` - Grands écrans

## 🎯 Philosophie : Mobile First

Tous les styles sont définis pour mobile par défaut, puis ajustés pour les écrans plus grands :

```jsx
// ✅ Bon : Mobile first
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4">

// ❌ Mauvais : Desktop first
<div className="grid grid-cols-4 lg:grid-cols-2 md:grid-cols-1">
```

## 🏗️ Composants Responsive

### Layout avec Menu Mobile

Le composant `Layout.jsx` inclut maintenant :

#### Desktop (lg et +)
- Sidebar fixe à gauche (256px)
- Logo visible dans le sidebar
- Menu toujours visible

#### Mobile (< lg)
- Header fixe en haut avec hamburger menu
- Sidebar en overlay (slide-in depuis la gauche)
- Fermeture automatique après navigation
- Overlay sombre pour fermer le menu

```jsx
// Exemple d'utilisation
<div className="hidden lg:flex">Desktop seulement</div>
<div className="lg:hidden">Mobile seulement</div>
```

### Tables Responsives

Les tables défilent horizontalement sur mobile :

```jsx
<div className="overflow-x-auto">
  <table className="table-industrial min-w-[640px]">
    {/* Contenu du tableau */}
  </table>
</div>
```

**CSS automatique** :
```css
@media (max-width: 768px) {
  .table-industrial-wrapper {
    overflow-x: auto;
    margin: 0 -1rem;
    padding: 0 1rem;
  }
  
  .table-industrial {
    min-width: 640px;
  }
}
```

### Grilles Adaptatives

#### KPI Cards (Dashboard)

```jsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
  <KpiCard />
  <KpiCard />
  <KpiCard />
  <KpiCard />
</div>
```

Résultat :
- **Mobile** : 1 colonne
- **Tablette** : 2 colonnes
- **Desktop** : 4 colonnes

#### Formulaires

```jsx
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
  <input /> {/* Pleine largeur mobile, 50% tablette+ */}
  <input />
</div>
```

### Spacing Responsive

Utilisez les classes Tailwind pour adapter les espacements :

```jsx
<div className="p-4 md:p-6 lg:p-8">
  {/* Padding : 16px mobile, 24px tablette, 32px desktop */}
</div>

<div className="space-y-4 lg:space-y-6">
  {/* Gap vertical : 16px mobile, 24px desktop */}
</div>
```

### Classe Utilitaire : page-container

```jsx
<div className="page-container">
  {/* Padding automatique : 16px mobile, 24px tablette, 32px desktop */}
</div>
```

## 🎨 Patterns Courants

### 1. Navigation Stack sur Mobile

```jsx
<div className="flex flex-col lg:flex-row gap-4">
  <aside>Sidebar</aside>
  <main>Content</main>
</div>
```

### 2. Boutons Pleine Largeur sur Mobile

```jsx
<button className="w-full sm:w-auto btn-primary">
  Soumettre
</button>
```

### 3. Text Size Adaptatif

```jsx
<h1 className="text-2xl md:text-3xl lg:text-4xl">
  Titre Responsive
</h1>
```

### 4. Masquer des Éléments

```jsx
<div className="hidden md:block">
  Visible uniquement sur tablette et desktop
</div>

<div className="md:hidden">
  Visible uniquement sur mobile
</div>
```

### 5. Modal Plein Écran sur Mobile

```jsx
<div className="fixed inset-0 md:inset-auto md:top-1/2 md:left-1/2 md:transform md:-translate-x-1/2 md:-translate-y-1/2 md:max-w-lg md:rounded-lg">
  {/* Modal content */}
</div>
```

## 📋 Checklist Responsive

Avant de déployer une nouvelle page, vérifier :

### Desktop (≥1024px)
- [ ] Layout avec sidebar visible
- [ ] Tous les tableaux affichés correctement
- [ ] Formulaires bien alignés
- [ ] Graphiques (Recharts) affichés
- [ ] Modals centrés

### Tablette (768px - 1023px)
- [ ] Grilles adaptées (2 colonnes généralement)
- [ ] Sidebar en overlay
- [ ] Tableaux scrollables
- [ ] Formulaires stack ou 2 colonnes

### Mobile (< 768px)
- [ ] Menu hamburger fonctionnel
- [ ] Toutes les actions accessibles
- [ ] Textes lisibles (min 14px)
- [ ] Boutons assez grands (min 44x44px)
- [ ] Pas de scroll horizontal non voulu
- [ ] Tableaux scrollent horizontalement

## 🔧 Outils de Test

### 1. Chrome DevTools

```
F12 → Toggle Device Toolbar (Ctrl+Shift+M)
```

Tester sur :
- iPhone SE (375px)
- iPhone 12 Pro (390px)
- iPad Air (820px)
- Desktop (1920px)

### 2. Firefox Responsive Design Mode

```
Ctrl+Shift+M
```

### 3. Extension Real Device Preview

Chrome Extension pour tester sur de vrais appareils via USB.

## 🐛 Problèmes Courants

### Problème : Sidebar couvre le contenu sur mobile

**Cause** : z-index mal configuré

**Solution** :
```jsx
<aside className="fixed lg:static z-40">
<main className="flex-1 z-0">
```

### Problème : Texte trop petit sur mobile

**Cause** : Pas de classes responsive

**Solution** :
```jsx
// ❌ Mauvais
<p className="text-xs">

// ✅ Bon
<p className="text-sm md:text-base">
```

### Problème : Tableau déborde de l'écran

**Cause** : Pas de wrapper scrollable

**Solution** :
```jsx
<div className="overflow-x-auto">
  <table className="min-w-[640px]">
```

### Problème : Boutons trop petits pour toucher

**Cause** : Padding insuffisant

**Solution** :
```jsx
// ❌ Mauvais
<button className="px-2 py-1">

// ✅ Bon (min 44x44px pour accessibilité tactile)
<button className="px-4 py-2.5">
```

## 📐 Dimensions Recommandées

### Touch Targets (éléments cliquables)
- **Minimum** : 44x44px (recommandation WCAG)
- **Confortable** : 48x48px

### Text
- **Corps de texte** : 14-16px (sm ou base)
- **Titres H1** : 24-32px mobile, 32-48px desktop
- **Labels** : 12-14px

### Spacing
- **Gap entre cartes** : 16px (gap-4)
- **Padding de page** : 16px mobile, 32px desktop
- **Marges internes** : 16-24px

## 🎯 Classes Utilitaires Personnalisées

### .kpi-grid

Grid automatique qui adapte le nombre de colonnes :

```jsx
<div className="kpi-grid">
  <KpiCard />
  <KpiCard />
  <KpiCard />
</div>
```

### .page-container

Padding responsive automatique :

```jsx
<div className="page-container">
  {/* p-4 mobile, p-6 tablette, p-8 desktop */}
</div>
```

### .container-responsive

Container avec max-width et padding :

```jsx
<div className="container-responsive">
  {/* max-w-7xl mx-auto avec padding responsive */}
</div>
```

## 📱 Pages Déjà Optimisées

- ✅ **Layout** : Menu mobile avec hamburger
- ✅ **Login** : Centré et adaptatif
- ✅ **Dashboard** : Grilles KPI responsive
- ✅ **Reports** : Tables scrollables, filtres stackés
- ✅ **Objectives** : Formulaires et tables adaptés
- ✅ **AIChat** : Interface chat mobile-friendly

## 🚀 Bonnes Pratiques

### 1. Tester Fréquemment

Ouvrir DevTools et tester à chaque modification significative.

### 2. Penser Touch First

Les utilisateurs mobiles utilisent leurs doigts, pas une souris :
- Boutons plus grands
- Espacement généreux
- Pas de hover states critiques

### 3. Réduire la Complexité sur Mobile

Afficher seulement l'essentiel :
```jsx
<div>
  <span className="hidden md:inline">Détails complets</span>
  <span className="md:hidden">Résumé</span>
</div>
```

### 4. Performance

Les mobiles sont moins puissants :
- Charger moins de données
- Lazy load les images
- Minimiser les animations coûteuses

### 5. Images Responsive

```jsx
<img 
  src="image.jpg" 
  srcSet="image-small.jpg 640w, image-large.jpg 1280w"
  sizes="(max-width: 640px) 100vw, 640px"
  alt="Description"
/>
```

## 🎓 Ressources

- [Tailwind Responsive Design](https://tailwindcss.com/docs/responsive-design)
- [MDN Responsive Images](https://developer.mozilla.org/en-US/docs/Learn/HTML/Multimedia_and_embedding/Responsive_images)
- [WCAG Touch Target Size](https://www.w3.org/WAI/WCAG21/Understanding/target-size.html)
- [Google Mobile-Friendly Test](https://search.google.com/test/mobile-friendly)

## 📝 Exemples Complets

### Dashboard Responsive

```jsx
export default function Dashboard() {
  return (
    <Layout>
      <PageHeader title="Tableau de bord" />
      
      <div className="page-container space-y-6">
        {/* KPI Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard title="Production" value="15,000" />
          <KpiCard title="Qualité" value="98.5%" />
          <KpiCard title="Défauts" value="150" />
          <KpiCard title="Efficacité" value="95%" />
        </div>

        {/* Graphiques */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="panel p-6">
            <h3 className="text-lg font-semibold mb-4">Production</h3>
            {/* Chart */}
          </div>
          <div className="panel p-6">
            <h3 className="text-lg font-semibold mb-4">Qualité</h3>
            {/* Chart */}
          </div>
        </div>
      </div>
    </Layout>
  );
}
```

### Formulaire Responsive

```jsx
<form className="space-y-4">
  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
    <div>
      <label>Prénom</label>
      <input className="input-field" />
    </div>
    <div>
      <label>Nom</label>
      <input className="input-field" />
    </div>
  </div>

  <div>
    <label>Email</label>
    <input className="input-field" />
  </div>

  <div className="flex flex-col sm:flex-row gap-3 justify-end">
    <button type="button" className="btn-secondary order-2 sm:order-1">
      Annuler
    </button>
    <button type="submit" className="btn-primary order-1 sm:order-2">
      Enregistrer
    </button>
  </div>
</form>
```

---

**Prochaines étapes** : Tester sur de vrais appareils et ajuster selon les retours utilisateurs ! 📱✨
