/**
 * VERSION MOCK pour tester sans backend
 * Renommez ce fichier en email-verification.js pour l'utiliser
 */

// Simuler un délai réseau
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Stocker les codes en mémoire (pour simulation)
const mockVerifications = new Map();

/**
 * MOCK : Demande l'envoi d'un code de vérification
 */
export const sendVerificationCode = async (email, reportId) => {
  await delay(800);
  
  // Générer un code aléatoire
  const code = Math.floor(100000 + Math.random() * 900000).toString();
  const verificationId = `mock-${Date.now()}`;
  const expiresAt = new Date(Date.now() + 5 * 60 * 1000).toISOString();
  
  // Stocker en mémoire
  mockVerifications.set(verificationId, {
    email,
    reportId,
    code,
    expiresAt,
    attempts: 0,
  });
  
  // Afficher le code dans la console pour le test
  console.log('🔐 CODE DE VÉRIFICATION MOCK:', code);
  console.log('📧 Email:', email);
  console.log('📄 Report ID:', reportId);
  console.log('⏱️ Expire à:', expiresAt);
  
  return {
    verificationId,
    expiresAt,
  };
};

/**
 * MOCK : Vérifie le code et "envoie" le rapport
 */
export const verifyAndSendReport = async (verificationId, code) => {
  await delay(500);
  
  const verification = mockVerifications.get(verificationId);
  
  if (!verification) {
    throw {
      response: {
        data: { error: 'Vérification introuvable' }
      }
    };
  }
  
  // Vérifier expiration
  if (new Date() > new Date(verification.expiresAt)) {
    throw {
      response: {
        data: { error: 'Le code a expiré' }
      }
    };
  }
  
  // Incrémenter les tentatives
  verification.attempts++;
  
  if (verification.attempts > 3) {
    throw {
      response: {
        data: { error: 'Trop de tentatives. Veuillez redemander un code.' }
      }
    };
  }
  
  // Vérifier le code
  if (code !== verification.code) {
    throw {
      response: {
        data: { 
          error: `Code incorrect. Il vous reste ${4 - verification.attempts} tentative(s)` 
        }
      }
    };
  }
  
  // Succès !
  console.log('✅ Rapport "envoyé" à:', verification.email);
  mockVerifications.delete(verificationId);
  
  return {
    success: true,
    message: 'Rapport envoyé avec succès',
  };
};

/**
 * MOCK : Renvoie un nouveau code
 */
export const resendVerificationCode = async (verificationId) => {
  await delay(800);
  
  const oldVerification = mockVerifications.get(verificationId);
  
  if (!oldVerification) {
    throw {
      response: {
        data: { error: 'Vérification introuvable' }
      }
    };
  }
  
  // Créer une nouvelle vérification
  const newCode = Math.floor(100000 + Math.random() * 900000).toString();
  const newVerificationId = `mock-${Date.now()}`;
  const expiresAt = new Date(Date.now() + 5 * 60 * 1000).toISOString();
  
  mockVerifications.delete(verificationId);
  mockVerifications.set(newVerificationId, {
    email: oldVerification.email,
    reportId: oldVerification.reportId,
    code: newCode,
    expiresAt,
    attempts: 0,
  });
  
  console.log('🔐 NOUVEAU CODE MOCK:', newCode);
  console.log('⏱️ Expire à:', expiresAt);
  
  return {
    verificationId: newVerificationId,
    expiresAt,
  };
};

/**
 * MOCK : Vérifie le statut
 */
export const checkVerificationStatus = async (verificationId) => {
  await delay(200);
  
  const verification = mockVerifications.get(verificationId);
  
  if (!verification) {
    throw {
      response: {
        data: { error: 'Vérification introuvable' }
      }
    };
  }
  
  return {
    status: 'PENDING',
    attemptsLeft: 3 - verification.attempts,
  };
};
