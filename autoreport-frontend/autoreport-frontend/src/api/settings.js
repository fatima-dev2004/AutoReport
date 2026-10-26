import apiClient from './client';

// Toutes les routes vivent sous /api/admin/settings (réservé ROLE_ADMIN côté backend)
const BASE = '/api/admin/settings';

// Configuration SMTP
export const getSmtpConfig = () => apiClient.get(`${BASE}/smtp`);
export const saveSmtpConfig = (config) => apiClient.put(`${BASE}/smtp`, config);
export const testSmtpConfig = () => apiClient.post(`${BASE}/smtp/test`);

// Configuration Planification
export const getScheduleConfig = () => apiClient.get(`${BASE}/schedule`);
export const saveScheduleConfig = (config) => apiClient.put(`${BASE}/schedule`, config);

// Configuration Entreprise
export const getCompanyConfig = () => apiClient.get(`${BASE}/company`);
export const saveCompanyConfig = (configOrFormData) => {
  const isFormData = configOrFormData instanceof FormData;
  return apiClient.put(`${BASE}/company`, configOrFormData, {
    headers: isFormData ? { 'Content-Type': 'multipart/form-data' } : undefined,
  });
};

// Destinataires Email
export const getEmailRecipients = () => apiClient.get(`${BASE}/email-recipients`);
export const addEmailRecipient = (recipient) => apiClient.post(`${BASE}/email-recipients`, recipient);
export const removeEmailRecipient = (id) => apiClient.delete(`${BASE}/email-recipients/${id}`);

// Toutes les configurations en une fois : le backend n'expose pas d'endpoint agrégé,
// on compose donc les 4 appels ici pour garder la page Settings.jsx inchangée.
export const getAllSettings = async () => {
  const [smtp, schedule, company, emailRecipients] = await Promise.all([
    getSmtpConfig().then((r) => r.data).catch(() => ({})),
    getScheduleConfig().then((r) => r.data).catch(() => ({})),
    getCompanyConfig().then((r) => r.data).catch(() => ({})),
    getEmailRecipients().then((r) => r.data).catch(() => []),
  ]);
  return { data: { smtp, schedule, company, emailRecipients } };
};
