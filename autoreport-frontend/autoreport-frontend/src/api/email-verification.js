import apiClient from './client';

/**
 * API pour la vérification d'email avant envoi de rapports
 */

/**
 * Demande l'envoi d'un code de vérification par email
 * @param {string} email - Email à vérifier
 * @param {number} reportId - ID du rapport à envoyer
 * @returns {Promise<{verificationId: string, expiresAt: string}>}
 */
export const sendVerificationCode = async (email, reportId) => {
  const response = await apiClient.post('/api/email/send-verification', {
    email,
    reportId,
  });
  return response.data;
};

/**
 * Vérifie le code et envoie le rapport par email
 * @param {string} verificationId - ID de la vérification
 * @param {string} code - Code de vérification (6 chiffres)
 * @returns {Promise<{success: boolean, message: string}>}
 */
export const verifyAndSendReport = async (verificationId, code) => {
  const response = await apiClient.post('/api/email/verify-and-send', {
    verificationId,
    code,
  });
  return response.data;
};

/**
 * Renvoie un nouveau code de vérification
 * @param {string} verificationId - ID de la vérification
 * @returns {Promise<{verificationId: string, expiresAt: string}>}
 */
export const resendVerificationCode = async (verificationId) => {
  const response = await apiClient.post(`/api/email/resend-verification/${verificationId}`);
  return response.data;
};

/**
 * Vérifie le statut d'une vérification
 * @param {string} verificationId - ID de la vérification
 * @returns {Promise<{status: string, attemptsLeft: number}>}
 */
export const checkVerificationStatus = async (verificationId) => {
  const response = await apiClient.get(`/api/email/verification-status/${verificationId}`);
  return response.data;
};
