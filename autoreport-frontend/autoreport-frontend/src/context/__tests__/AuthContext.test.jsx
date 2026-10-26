import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../AuthContext';
import * as authApi from '../../api/auth';

vi.mock('../../api/auth');

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('initialise avec user null et ready false', () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('charge l\'utilisateur depuis localStorage au montage', async () => {
    const mockUser = {
      email: 'test@test.com',
      fullName: 'Test User',
      role: 'ADMIN',
    };

    localStorage.setItem('autoreport_user', JSON.stringify(mockUser));

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    await waitFor(() => {
      expect(result.current.ready).toBe(true);
    });

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('login met à jour l\'utilisateur et le localStorage', async () => {
    const mockResponse = {
      token: 'test-token-123',
      email: 'user@test.com',
      fullName: 'New User',
      role: 'MANAGER',
      phone: '+212 600 000 000',
      photoUrl: 'data:image/png;base64,abc123',
    };

    authApi.login = vi.fn().mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    await act(async () => {
      await result.current.login('user@test.com', 'password123');
    });

    expect(result.current.user).toEqual({
      email: 'user@test.com',
      fullName: 'New User',
      role: 'MANAGER',
      phone: '+212 600 000 000',
      photoUrl: 'data:image/png;base64,abc123',
    });

    expect(localStorage.getItem('autoreport_token')).toBe('test-token-123');
    expect(JSON.parse(localStorage.getItem('autoreport_user'))).toEqual({
      email: 'user@test.com',
      fullName: 'New User',
      role: 'MANAGER',
      phone: '+212 600 000 000',
      photoUrl: 'data:image/png;base64,abc123',
    });
  });

  it('logout supprime l\'utilisateur et nettoie le localStorage', async () => {
    localStorage.setItem('autoreport_token', 'test-token');
    localStorage.setItem('autoreport_user', JSON.stringify({ email: 'test@test.com' }));

    authApi.logout = vi.fn().mockResolvedValue({});

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(localStorage.getItem('autoreport_token')).toBeNull();
    expect(localStorage.getItem('autoreport_user')).toBeNull();
  });

  it('logout nettoie même si l\'appel API échoue', async () => {
    localStorage.setItem('autoreport_token', 'test-token');
    localStorage.setItem('autoreport_user', JSON.stringify({ email: 'test@test.com' }));

    authApi.logout = vi.fn().mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    await act(async () => {
      await result.current.logout();
    });

    // Même en cas d'erreur, le localStorage doit être nettoyé
    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('autoreport_token')).toBeNull();
    expect(localStorage.getItem('autoreport_user')).toBeNull();
  });

  it('gère les données localStorage corrompues', async () => {
    localStorage.setItem('autoreport_user', 'invalid-json');

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    await waitFor(() => {
      expect(result.current.ready).toBe(true);
    });

    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('autoreport_user')).toBeNull();
  });
});
