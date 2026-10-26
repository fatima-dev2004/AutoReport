import apiClient from './client';

/**
 * Liste les objectifs de production avec pagination et filtres
 * @param {Object} params - Paramètres de requête
 * @param {number} params.page - Numéro de page (0-indexed)
 * @param {number} params.size - Taille de la page
 * @param {string} params.line - Filtrer par ligne
 * @param {string} params.period - Filtrer par période (DAILY, WEEKLY, MONTHLY, YEARLY)
 * @param {boolean} params.active - Filtrer par statut actif
 * @returns {Promise} - Page d'objectifs
 */
export const listObjectives = (params = {}) => {
  const queryParams = {
    page: params.page || 0,
    size: params.size || 10,
    ...(params.line && { line: params.line }),
    ...(params.period && params.period !== 'all' && { period: params.period }),
    ...(params.active !== undefined && { active: params.active }),
  };

  return apiClient.get('/api/objectives', { params: queryParams }).then((res) => res.data);
};

/**
 * Récupère un objectif par ID
 * @param {number} id - ID de l'objectif
 * @returns {Promise} - Objectif
 */
export const getObjective = (id) =>
  apiClient.get(`/api/objectives/${id}`).then((res) => res.data);

/**
 * Crée un nouvel objectif
 * @param {Object} data - Données de l'objectif
 * @param {string} data.line - Ligne de production
 * @param {string} data.period - Période (DAILY, WEEKLY, MONTHLY, YEARLY)
 * @param {number} data.targetQuantity - Quantité cible
 * @param {number} data.targetQualityRate - Taux de qualité cible (%)
 * @param {number} data.maxDefectRate - Taux de défaut maximum (%)
 * @param {string} data.startDate - Date de début (ISO)
 * @param {string} data.endDate - Date de fin (ISO, optionnel)
 * @param {boolean} data.active - Actif ou non
 * @returns {Promise} - Objectif créé
 */
export const createObjective = (data) =>
  apiClient.post('/api/objectives', data).then((res) => res.data);

/**
 * Met à jour un objectif
 * @param {number} id - ID de l'objectif
 * @param {Object} data - Données à mettre à jour
 * @returns {Promise} - Objectif mis à jour
 */
export const updateObjective = (id, data) =>
  apiClient.put(`/api/objectives/${id}`, data).then((res) => res.data);

/**
 * Supprime un objectif
 * @param {number} id - ID de l'objectif
 * @returns {Promise}
 */
export const deleteObjective = (id) =>
  apiClient.delete(`/api/objectives/${id}`).then((res) => res.data);

/**
 * Active/désactive un objectif
 * @param {number} id - ID de l'objectif
 * @param {boolean} active - Nouveau statut
 * @returns {Promise}
 */
export const toggleObjectiveStatus = (id, active) =>
  apiClient.patch(`/api/objectives/${id}/status`, { active }).then((res) => res.data);

/**
 * Récupère les lignes de production disponibles
 * @returns {Promise} - Liste des lignes
 */
export const getProductionLines = async () => {
  try {
    const response = await apiClient.get('/api/objectives/lines');
    const lines = Array.isArray(response.data) ? response.data.filter(Boolean) : [];
    return lines.length > 0 ? lines : ['Ligne A', 'Ligne B', 'Ligne C', 'Ligne D'];
  } catch (error) {
    return ['Ligne A', 'Ligne B', 'Ligne C', 'Ligne D'];
  }
};

/**
 * Récupère les statistiques de réalisation d'un objectif
 * @param {number} id - ID de l'objectif
 * @returns {Promise} - Statistiques { achieved, target, rate, status }
 */
export const getObjectiveStats = (id) =>
  apiClient.get(`/api/objectives/${id}/stats`).then((res) => res.data);

/**
 * Compare les réalisations aux objectifs pour une période
 * @param {string} startDate - Date de début (ISO)
 * @param {string} endDate - Date de fin (ISO)
 * @param {string} line - Ligne de production (optionnel)
 * @returns {Promise} - Rapport de comparaison
 */
export const compareObjectivesVsReality = (startDate, endDate, line = null) => {
  const params = { startDate, endDate };
  if (line) params.line = line;
  return apiClient.get('/api/objectives/compare', { params }).then((res) => res.data);
};
