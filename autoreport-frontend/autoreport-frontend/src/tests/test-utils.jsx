import React from 'react';
import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';

// Mock user pour les tests
export const mockUser = {
  email: 'test@autoreport.com',
  fullName: 'Test User',
  role: 'ADMIN',
};

export const mockManagerUser = {
  email: 'manager@autoreport.com',
  fullName: 'Manager User',
  role: 'MANAGER',
};

export const mockRegularUser = {
  email: 'user@autoreport.com',
  fullName: 'Regular User',
  role: 'USER',
};

// Wrapper personnalisé avec tous les providers nécessaires
function AllProviders({ children, user = mockUser }) {
  // Mock du contexte Auth
  const mockAuthContext = {
    user,
    ready: true,
    login: vi.fn(),
    logout: vi.fn(),
    isAuthenticated: !!user,
  };

  return (
    <BrowserRouter>
      <AuthProvider value={mockAuthContext}>
        {children}
      </AuthProvider>
    </BrowserRouter>
  );
}

// Fonction de render personnalisée
export function renderWithProviders(ui, { user = mockUser, ...options } = {}) {
  return render(ui, {
    wrapper: (props) => <AllProviders {...props} user={user} />,
    ...options,
  });
}

// Mock pour axios
export const mockAxios = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
  delete: vi.fn(),
  create: vi.fn(() => mockAxios),
  interceptors: {
    request: {
      use: vi.fn(),
      eject: vi.fn(),
    },
    response: {
      use: vi.fn(),
      eject: vi.fn(),
    },
  },
};

// Helpers pour les réponses API
export const createMockResponse = (data, status = 200) => ({
  data,
  status,
  statusText: 'OK',
  headers: {},
  config: {},
});

export const createMockError = (message, status = 400) => ({
  response: {
    data: { error: message },
    status,
    statusText: 'Bad Request',
  },
  message,
});

// Mock pour les rapports
export const mockReports = [
  {
    id: 1,
    title: 'Rapport Production - Semaine 01',
    type: 'WEEKLY',
    periodStart: '2024-01-01',
    periodEnd: '2024-01-07',
    totalProduction: 15000,
    qualityRate: 98.5,
    generatedByEmail: 'admin@autoreport.com',
    generatedAt: '2024-01-08T10:30:00',
  },
  {
    id: 2,
    title: 'Rapport Production - Semaine 02',
    type: 'WEEKLY',
    periodStart: '2024-01-08',
    periodEnd: '2024-01-14',
    totalProduction: 16000,
    qualityRate: 97.2,
    generatedByEmail: 'admin@autoreport.com',
    generatedAt: '2024-01-15T10:30:00',
  },
];

// Mock pour les objectifs
export const mockObjectives = [
  {
    id: 1,
    line: 'Line A',
    period: 'MONTHLY',
    targetQuantity: 50000,
    targetQualityRate: 95.0,
    maxDefectRate: 5.0,
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    active: true,
  },
  {
    id: 2,
    line: 'Line B',
    period: 'WEEKLY',
    targetQuantity: 12000,
    targetQualityRate: 96.0,
    maxDefectRate: 4.0,
    startDate: '2024-01-01',
    endDate: null,
    active: true,
  },
];

// Mock pour les utilisateurs
export const mockUsers = [
  {
    id: 1,
    email: 'admin@autoreport.com',
    fullName: 'Admin User',
    role: 'ADMIN',
    active: true,
  },
  {
    id: 2,
    email: 'manager@autoreport.com',
    fullName: 'Manager User',
    role: 'MANAGER',
    active: true,
  },
];

// Export tout
export * from '@testing-library/react';
export { default as userEvent } from '@testing-library/user-event';
