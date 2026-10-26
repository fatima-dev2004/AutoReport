# Guide des Tests Frontend - AutoReport

Ce document explique comment exécuter et écrire des tests pour le frontend AutoReport.

## 🛠️ Configuration

Le projet utilise :
- **Vitest** : Framework de test rapide et moderne pour Vite
- **React Testing Library** : Pour tester les composants React
- **jsdom** : Environnement DOM pour les tests
- **@testing-library/user-event** : Simulation d'interactions utilisateur

## 📦 Installation des Dépendances

```bash
cd autoreport-frontend
npm install
```

Les dépendances de test sont déjà dans `package.json` :
- `vitest`
- `@vitest/ui`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `@testing-library/user-event`
- `jsdom`

## 🚀 Commandes de Test

### Lancer tous les tests
```bash
npm test
```

### Mode watch (relance automatique lors des changements)
```bash
npm test -- --watch
```

### Interface UI interactive
```bash
npm run test:ui
```
Ouvre une interface graphique dans le navigateur pour visualiser et déboguer les tests.

### Rapport de couverture
```bash
npm run test:coverage
```
Génère un rapport de couverture de code dans `coverage/`.

### Lancer un seul fichier de test
```bash
npm test -- src/components/__tests__/KpiCard.test.jsx
```

### Mode debug
```bash
npm test -- --inspect-brk
```

## 📁 Structure des Tests

```
src/
├── tests/
│   ├── setup.js           # Configuration globale des tests
│   └── test-utils.jsx     # Utilitaires et helpers
├── components/
│   ├── __tests__/
│   │   ├── KpiCard.test.jsx
│   │   └── PageHeader.test.jsx
├── api/
│   ├── __tests__/
│   │   ├── client.test.js
│   │   └── gemini.test.js
├── context/
│   └── __tests__/
│       └── AuthContext.test.jsx
└── pages/
    └── __tests__/
        └── Login.test.jsx
```

## ✍️ Écrire des Tests

### Test Simple d'un Composant

```javascript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import MyComponent from '../MyComponent';

describe('MyComponent', () => {
  it('affiche le texte correctement', () => {
    render(<MyComponent text="Hello World" />);
    
    expect(screen.getByText('Hello World')).toBeInTheDocument();
  });
});
```

### Test avec Interactions Utilisateur

```javascript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Button from '../Button';

describe('Button', () => {
  it('appelle onClick quand cliqué', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();
    
    render(<Button onClick={handleClick}>Click me</Button>);
    
    await user.click(screen.getByText('Click me'));
    
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});
```

### Test avec Providers (Router, Auth)

```javascript
import { renderWithProviders, mockUser } from '../../tests/test-utils';

describe('ProtectedComponent', () => {
  it('affiche le contenu pour un utilisateur connecté', () => {
    renderWithProviders(<ProtectedComponent />, { user: mockUser });
    
    expect(screen.getByText('Content')).toBeInTheDocument();
  });
});
```

### Test d'API avec Mock

```javascript
import { describe, it, expect, vi } from 'vitest';
import { listReports } from '../reports';
import axios from 'axios';

vi.mock('axios');

describe('reports API', () => {
  it('récupère la liste des rapports', async () => {
    const mockData = { content: [], totalElements: 0 };
    axios.get = vi.fn().mockResolvedValue({ data: mockData });
    
    const result = await listReports({ page: 0, size: 10 });
    
    expect(result).toEqual(mockData);
    expect(axios.get).toHaveBeenCalledWith(
      '/api/reports',
      expect.objectContaining({
        params: expect.objectContaining({ page: 0, size: 10 })
      })
    );
  });
});
```

### Test Asynchrone

```javascript
import { waitFor } from '@testing-library/react';

it('charge les données', async () => {
  render(<DataComponent />);
  
  // Attendre que l'élément apparaisse
  await waitFor(() => {
    expect(screen.getByText('Data loaded')).toBeInTheDocument();
  });
});
```

## 🎯 Bonnes Pratiques

### 1. Tester le Comportement, Pas l'Implémentation

❌ **Mauvais** : Tester les détails internes
```javascript
expect(component.state.count).toBe(1); // N'existe plus avec hooks
```

✅ **Bon** : Tester ce que l'utilisateur voit
```javascript
expect(screen.getByText('Count: 1')).toBeInTheDocument();
```

### 2. Utiliser les Queries dans l'Ordre de Priorité

1. `getByRole` : Meilleur pour l'accessibilité
2. `getByLabelText` : Pour les formulaires
3. `getByPlaceholderText` : Pour les inputs
4. `getByText` : Pour le contenu visible
5. `getByTestId` : En dernier recours uniquement

```javascript
// ✅ Bon
screen.getByRole('button', { name: /submit/i });

// ❌ À éviter
screen.getByTestId('submit-button');
```

### 3. Nettoyer les Mocks

```javascript
beforeEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
});
```

### 4. Tester les Cas d'Erreur

```javascript
it('affiche une erreur si la requête échoue', async () => {
  api.getData = vi.fn().mockRejectedValue(new Error('Network error'));
  
  render(<Component />);
  
  await waitFor(() => {
    expect(screen.getByText(/erreur/i)).toBeInTheDocument();
  });
});
```

### 5. Utiliser describe() pour Grouper

```javascript
describe('LoginForm', () => {
  describe('validation', () => {
    it('valide l\'email', () => { /* ... */ });
    it('valide le mot de passe', () => { /* ... */ });
  });

  describe('soumission', () => {
    it('appelle l\'API avec les bonnes données', () => { /* ... */ });
    it('redirige après succès', () => { /* ... */ });
  });
});
```

## 🐛 Débogage

### Afficher le DOM dans la Console

```javascript
import { screen } from '@testing-library/react';

// Afficher tout le DOM
screen.debug();

// Afficher un élément spécifique
screen.debug(screen.getByRole('button'));
```

### Rechercher des Éléments

```javascript
// Voir tous les rôles disponibles
screen.logTestingPlaygroundURL();

// Suggestions de queries
screen.getByRole(''); // Affiche les rôles disponibles dans l'erreur
```

### Mode Verbose

```bash
npm test -- --reporter=verbose
```

## 📊 Couverture de Code

### Objectifs de Couverture

- **Statements** : > 80%
- **Branches** : > 70%
- **Functions** : > 80%
- **Lines** : > 80%

### Zones Critiques à Tester en Priorité

1. ✅ **Authentification** : Login, logout, refresh token
2. ✅ **Formulaires** : Validation, soumission, erreurs
3. ✅ **API Calls** : Succès, erreurs, loading states
4. ✅ **Navigation** : Routes protégées, redirections
5. ✅ **Composants réutilisables** : KpiCard, Modal, Toast

### Vérifier la Couverture

```bash
npm run test:coverage
```

Ouvrir `coverage/index.html` dans un navigateur pour voir le rapport détaillé.

## 🔧 Configuration Vitest

Le fichier `vitest.config.js` configure :

```javascript
{
  globals: true,              // Variables globales (describe, it, expect)
  environment: 'jsdom',       // DOM simulé pour React
  setupFiles: './src/tests/setup.js',
  css: true,                  // Support CSS
  coverage: {
    provider: 'v8',
    reporter: ['text', 'json', 'html']
  }
}
```

## 📚 Tests Existants

### Composants
- ✅ `KpiCard` : Affichage des KPIs
- ✅ `PageHeader` : En-têtes de page

### API
- ✅ `client` : Intercepteurs axios, tokens
- ✅ `gemini` : API IA, configuration

### Contexte
- ✅ `AuthContext` : Login, logout, persistence

### Pages
- ✅ `Login` : Formulaire, validation, soumission

## 🎓 Ressources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
- [Testing Library Queries](https://testing-library.com/docs/queries/about)
- [Common Mistakes](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)

## 🚧 Tests à Ajouter (TODO)

- [ ] Tests des pages Reports, Objectives, Dashboard
- [ ] Tests des composants Modal, Toast, Loader
- [ ] Tests de l'API objectives, reports
- [ ] Tests d'intégration end-to-end
- [ ] Tests de performance avec Vitest bench

## 💡 Tips

### Tester un Hook Personnalisé

```javascript
import { renderHook, act } from '@testing-library/react';

it('teste useCounter', () => {
  const { result } = renderHook(() => useCounter());
  
  expect(result.current.count).toBe(0);
  
  act(() => {
    result.current.increment();
  });
  
  expect(result.current.count).toBe(1);
});
```

### Attendre un Changement

```javascript
// Attendre la disparition d'un loader
await waitFor(() => {
  expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
});
```

### Tester les Snapshot (à utiliser avec parcimonie)

```javascript
it('correspond au snapshot', () => {
  const { container } = render(<Component />);
  expect(container).toMatchSnapshot();
});
```

## 🎉 Lancer les Tests en CI/CD

Dans votre pipeline (GitHub Actions, GitLab CI, etc.) :

```yaml
- name: Run tests
  run: npm test -- --run --coverage

- name: Upload coverage
  uses: codecov/codecov-action@v3
```

## 📝 Convention de Nommage

- Fichiers de test : `*.test.jsx` ou `*.test.js`
- Dossier : `__tests__/` à côté du code testé
- Describe : Nom du composant/fonction
- It : Description du comportement attendu

```javascript
describe('LoginForm', () => {
  it('affiche un message d\'erreur si l\'email est invalide', () => {
    // Test
  });
});
```

Bon testing ! 🧪✨
