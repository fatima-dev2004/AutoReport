import React, { useState, useEffect } from 'react';
import { X, Mail, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import Modal from './Modal';
import { useToast } from './Toast';
import { sendReportByEmail } from '../api/reports';

/**
 * Modal d'envoi de rapport par email (version simplifiée)
 */
export default function EmailVerificationModal({ isOpen, onClose, reportId, reportTitle }) {
  const toast = useToast();
  
  const [email, setEmail] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  // Réinitialiser lors de l'ouverture
  useEffect(() => {
    if (isOpen) {
      setEmail('');
      setRecipientName('');
      setSuccess(false);
    }
  }, [isOpen]);

  // Envoyer le rapport par email
  const handleSendEmail = async (e) => {
    e.preventDefault();
    
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      toast.error('Veuillez entrer une adresse email valide');
      return;
    }

    setLoading(true);
    try {
      await sendReportByEmail(reportId, email, recipientName || 'Destinataire');
      setSuccess(true);
      toast.success(`Rapport envoyé avec succès à ${email}`);
    } catch (error) {
      console.error('Erreur envoi email:', error);
      toast.error(error.response?.data?.error || 'Échec de l\'envoi du rapport par email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Envoyer le rapport par email">
      <div className="space-y-4">
        {!success ? (
          <>
            <div className="flex items-start gap-3 p-4 bg-surface-2 rounded-lg border border-border">
              <AlertCircle size={20} className="text-amber shrink-0 mt-0.5" />
              <div className="text-sm text-text-muted">
                <p className="font-medium text-text mb-1">Envoi de rapport</p>
                <p>
                  Le rapport <span className="font-medium text-text">{reportTitle}</span> sera envoyé 
                  par email avec les fichiers PDF et Excel en pièces jointes.
                </p>
              </div>
            </div>

            <form onSubmit={handleSendEmail} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-text mb-2">
                  Adresse email du destinataire *
                </label>
                <div className="relative">
                  <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-faint" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="destinataire@example.com"
                    className="input-field pl-10 w-full"
                    autoFocus
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-2">
                  Nom du destinataire (optionnel)
                </label>
                <input
                  type="text"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  placeholder="Jean Dupont"
                  className="input-field w-full"
                />
              </div>

              <div className="flex gap-2 justify-end pt-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="btn-secondary"
                  disabled={loading}
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <Loader2 size={16} className="animate-spin" />
                      Envoi en cours...
                    </>
                  ) : (
                    <>
                      <Mail size={16} />
                      Envoyer le rapport
                    </>
                  )}
                </button>
              </div>
            </form>
          </>
        ) : (
          <>
            <div className="text-center space-y-4 py-6">
              <div className="w-20 h-20 bg-teal/10 rounded-full flex items-center justify-center mx-auto">
                <CheckCircle2 size={40} className="text-teal" />
              </div>
              <div className="space-y-2">
                <h3 className="font-medium text-text text-lg">Rapport envoyé avec succès !</h3>
                <p className="text-sm text-text-muted">
                  Le rapport <span className="font-medium text-text">{reportTitle}</span> a été envoyé à{' '}
                  <span className="font-medium text-text">{email}</span>
                </p>
              </div>
            </div>

            <div className="flex justify-center pt-2">
              <button onClick={onClose} className="btn-primary">
                Fermer
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
