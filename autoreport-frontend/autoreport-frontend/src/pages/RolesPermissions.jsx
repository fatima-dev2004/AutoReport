import React, { useEffect, useMemo, useState } from 'react';
import { ShieldCheck, Save, RotateCcw } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import {
  listUsers,
  getAvailablePermissions,
  getUserPermissions,
  setUserPermissions,
} from '../api/users';

const ROLES = ['ADMIN', 'MANAGER', 'USER'];

// Regroupement des permissions par catégorie (affichage uniquement)
const CATEGORY_BY_PREFIX = [
  { match: ['CREATE_USERS', 'VIEW_USERS', 'EDIT_USERS', 'DELETE_USERS', 'MANAGE_PERMISSIONS'], label: 'Gestion des utilisateurs' },
  { match: ['IMPORT_EXCEL', 'VIEW_IMPORT_HISTORY', 'DELETE_IMPORT_DATA'], label: 'Import Excel' },
  { match: ['GENERATE_REPORTS', 'VIEW_REPORTS', 'DOWNLOAD_REPORTS', 'DELETE_REPORTS', 'SHARE_REPORTS'], label: 'Rapports' },
  { match: ['VIEW_DASHBOARD', 'EXPORT_DASHBOARD'], label: 'Tableau de bord' },
  { match: ['CREATE_SCHEDULES', 'VIEW_SCHEDULES', 'EDIT_SCHEDULES', 'DELETE_SCHEDULES'], label: 'Planification' },
  { match: ['CONFIGURE_SYSTEM', 'VIEW_LOGS', 'MANAGE_EMAIL_SETTINGS'], label: 'Configuration système' },
];

function categoryFor(permissionName) {
  const found = CATEGORY_BY_PREFIX.find((c) => c.match.includes(permissionName));
  return found ? found.label : 'Autres';
}

export default function RolesPermissions() {
  const { t } = useLanguage();
  const toast = useToast();

  const [users, setUsers] = useState([]);
  const [allPermissions, setAllPermissions] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [checkedPermissions, setCheckedPermissions] = useState([]);
  const [initialPermissions, setInitialPermissions] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingPermissions, setLoadingPermissions] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    (async () => {
      setLoadingUsers(true);
      try {
        const [usersData, permsData] = await Promise.all([listUsers(), getAvailablePermissions()]);
        setUsers(Array.isArray(usersData) ? usersData : []);
        setAllPermissions(Array.isArray(permsData) ? permsData : []);
      } catch (err) {
        toast.error("Impossible de charger les utilisateurs ou permissions");
      } finally {
        setLoadingUsers(false);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selectedUser = useMemo(
    () => users.find((u) => String(u.id) === String(selectedUserId)),
    [users, selectedUserId]
  );

  const groupedPermissions = useMemo(() => {
    const groups = {};
    allPermissions.forEach((p) => {
      const cat = categoryFor(p.name);
      if (!groups[cat]) groups[cat] = [];
      groups[cat].push(p);
    });
    return groups;
  }, [allPermissions]);

  const handleSelectUser = async (id) => {
    setSelectedUserId(id);
    if (!id) {
      setCheckedPermissions([]);
      setInitialPermissions([]);
      return;
    }
    setLoadingPermissions(true);
    try {
      const perms = await getUserPermissions(id);
      setCheckedPermissions(perms);
      setInitialPermissions(perms);
    } catch (err) {
      toast.error("Impossible de charger les permissions de cet utilisateur");
    } finally {
      setLoadingPermissions(false);
    }
  };

  const togglePermission = (name) => {
    setCheckedPermissions((current) =>
      current.includes(name) ? current.filter((p) => p !== name) : [...current, name]
    );
  };

  const toggleCategory = (category, allChecked) => {
    const namesInCategory = (groupedPermissions[category] || []).map((p) => p.name);
    setCheckedPermissions((current) => {
      if (allChecked) {
        return current.filter((p) => !namesInCategory.includes(p));
      }
      const merged = new Set([...current, ...namesInCategory]);
      return Array.from(merged);
    });
  };

  const hasChanges = useMemo(() => {
    if (checkedPermissions.length !== initialPermissions.length) return true;
    const a = [...checkedPermissions].sort();
    const b = [...initialPermissions].sort();
    return a.some((v, i) => v !== b[i]);
  }, [checkedPermissions, initialPermissions]);

  const handleSave = async () => {
    if (!selectedUserId) return;
    setSaving(true);
    try {
      await setUserPermissions(selectedUserId, checkedPermissions);
      setInitialPermissions(checkedPermissions);
      toast.success('Permissions mises à jour avec succès');
    } catch (err) {
      toast.error('Erreur lors de la mise à jour des permissions');
    } finally {
      setSaving(false);
    }
  };

  const handleReset = () => setCheckedPermissions(initialPermissions);

  const roleLabel = (role) => {
    if (role === 'ADMIN') return t('users.admin');
    if (role === 'MANAGER') return t('users.manager');
    return t('users.user');
  };

  return (
    <Layout>
      <PageHeader eyebrow="Gestion des accès" title="Rôles & Permissions" />

      <div className="p-8 space-y-6">
        <div className="panel p-5">
          <div className="flex items-center gap-2 mb-3">
            <ShieldCheck size={18} className="text-blue-500" />
            <div className="eyebrow">Sélectionner un utilisateur</div>
          </div>
          <p className="text-sm text-text-muted mb-4">
            Choisissez un utilisateur pour voir et modifier ses permissions individuelles, au-delà de celles
            attribuées par défaut à son rôle.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <div className="md:col-span-2">
              <label className="eyebrow block mb-1.5">Utilisateur</label>
              <select
                className="input-field"
                value={selectedUserId}
                onChange={(e) => handleSelectUser(e.target.value)}
                disabled={loadingUsers}
              >
                <option value="">
                  {loadingUsers ? 'Chargement...' : 'Sélectionner un utilisateur'}
                </option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {(u.fullName || u.email)} — {roleLabel(u.role)}
                  </option>
                ))}
              </select>
            </div>
            {selectedUser && (
              <div className="text-sm text-text-muted">
                Rôle actuel :{' '}
                <span className="font-mono text-xs uppercase tracking-wide px-2 py-1 rounded-lg border border-blue-500/30 bg-blue-500/10 text-blue-500">
                  {roleLabel(selectedUser.role)}
                </span>
              </div>
            )}
          </div>
        </div>

        {selectedUserId && (
          <div className="panel p-5">
            <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
              <div>
                <div className="eyebrow mb-1">Permissions</div>
                <p className="text-sm text-text-muted">
                  Les permissions par défaut du rôle sont déjà cochées. Ajoutez ou retirez des droits précis
                  sans changer le rôle de l'utilisateur.
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={handleReset}
                  disabled={!hasChanges || saving}
                  className="btn-secondary disabled:opacity-50 flex items-center gap-2"
                >
                  <RotateCcw size={15} />
                  Annuler
                </button>
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={!hasChanges || saving}
                  className="btn-primary disabled:opacity-50 flex items-center gap-2"
                >
                  <Save size={15} />
                  {saving ? 'Enregistrement...' : 'Enregistrer'}
                </button>
              </div>
            </div>

            {loadingPermissions ? (
              <div className="text-sm text-text-muted py-6 text-center">Chargement des permissions...</div>
            ) : (
              <div className="space-y-5">
                {Object.entries(groupedPermissions).map(([category, perms]) => {
                  const namesInCategory = perms.map((p) => p.name);
                  const allChecked = namesInCategory.every((n) => checkedPermissions.includes(n));
                  return (
                    <div key={category}>
                      <div className="flex items-center justify-between mb-2">
                        <div className="text-xs font-mono uppercase tracking-wide text-text-muted">
                          {category}
                        </div>
                        <button
                          type="button"
                          onClick={() => toggleCategory(category, allChecked)}
                          className="text-xs text-blue-500 hover:underline"
                        >
                          {allChecked ? 'Tout décocher' : 'Tout cocher'}
                        </button>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {perms.map((perm) => (
                          <label
                            key={perm.name}
                            className="flex items-center gap-3 rounded-xl border border-border bg-surface-2 px-3 py-2 text-sm text-text-muted"
                          >
                            <input
                              type="checkbox"
                              checked={checkedPermissions.includes(perm.name)}
                              onChange={() => togglePermission(perm.name)}
                              className="accent-blue-600"
                            />
                            <span>{perm.description}</span>
                          </label>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>
    </Layout>
  );
}
