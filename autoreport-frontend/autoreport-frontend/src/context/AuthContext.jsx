import React, { createContext, useContext, useEffect, useState } from 'react';
import { login as loginRequest, logout as logoutRequest } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    // Au chargement, vérifier si l'utilisateur est stocké (pour persistance UI)
    // Le vrai token sera dans les cookies httpOnly
    const storedUser = localStorage.getItem('autoreport_user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('autoreport_user');
      }
    }
    setReady(true);
  }, []);

  const login = async (email, password) => {
    const data = await loginRequest(email, password);
    const nextUser = {
      email: data.email,
      role: data.role,
      fullName: data.fullName,
      phone: data.phone || '',
      photoUrl: data.photoUrl || '',
    };

    // On garde le token en localStorage pour compatibilité (mais le vrai sera dans le cookie)
    if (data.token) {
      localStorage.setItem('autoreport_token', data.token);
    }

    // Stocker les infos utilisateur pour l'UI
    localStorage.setItem('autoreport_user', JSON.stringify(nextUser));
    setUser(nextUser);
    return nextUser;
  };

  const logout = async () => {
    try {
      // Appeler l'endpoint de logout pour invalider le refresh token côté serveur
      await logoutRequest();
    } catch (error) {
      console.error('Erreur lors du logout:', error);
    } finally {
      // Nettoyer le localStorage dans tous les cas
      localStorage.removeItem('autoreport_token');
      localStorage.removeItem('autoreport_user');
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, ready, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
