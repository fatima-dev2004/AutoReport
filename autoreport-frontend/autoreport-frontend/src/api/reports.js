import apiClient from './client';

/**
 * Liste les rapports avec pagination et filtres côté serveur
 * @param {Object} params - Paramètres de requête
 * @param {number} params.page - Numéro de page (commence à 0)
 * @param {number} params.size - Taille de la page
 * @param {string} params.search - Recherche textuelle
 * @param {string} params.userEmail - Filtrer par email utilisateur
 * @param {string} params.type - Filtrer par type de rapport
 * @param {string} params.startDate - Date de début (ISO)
 * @param {string} params.endDate - Date de fin (ISO)
 * @param {string} params.sortBy - Champ de tri (ex: 'generatedAt', 'title')
 * @param {string} params.sortDir - Direction du tri ('asc' ou 'desc')
 * @returns {Promise} - { content: [], totalElements, totalPages, currentPage, size }
 */
export const listReports = (params = {}) => {
  const queryParams = {
    page: params.page || 0,
    size: params.size || 10,
    ...(params.search && { search: params.search }),
    ...(params.userEmail && params.userEmail !== 'all' && { userEmail: params.userEmail }),
    ...(params.type && params.type !== 'all' && { type: params.type }),
    ...(params.startDate && { startDate: params.startDate }),
    ...(params.endDate && { endDate: params.endDate }),
    ...(params.sortBy && { sortBy: params.sortBy }),
    ...(params.sortDir && { sortDir: params.sortDir }),
  };

  return apiClient.get('/api/reports', { params: queryParams }).then((res) => res.data);
};

/**
 * Récupère les filtres disponibles (utilisateurs uniques, types uniques)
 * @returns {Promise} - { users: [], types: [] }
 */
export const getReportFilters = () =>
  apiClient.get('/api/reports/filters').then((res) => res.data);

// POST /api/reports/import (multipart)
export const importExcel = (file, onUploadProgress) => {
  const formData = new FormData();
  formData.append('file', file);
  return apiClient
    .post('/api/reports/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    })
    .then((res) => res.data);
};

// POST /api/reports/generate?start=&end= (rapport simple, sans logo/page de garde)
export const generateReport = (start, end) =>
  apiClient.post('/api/reports/generate', null, { params: { start, end } }).then((res) => res.data);

// POST /api/reports/generate/enhanced (rapport avec design : logo, page de garde, couleurs)
export const generateEnhancedReport = (start, end, options = {}) =>
  apiClient
    .post('/api/reports/generate/enhanced', {
      startDate: start,
      endDate: end,
      includeCoverPage: true,
      includeCharts: true,
      includeDetailedData: true,
      ...options,
    })
    .then((res) => res.data);

// GET /api/reports/{id}/download/pdf | excel — triggers a browser download
export const downloadReport = async (id, format, filenameHint) => {
  const path = format === 'pdf' ? `/api/reports/${id}/download/pdf` : `/api/reports/${id}/download/excel`;
  const res = await apiClient.get(path, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filenameHint || `rapport.${format === 'pdf' ? 'pdf' : 'xlsx'}`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

/**
 * Envoie un rapport par email
 * @param {number} id - ID du rapport
 * @param {string} email - Email du destinataire
 * @param {string} recipientName - Nom du destinataire (optionnel)
 * @returns {Promise} - Réponse de l'envoi
 */
export const sendReportByEmail = (id, email, recipientName = '') => {
  return apiClient.post(`/api/reports/${id}/send-to`, null, {
    params: {
      email,
      recipientName,
    },
  }).then((res) => res.data);
};
