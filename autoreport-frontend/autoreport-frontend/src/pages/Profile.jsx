import React, { useState, useRef, useEffect } from 'react';
import { User, Mail, Shield, Lock, Save, Eye, EyeOff, Camera, Phone } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';

export default function Profile() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const toast = useToast();
  const fileInputRef = useRef(null);

  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
  });
  const [profilePhoto, setProfilePhoto] = useState(user?.photoUrl || '');

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  });

  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    setFormData({
      fullName: user?.fullName || '',
      email: user?.email || '',
      phone: user?.phone || '',
    });
    setProfilePhoto(user?.photoUrl || '');
  }, [user]);

  const handlePhotoChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error(t('profile.invalidImage'));
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result;
      setProfilePhoto(typeof result === 'string' ? result : '');
      const currentUser = JSON.parse(localStorage.getItem('autoreport_user') || '{}');
      const updatedUser = { ...currentUser, photoUrl: result };
      localStorage.setItem('autoreport_user', JSON.stringify(updatedUser));
    };
    reader.readAsDataURL(file);
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSavingProfile(true);

    try {
      // TODO: Appel API pour mettre à jour le profil
      // await updateProfile(formData);
      
      // Simulation
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      toast.success(t('profile.saved'));
      setEditing(false);
    } catch (err) {
      toast.error(t('profile.error'));
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error(t('profile.passwordMismatch'));
      return;
    }

    if (passwordData.newPassword.length < 8) {
      toast.error(t('profile.passwordTooShort'));
      return;
    }

    setSavingPassword(true);

    try {
      // TODO: Appel API pour changer le mot de passe
      // await changePassword(passwordData);
      
      // Simulation
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      toast.success(t('profile.passwordChanged'));
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (err) {
      toast.error(t('profile.passwordError'));
    } finally {
      setSavingPassword(false);
    }
  };

  const getRoleBadge = (role) => {
    const badges = {
      ADMIN: { label: t('users.admin'), color: 'bg-purple-100 text-purple-700 border-purple-200' },
      MANAGER: { label: t('users.manager'), color: 'bg-blue-100 text-blue-700 border-blue-200' },
      USER: { label: t('users.user'), color: 'bg-gray-100 text-gray-700 border-gray-200' },
    };
    return badges[role] || badges.USER;
  };

  const roleBadge = getRoleBadge(user?.role);

  return (
    <Layout>
      <PageHeader eyebrow={t('profile.eyebrow')} title={t('profile.title')} />

      <div className="p-8 space-y-6 max-w-4xl">
        {/* Carte de profil */}
        <div className="panel p-6">
          <div className="flex items-start gap-6">
            {/* Avatar */}
            <div className="shrink-0 relative">
              <div className="h-24 w-24 rounded-full overflow-hidden bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white text-3xl font-display font-bold shadow-lg ring-4 ring-white">
                {profilePhoto ? (
                  <img src={profilePhoto} alt={user?.fullName || 'Profile'} className="h-full w-full object-cover" />
                ) : (
                  user?.fullName?.charAt(0)?.toUpperCase() || 'U'
                )}
              </div>
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="absolute -bottom-2 -right-2 h-9 w-9 rounded-full bg-blue-600 text-white flex items-center justify-center shadow-lg hover:bg-blue-700 transition"
                aria-label={t('profile.changePhoto')}
              >
                <Camera size={16} />
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handlePhotoChange}
              />
            </div>

            {/* Informations */}
            <div className="flex-1">
              <h2 className="font-display font-bold text-2xl text-text mb-1">
                {user?.fullName}
              </h2>
              <p className="text-text-muted mb-3">{user?.email}</p>
              <div className="flex items-center gap-2">
                <span
                  className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border text-xs font-medium ${roleBadge.color}`}
                >
                  <Shield size={12} />
                  {roleBadge.label}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Modifier les informations personnelles */}
        <div className="panel">
          <div className="p-5 border-b border-border flex items-center justify-between">
            <div className="eyebrow">{t('profile.personalInfo')}</div>
            {!editing && (
              <button
                onClick={() => setEditing(true)}
                className="btn-secondary"
              >
                {t('common.edit')}
              </button>
            )}
          </div>

          <form onSubmit={handleSaveProfile} className="p-6 space-y-4">
            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.name')}
              </label>
              <div className="relative">
                <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type="text"
                  value={formData.fullName}
                  onChange={(e) =>
                    setFormData({ ...formData, fullName: e.target.value })
                  }
                  disabled={!editing}
                  className="input-field pl-9"
                  required
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.email')}
              </label>
              <div className="relative">
                <Mail size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                  disabled={!editing}
                  className="input-field pl-9"
                  required
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.phone')}
              </label>
              <div className="relative">
                <Phone size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type="tel"
                  value={formData.phone}
                  onChange={(e) =>
                    setFormData({ ...formData, phone: e.target.value })
                  }
                  disabled={!editing}
                  className="input-field pl-9"
                  placeholder={t('profile.phonePlaceholder')}
                />
              </div>
            </div>

            {editing && (
              <div className="flex gap-3 pt-2">
                <button
                  type="submit"
                  disabled={savingProfile}
                  className="btn-primary"
                >
                  <Save size={16} />
                  {savingProfile ? t('profile.saving') : t('common.save')}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditing(false);
                    setFormData({
                      fullName: user?.fullName || '',
                      email: user?.email || '',
                    });
                  }}
                  className="btn-secondary"
                >
                  {t('common.cancel')}
                </button>
              </div>
            )}
          </form>
        </div>

        {/* Changer le mot de passe */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="eyebrow">{t('profile.security')}</div>
          </div>

          <form onSubmit={handleChangePassword} className="p-6 space-y-4">
            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.currentPassword')}
              </label>
              <div className="relative">
                <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type={showPasswords.current ? 'text' : 'password'}
                  value={passwordData.currentPassword}
                  onChange={(e) =>
                    setPasswordData({
                      ...passwordData,
                      currentPassword: e.target.value,
                    })
                  }
                  className="input-field pl-9 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() =>
                    setShowPasswords({
                      ...showPasswords,
                      current: !showPasswords.current,
                    })
                  }
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text"
                >
                  {showPasswords.current ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.newPassword')}
              </label>
              <div className="relative">
                <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type={showPasswords.new ? 'text' : 'password'}
                  value={passwordData.newPassword}
                  onChange={(e) =>
                    setPasswordData({ ...passwordData, newPassword: e.target.value })
                  }
                  className="input-field pl-9 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() =>
                    setShowPasswords({ ...showPasswords, new: !showPasswords.new })
                  }
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text"
                >
                  {showPasswords.new ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              <p className="text-xs text-text-faint mt-1">
                {t('profile.passwordMinLength')}
              </p>
            </div>

            <div>
              <label className="text-xs font-medium text-text-muted block mb-1.5">
                {t('profile.confirmPassword')}
              </label>
              <div className="relative">
                <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type={showPasswords.confirm ? 'text' : 'password'}
                  value={passwordData.confirmPassword}
                  onChange={(e) =>
                    setPasswordData({
                      ...passwordData,
                      confirmPassword: e.target.value,
                    })
                  }
                  className="input-field pl-9 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() =>
                    setShowPasswords({
                      ...showPasswords,
                      confirm: !showPasswords.confirm,
                    })
                  }
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text"
                >
                  {showPasswords.confirm ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={savingPassword}
              className="btn-primary"
            >
              <Lock size={16} />
              {savingPassword ? t('profile.changing') : t('profile.changePassword')}
            </button>
          </form>
        </div>

        {/* Statistiques utilisateur */}
        <div className="panel p-6">
          <div className="eyebrow mb-4">{t('profile.statistics')}</div>
          <div className="grid grid-cols-3 gap-4">
            <div className="text-center p-4 bg-surface-2 rounded-lg">
              <div className="text-2xl font-display font-bold text-amber mb-1">12</div>
              <div className="text-xs text-text-muted">{t('profile.reportsGenerated')}</div>
            </div>
            <div className="text-center p-4 bg-surface-2 rounded-lg">
              <div className="text-2xl font-display font-bold text-teal mb-1">8</div>
              <div className="text-xs text-text-muted">{t('profile.excelImports')}</div>
            </div>
            <div className="text-center p-4 bg-surface-2 rounded-lg">
              <div className="text-2xl font-display font-bold text-text mb-1">45</div>
              <div className="text-xs text-text-muted">{t('profile.logins')}</div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
