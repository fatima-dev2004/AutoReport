import axios from 'axios';

// L'URL du backend Spring Boot. Modifiable via .env (VITE_API_BASE_URL).
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({ 
  baseURL,
  withCredentials: true, // Important pour envoyer les cookies httpOnly
});

// Flag pour éviter les boucles infinies lors du refresh
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Intercepteur de requêtes : ajouter le token depuis localStorage (fallback)
// Note: Le token principal viendra des cookies httpOnly, mais on garde localStorage pour compatibilité
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('autoreport_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercepteur de réponses : gérer le refresh automatique du token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Si 401 et pas déjà en train de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Si un refresh est déjà en cours, mettre cette requête en file d'attente
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => apiClient(originalRequest))
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Tenter de refresh le token
        await apiClient.post('/api/auth/refresh');
        
        // Le nouveau token est maintenant dans les cookies
        isRefreshing = false;
        processQueue(null);
        
        // Réessayer la requête originale
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Échec du refresh : déconnecter l'utilisateur
        processQueue(refreshError, null);
        isRefreshing = false;
        
        localStorage.removeItem('autoreport_token');
        localStorage.removeItem('autoreport_user');
        
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login';
        }
        
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
