import apiClient from './client';

// POST /api/auth/login -> { token, email, role, fullName }
export const login = (email, password) =>
  apiClient.post('/api/auth/login', { email, password }).then((res) => res.data);

// POST /api/auth/refresh -> renouvelle le token via cookie httpOnly
export const refreshToken = () =>
  apiClient.post('/api/auth/refresh').then((res) => res.data);

// POST /api/auth/logout -> invalide le refresh token
export const logout = () =>
  apiClient.post('/api/auth/logout').then((res) => res.data);
