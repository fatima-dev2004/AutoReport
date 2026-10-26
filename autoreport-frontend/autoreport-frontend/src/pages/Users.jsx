import React, { useEffect, useState, useRef } from 'react';
import { UserPlus, Ban, CheckCircle, Trash2, X, Camera, Phone, ShieldCheck } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import { listUsers, createUser, setUserEnabled, changeUserRole, deleteUser } from '../api/users';
import { readReportCatalog, getVisibleReportIdsForUser, setVisibleReportIdsForUser } from '../utils/reportAccess';

const ROLES = ['ADMIN', 'MANAGER', 'USER'];

export default function Users() {
  const { t } = useLanguage();
  const toast = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ email: '', password: '', fullName: '', phone: '', role: 'USER', photoUrl: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [selectedAccessUser, setSelectedAccessUser] = useState('');
  const [selectedReportIds, setSelectedReportIds] = useState([]);
  const fileInputRef = useRef(null);

  const refresh = async () => {
    setLoading(true);
    try {
      const data = await listUsers();
      console.log('Users loaded:', data); // Debug
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error loading users:', err);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const handlePhotoSelect = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('Veuillez sélectionner une image valide.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : '';
      setForm((current) => ({ ...current, photoUrl: result }));
    };
    reader.readAsDataURL(file);
  };

  const reportCatalog = readReportCatalog();

  const handleAccessUserChange = (email) => {
    setSelectedAccessUser(email);
    setSelectedReportIds(getVisibleReportIdsForUser(email));
  };

  const toggleReportAccess = (reportId) => {
    setSelectedReportIds((current) => {
      const next = current.includes(String(reportId))
        ? current.filter((id) => id !== String(reportId))
        : [...current, String(reportId)];
      return next;
    });
  };

  const saveReportAccess = () => {
    if (!selectedAccessUser) return;
    setVisibleReportIdsForUser(selectedAccessUser, selectedReportIds);
    toast.success(t('users.reportAccessSaved'));
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const result = await createUser(form);
      console.log('User created:', result); // Debug

      // Afficher message de succès
      toast.success(t('users.created'));

      // Réinitialiser le formulaire
      setForm({ email: '', password: '', fullName: '', phone: '', role: 'USER', photoUrl: '' });
      if (fileInputRef.current) fileInputRef.current.value = '';
      setShowForm(false);
      
      // Attendre un peu avant de rafraîchir pour laisser le backend persister
      setTimeout(() => {
        refresh();
      }, 300);
      
    } catch (err) {
      console.error('Error creating user:', err);
      const errorMsg = err.response?.data?.error || t('users.createError');
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  const toggleEnabled = async (u) => {
    try {
      await setUserEnabled(u.id, !u.enabled);
      toast.success(t('users.updated'));
      refresh();
    } catch (err) {
      toast.error(t('users.updateError'));
    }
  };

  const handleRoleChange = async (u, role) => {
    try {
      await changeUserRole(u.id, role);
      toast.success(t('users.updated'));
      refresh();
    } catch (err) {
      toast.error(t('users.updateError'));
    }
  };

  const handleDelete = async (u) => {
    if (!confirm(t('users.deleteConfirm'))) return;
    try {
      await deleteUser(u.id);
      toast.success(t('users.deleted'));
      refresh();
    } catch (err) {
      toast.error(t('users.deleteError'));
    }
  };

  return (
    <Layout>
      <PageHeader
        eyebrow={t('users.eyebrow')}
        title={t('users.title')}
        action={
          <button className="btn-primary" onClick={() => setShowForm((v) => !v)}>
            {showForm ? <X size={16} /> : <UserPlus size={16} />}
            {showForm ? t('common.close') : t('users.addUser')}
          </button>
        }
      />

      <div className="p-8 space-y-6">
        {showForm && (
          <form onSubmit={handleCreate} className="panel p-5 grid grid-cols-1 md:grid-cols-5 gap-3 items-end">
            <div>
              <label className="eyebrow block mb-1.5">Nom complet</label>
              <input
                required
                className="input-field"
                value={form.fullName}
                onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
              />
            </div>
            <div>
              <label className="eyebrow block mb-1.5">Email</label>
              <input
                type="email"
                required
                className="input-field"
                value={form.email}
                onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              />
            </div>
            <div>
              <label className="eyebrow block mb-1.5">Téléphone</label>
              <div className="relative">
                <Phone size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
                <input
                  type="tel"
                  className="input-field pl-8"
                  value={form.phone}
                  onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                  placeholder="+212 600 000 000"
                />
              </div>
            </div>
            <div>
              <label className="eyebrow block mb-1.5">Mot de passe</label>
              <input
                type="password"
                required
                className="input-field"
                value={form.password}
                onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
              />
            </div>
            <div>
              <label className="eyebrow block mb-1.5">Rôle</label>
              <select
                className="input-field"
                value={form.role}
                onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>

            <div className="md:col-span-5">
              <div className="flex items-center gap-3 flex-wrap">
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="btn-secondary"
                >
                  <Camera size={16} />
                  {form.photoUrl ? 'Changer la photo' : 'Ajouter une photo'}
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handlePhotoSelect}
                />
                {form.photoUrl && (
                  <img
                    src={form.photoUrl}
                    alt="Preview"
                    className="h-14 w-14 rounded-full object-cover border border-border"
                  />
                )}
              </div>
            </div>
            {error && <div className="md:col-span-5 text-sm text-alert">{error}</div>}
            <div className="md:col-span-5">
              <button type="submit" disabled={submitting} className="btn-primary">
                {submitting ? 'Création...' : "Créer l'utilisateur"}
              </button>
            </div>
          </form>
        )}

        <div className="panel p-5">
          <div className="flex items-center gap-2 mb-3">
            <ShieldCheck size={18} className="text-blue-500" />
            <div className="eyebrow">{t('users.reportAccess')}</div>
          </div>
          <p className="text-sm text-text-muted mb-4">{t('users.reportAccessHint')}</p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-end">
            <div>
              <label className="eyebrow block mb-1.5">Utilisateur</label>
              <select
                className="input-field"
                value={selectedAccessUser}
                onChange={(e) => handleAccessUserChange(e.target.value)}
              >
                <option value="">Sélectionner un utilisateur</option>
                {users.map((user) => (
                  <option key={user.email} value={user.email}>{user.fullName || user.email}</option>
                ))}
              </select>
            </div>
            <div>
              <button
                type="button"
                onClick={saveReportAccess}
                disabled={!selectedAccessUser}
                className="btn-primary w-full disabled:opacity-50"
              >
                {t('users.reportAccessSaved')}
              </button>
            </div>
          </div>

          {selectedAccessUser && (
            <div className="mt-5 grid grid-cols-1 md:grid-cols-2 gap-3">
              {reportCatalog.map((report) => (
                <label
                  key={report.id}
                  className="flex items-center gap-3 rounded-xl border border-border bg-surface-2 px-3 py-2 text-sm text-text-muted"
                >
                  <input
                    type="checkbox"
                    checked={selectedReportIds.includes(String(report.id))}
                    onChange={() => toggleReportAccess(report.id)}
                    className="accent-blue-600"
                  />
                  <span>{report.title}</span>
                </label>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="overflow-x-auto">
            <table className="table-industrial w-full">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Email</th>
                  <th>Rôle</th>
                  <th>Statut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={5} className="text-center text-text-muted py-8">
                      Chargement...
                    </td>
                  </tr>
                )}
                {!loading && (!users || users.length === 0) && (
                  <tr>
                    <td colSpan={5} className="text-center text-text-muted py-8">
                      Aucun utilisateur. Créez-en un pour commencer.
                    </td>
                  </tr>
                )}
                {!loading &&
                  Array.isArray(users) &&
                  users.map((u) => (
                    <tr key={u.id}>
                      <td className="font-medium">{u.fullName}</td>
                      <td className="text-text-muted">{u.email}</td>
                      <td>
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u, e.target.value)}
                          className="bg-surface-2 border border-border rounded-lg px-2 py-1 text-xs font-mono"
                        >
                          {ROLES.map((r) => (
                            <option key={r} value={r}>
                              {r}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <span
                          className={`font-mono text-[11px] uppercase tracking-wide px-2 py-1 rounded-lg border ${
                            u.enabled
                              ? 'text-teal border-teal/30 bg-teal/10'
                              : 'text-alert border-alert/30 bg-alert/10'
                          }`}
                        >
                          {u.enabled ? 'Actif' : 'Désactivé'}
                        </span>
                      </td>
                      <td>
                        <div className="flex items-center gap-1.5 justify-end">
                          <button
                            title={u.enabled ? 'Désactiver' : 'Activer'}
                            onClick={() => toggleEnabled(u)}
                            className="p-2 rounded-lg hover:bg-surface-3 text-text-muted hover:text-amber transition-colors"
                          >
                            {u.enabled ? <Ban size={15} /> : <CheckCircle size={15} />}
                          </button>
                          <button
                            title="Supprimer"
                            onClick={() => handleDelete(u)}
                            className="p-2 rounded-lg hover:bg-surface-3 text-text-muted hover:text-alert transition-colors"
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </Layout>
  );
}
