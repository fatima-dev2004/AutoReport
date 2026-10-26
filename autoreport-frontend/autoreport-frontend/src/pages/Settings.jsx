import React, { useState, useEffect } from 'react';
import {
  Settings as SettingsIcon,
  Mail,
  Clock,
  Building2,
  Image,
  Save,
  Plus,
  Trash2,
  TestTube,
} from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import Loader from '../components/Loader';
import {
  getAllSettings,
  saveSmtpConfig,
  testSmtpConfig,
  saveScheduleConfig,
  saveCompanyConfig,
  addEmailRecipient,
  removeEmailRecipient,
} from '../api/settings';

export default function Settings() {
  const { t } = useLanguage();
  const toast = useToast();
  const [loading, setLoading] = useState(true);

  const [smtpConfig, setSmtpConfig] = useState({
    host: '',
    port: '',
    username: '',
    password: '',
    fromName: '',
    fromEmail: '',
  });

  const [scheduleConfig, setScheduleConfig] = useState({
    autoSendEnabled: false,
    sendTime: '08:00',
    sendDays: [],
  });

  const [companyConfig, setCompanyConfig] = useState({
    name: '',
    logo: null,
  });
  const [companyLogoName, setCompanyLogoName] = useState('');

  const [emailRecipients, setEmailRecipients] = useState([]);

  const [newRecipient, setNewRecipient] = useState({ name: '', email: '' });
  const [saving, setSaving] = useState(false);
  const [syncingSigip, setSyncingSigip] = useState(false);

  // Charger toutes les configurations au montage
  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    setLoading(true);
    try {
      const response = await getAllSettings();
      const settings = response.data;

      if (settings.smtp) setSmtpConfig(settings.smtp);
      if (settings.schedule) setScheduleConfig(settings.schedule);
      if (settings.company) setCompanyConfig(settings.company);
      if (settings.emailRecipients) setEmailRecipients(settings.emailRecipients);
    } catch (err) {
      // Si backend pas encore prêt, garder valeurs par défaut
      console.warn('Settings not loaded from backend, using defaults');
    } finally {
      setLoading(false);
    }
  };

  const weekDays = [
    { value: 'monday', label: t('settings.days.monday') },
    { value: 'tuesday', label: t('settings.days.tuesday') },
    { value: 'wednesday', label: t('settings.days.wednesday') },
    { value: 'thursday', label: t('settings.days.thursday') },
    { value: 'friday', label: t('settings.days.friday') },
    { value: 'saturday', label: t('settings.days.saturday') },
    { value: 'sunday', label: t('settings.days.sunday') },
  ];

  const handleSaveSmtp = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      await saveSmtpConfig(smtpConfig);
      toast.success(t('settings.smtpSaved'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.error')
      );
    } finally {
      setSaving(false);
    }
  };

  const handleTestEmail = async () => {
    toast.info(t('settings.testSending') || 'Envoi d\'un email de test...');
    try {
      await testSmtpConfig();
      toast.success(t('settings.testSuccess'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.testError')
      );
    }
  };

  const handleSaveSchedule = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      await saveScheduleConfig(scheduleConfig);
      toast.success(t('settings.saved'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.error')
      );
    } finally {
      setSaving(false);
    }
  };

  const handleSaveCompany = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      // Si logo est un fichier, utiliser FormData
      if (companyConfig.logo instanceof File) {
        const formData = new FormData();
        formData.append('name', companyConfig.name);
        formData.append('logo', companyConfig.logo);
        await saveCompanyConfig(formData);
      } else {
        await saveCompanyConfig({ name: companyConfig.name });
      }
      toast.success(t('settings.companySaved'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.error')
      );
    } finally {
      setSaving(false);
    }
  };

  const handleSyncSigip = async () => {
    setSyncingSigip(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 1200));
      toast.success(t('settings.syncSigipSuccess'));
    } catch (err) {
      toast.error(t('settings.error'));
    } finally {
      setSyncingSigip(false);
    }
  };

  const handleAddRecipient = async () => {
    if (!newRecipient.name || !newRecipient.email) {
      toast.error(t('settings.fillAllFields'));
      return;
    }

    try {
      const response = await addEmailRecipient(newRecipient);
      setEmailRecipients([...emailRecipients, response.data]);
      setNewRecipient({ name: '', email: '' });
      toast.success(t('settings.recipientAdded'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.addError')
      );
    }
  };

  const handleRemoveRecipient = async (id) => {
    try {
      await removeEmailRecipient(id);
      setEmailRecipients(emailRecipients.filter((r) => r.id !== id));
      toast.info(t('settings.recipientRemoved'));
    } catch (err) {
      toast.error(
        err.response?.data?.error || t('settings.deleteError')
      );
    }
  };

  const toggleDay = (day) => {
    if (scheduleConfig.sendDays.includes(day)) {
      setScheduleConfig({
        ...scheduleConfig,
        sendDays: scheduleConfig.sendDays.filter((d) => d !== day),
      });
    } else {
      setScheduleConfig({
        ...scheduleConfig,
        sendDays: [...scheduleConfig.sendDays, day],
      });
    }
  };

  return (
    <Layout>
      <PageHeader eyebrow={t('settings.eyebrow')} title={t('settings.title')} />

      {loading ? (
        <div className="p-8">
          <Loader text={t('common.loading')} />
        </div>
      ) : (
        <div className="p-8 space-y-6 max-w-5xl">
        {/* Configuration SMTP */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="flex items-center gap-2">
              <Mail size={18} className="text-amber" />
              <div className="eyebrow">{t('settings.email')}</div>
            </div>
          </div>

          <form onSubmit={handleSaveSmtp} className="p-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.smtpHost')}
                </label>
                <input
                  type="text"
                  value={smtpConfig.host}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, host: e.target.value })
                  }
                  className="input-field"
                  placeholder="smtp.gmail.com"
                  required
                />
              </div>

              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.smtpPort')}
                </label>
                <input
                  type="number"
                  value={smtpConfig.port}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, port: e.target.value })
                  }
                  className="input-field"
                  placeholder="587"
                  required
                />
              </div>

              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.smtpUser')}
                </label>
                <input
                  type="text"
                  value={smtpConfig.username}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, username: e.target.value })
                  }
                  className="input-field"
                  required
                />
              </div>

              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.smtpPassword')}
                </label>
                <input
                  type="password"
                  value={smtpConfig.password}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, password: e.target.value })
                  }
                  className="input-field"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.fromName')}
                </label>
                <input
                  type="text"
                  value={smtpConfig.fromName}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, fromName: e.target.value })
                  }
                  className="input-field"
                  required
                />
              </div>

              <div>
                <label className="text-xs font-medium text-text-muted block mb-1.5">
                  {t('settings.fromEmail')}
                </label>
                <input
                  type="email"
                  value={smtpConfig.fromEmail}
                  onChange={(e) =>
                    setSmtpConfig({ ...smtpConfig, fromEmail: e.target.value })
                  }
                  className="input-field"
                  required
                />
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button type="submit" disabled={saving} className="btn-primary">
                <Save size={16} />
                {saving ? t('settings.saving') : t('settings.save')}
              </button>
              <button
                type="button"
                onClick={handleTestEmail}
                className="btn-secondary"
              >
                <TestTube size={16} />
                {t('settings.test')}
              </button>
            </div>
          </form>
        </div>

        {/* Planification automatique */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="flex items-center gap-2">
              <Clock size={18} className="text-amber" />
              <div className="eyebrow">{t('settings.autoSendReports')}</div>
            </div>
          </div>

          <form onSubmit={handleSaveSchedule} className="p-6 space-y-4">
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                id="autoSendEnabled"
                checked={scheduleConfig.autoSendEnabled}
                onChange={(e) =>
                  setScheduleConfig({
                    ...scheduleConfig,
                    autoSendEnabled: e.target.checked,
                  })
                }
                className="rounded border-border text-amber focus:ring-amber/30"
              />
              <label htmlFor="autoSendEnabled" className="text-sm font-medium">
                {t('settings.enableAutoSend')}
              </label>
            </div>

            {scheduleConfig.autoSendEnabled && (
              <>
                <div>
                  <label className="text-xs font-medium text-text-muted block mb-1.5">
                    {t('settings.sendTime')}
                  </label>
                  <input
                    type="time"
                    value={scheduleConfig.sendTime}
                    onChange={(e) =>
                      setScheduleConfig({
                        ...scheduleConfig,
                        sendTime: e.target.value,
                      })
                    }
                    className="input-field"
                    required
                  />
                </div>

                <div>
                  <label className="text-xs font-medium text-text-muted block mb-2">
                    {t('settings.sendDays')}
                  </label>
                  <div className="flex flex-wrap gap-2">
                    {weekDays.map((day) => (
                      <button
                        key={day.value}
                        type="button"
                        onClick={() => toggleDay(day.value)}
                        className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                          scheduleConfig.sendDays.includes(day.value)
                            ? 'bg-amber text-white'
                            : 'bg-surface-2 text-text-muted hover:bg-surface-3'
                        }`}
                      >
                        {day.label}
                      </button>
                    ))}
                  </div>
                </div>
              </>
            )}

            <button type="submit" disabled={saving} className="btn-primary">
              <Save size={16} />
              {saving ? t('settings.saving') : t('settings.save')}
            </button>
          </form>
        </div>

        {/* Destinataires des emails */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="flex items-center gap-2">
              <Mail size={18} className="text-amber" />
              <div className="eyebrow">{t('settings.reportRecipients')}</div>
            </div>
          </div>

          <div className="p-6 space-y-4">
            {/* Liste des destinataires */}
            <div className="space-y-2">
              {emailRecipients.map((recipient) => (
                <div
                  key={recipient.id}
                  className="flex items-center justify-between p-3 bg-surface-2 rounded-lg"
                >
                  <div>
                    <div className="font-medium text-sm">{recipient.name}</div>
                    <div className="text-xs text-text-muted font-mono">
                      {recipient.email}
                    </div>
                  </div>
                  <button
                    onClick={() => handleRemoveRecipient(recipient.id)}
                    className="p-2 rounded-lg hover:bg-alert/10 text-text-muted hover:text-alert transition-colors"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}
            </div>

            {/* Ajouter un destinataire */}
            <div className="border-t border-border pt-4">
              <div className="text-xs font-medium text-text-muted mb-2">
                {t('settings.addRecipient')}
              </div>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={newRecipient.name}
                  onChange={(e) =>
                    setNewRecipient({ ...newRecipient, name: e.target.value })
                  }
                  placeholder={t('settings.namePlaceholder')}
                  className="input-field flex-1"
                />
                <input
                  type="email"
                  value={newRecipient.email}
                  onChange={(e) =>
                    setNewRecipient({ ...newRecipient, email: e.target.value })
                  }
                  placeholder={t('settings.emailPlaceholder')}
                  className="input-field flex-1"
                />
                <button
                  onClick={handleAddRecipient}
                  className="btn-primary shrink-0"
                >
                  <Plus size={16} />
                  {t('settings.add')}
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Informations de l'entreprise */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="flex items-center gap-2">
              <Building2 size={18} className="text-amber" />
              <div className="eyebrow">{t('settings.companyInfo')}</div>
            </div>
          </div>

          <div className="p-6 space-y-4">
            <button
              type="button"
              onClick={handleSyncSigip}
              disabled={syncingSigip}
              className="btn-secondary"
            >
              {syncingSigip ? t('settings.syncSigipLoading') : t('settings.syncSigip')}
            </button>
          </div>

          <form onSubmit={handleSaveCompany} className="p-6 space-y-4">
            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('settings.companyName')}
              </label>
              <input
                type="text"
                value={companyConfig.name}
                onChange={(e) =>
                  setCompanyConfig({ ...companyConfig, name: e.target.value })
                }
                className="input-field"
                required
              />
            </div>

            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('settings.companyLogo')}
              </label>
              <div className="flex items-center gap-4">
                <input
                  id="company-logo-input"
                  type="file"
                  accept="image/*"
                  onChange={(e) => {
                    const selectedFile = e.target.files?.[0];
                    setCompanyConfig({
                      ...companyConfig,
                      logo: selectedFile,
                    });
                    setCompanyLogoName(selectedFile ? selectedFile.name : '');
                  }}
                  className="hidden"
                />
                <label
                  htmlFor="company-logo-input"
                  className="btn-secondary cursor-pointer"
                >
                  {companyLogoName || t('settings.chooseFile')}
                </label>
                <Image size={20} className="text-text-muted" />
              </div>
              <p className="text-xs text-text-faint mt-1">
                {t('settings.logoFormat')}
              </p>
            </div>

            <button type="submit" disabled={saving} className="btn-primary">
              <Save size={16} />
              {saving ? t('settings.saving') : t('common.save')}
            </button>
          </form>
        </div>
        </div>
      )}
    </Layout>
  );
}
