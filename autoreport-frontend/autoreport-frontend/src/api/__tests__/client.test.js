import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';

// Mock axios
vi.mock('axios');

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('ajoute le token Bearer dans les headers si présent dans localStorage', async () => {
    const mockToken = 'test-token-123';
    localStorage.setItem('autoreport_token', mockToken);

    const mockCreate = vi.fn(() => ({
      interceptors: {
        request: {
          use: vi.fn((callback) => {
            const config = { headers: {} };
            const result = callback(config);
            expect(result.headers.Authorization).toBe(`Bearer ${mockToken}`);
          }),
        },
        response: {
          use: vi.fn(),
        },
      },
    }));

    axios.create = mockCreate;

    // Importer le client après avoir configuré le mock
    await import('../client.js');

    expect(mockCreate).toHaveBeenCalled();
  });

  it('configure la baseURL correctement', () => {
    const mockCreate = vi.fn(() => ({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    }));

    axios.create = mockCreate;

    // L'URL par défaut devrait être http://localhost:8080
    expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({
        baseURL: expect.any(String),
        withCredentials: true,
      })
    );
  });

  it('gère les erreurs 401 correctement', () => {
    const mockInterceptor = {
      request: { use: vi.fn() },
      response: {
        use: vi.fn((successCallback, errorCallback) => {
          // Simuler une erreur 401
          const error = {
            response: { status: 401 },
          };

          localStorage.setItem('autoreport_token', 'test-token');
          localStorage.setItem('autoreport_user', JSON.stringify({ email: 'test@test.com' }));

          try {
            errorCallback(error);
          } catch (e) {
            // L'erreur devrait être rejetée
          }

          // Vérifier que localStorage a été nettoyé
          expect(localStorage.getItem('autoreport_token')).toBeNull();
          expect(localStorage.getItem('autoreport_user')).toBeNull();
        }),
      },
    };

    axios.create = vi.fn(() => ({ interceptors: mockInterceptor }));

    // Charger le module
    require('../client.js');
  });
});
