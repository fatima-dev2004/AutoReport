import React, { useEffect, useState } from 'react';
import { Target, Plus, Edit2, Trash2, Loader2, CheckCircle, XCircle, TrendingUp, AlertTriangle, ChevronLeft, ChevronRight } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import {
  listObjectives,
  createObjective,
  updateObjective,
  deleteObjective,
  toggleObjectiveStatus,
  getProductionLines,
  getObjectiveStats,
} from '../api/objectives';

const PERIODS = [
  { value: 'DAILY', label_key: 'objectives.daily' },
  { value: 'WEEKLY', label_key: 'objectives.weekly' },
  { value: 'MONTHLY', label_key: 'objectives.monthly' },
  { value: 'QUARTERLY', label_key: 'objectives.quarterly' },
  { value: 'YEARLY', label_key: 'objectives.yearly' },
];

const todayISO = () => new Date().toISOString().slice(0, 10);

export default function Objectives() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const toast = useToast();
  const canManage = user?.role === 'ADMIN' || user?.role === 'MANAGER';

  const [objectivesData, setObjectivesData] = useState({
    content: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    size: 10,
  });

  const [loading, setLoading] = useState(true);
  const [filterLine, setFilterLine] = useState('all');
  const [filterPeriod, setFilterPeriod] = useState('all');
  const [filterActive, setFilterActive] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);

  const [productionLines, setProductionLines] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editingObjective, setEditingObjective] = useState(null);
  const [objectiveStats, setObjectiveStats] = useState({});

  const [formData, setFormData] = useState({
    line: '',
    period: 'MONTHLY',
    targetQuantity: '',
    targetQualityRate: '95',
    maxDefectRate: '5',
    startDate: todayISO(),
    endDate: '',
    active: true,
  });

  // Charger les objectifs
  const refresh = async () => {
    setLoading(true);
    try {
      const data = await listObjectives({
        page: currentPage,
        size: 10,
        line: filterLine !== 'all' ? filterLine : undefined,
        period: filterPeriod !== 'all' ? filterPeriod : undefined,
        active: filterActive,
      });
      setObjectivesData(data);

      // Charger les stats pour chaque objectif
      const stats = {};
      for (const obj of data.content) {
        try {
          const stat = await getObjectiveStats(obj.id);
          stats[obj.id] = stat;
        } catch (err) {
          stats[obj.id] = null;
        }
      }
      setObjectiveStats(stats);
    } catch (error) {
      console.error('Erreur chargement objectifs:', error);
      toast.error(t('objectives.loadError'));
    } finally {
      setLoading(false);
    }
  };

  // Charger les lignes de production
  const loadLines = async () => {
    try {
      const lines = await getProductionLines();
      setProductionLines(lines);
    } catch (error) {
      console.error('Erreur chargement lignes:', error);
    }
  };

  useEffect(() => {
    loadLines();
  }, []);

  useEffect(() => {
    refresh();
  }, [currentPage, filterLine, filterPeriod, filterActive]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Mapping période français → anglais pour le backend
    const periodMapping = {
      'DAILY': 'DAILY',
      'WEEKLY': 'WEEKLY',
      'MONTHLY': 'MONTHLY',
      'QUARTERLY': 'QUARTERLY',
      'YEARLY': 'YEARLY',
    };

    // Convertir et ajuster les noms de champs pour le backend
    const payload = {
      line: formData.line,
      periodType: formData.period, // Le backend attend 'periodType', pas 'period'
      targetQuantity: Number(formData.targetQuantity),
      targetQualityRate: formData.targetQualityRate ? Number(formData.targetQualityRate) : 95,
      maxDefects: formData.maxDefectRate ? Number(formData.maxDefectRate) : 5, // Backend attend 'maxDefects', pas 'maxDefectRate'
      startDate: formData.startDate,
      endDate: formData.endDate || null,
      active: formData.active,
      notes: formData.notes || '',
    };

    console.log('📤 Payload envoyé au backend:', JSON.stringify(payload, null, 2));

    try {
      if (editingObjective) {
        await updateObjective(editingObjective.id, payload);
        toast.success(t('objectives.updated'));
      } else {
        await createObjective(payload);
        toast.success(t('objectives.created'));
      }
      setShowModal(false);
      setEditingObjective(null);
      resetForm();
      refresh();
    } catch (error) {
      console.error('❌ Erreur sauvegarde objectif:', error);
      console.error('Réponse serveur:', error.response?.data);
      toast.error(error.response?.data?.error || error.response?.data?.message || (editingObjective ? t('objectives.updateError') : t('objectives.createError')));
    }
  };

  const handleEdit = (objective) => {
    setEditingObjective(objective);
    setFormData({
      line: objective.line,
      period: objective.period,
      targetQuantity: objective.targetQuantity,
      targetQualityRate: objective.targetQualityRate,
      maxDefectRate: objective.maxDefectRate,
      startDate: objective.startDate,
      endDate: objective.endDate || '',
      active: objective.active,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (!confirm(t('objectives.deleteConfirm'))) return;

    try {
      await deleteObjective(id);
      toast.success(t('objectives.deleted'));
      refresh();
    } catch (error) {
      console.error('Erreur suppression objectif:', error);
      toast.error(t('objectives.deleteError'));
    }
  };

  const handleToggleStatus = async (id, currentStatus) => {
    try {
      await toggleObjectiveStatus(id, !currentStatus);
      toast.success(`Objectif ${!currentStatus ? 'activé' : 'désactivé'}`);
      refresh();
    } catch (error) {
      console.error('Erreur changement statut:', error);
      toast.error('Erreur lors du changement de statut');
    }
  };

  const resetForm = () => {
    setFormData({
      line: '',
      period: 'MONTHLY',
      targetQuantity: '',
      targetQualityRate: '95',
      maxDefectRate: '5',
      startDate: todayISO(),
      endDate: '',
      active: true,
    });
  };

  const openCreateModal = () => {
    resetForm();
    setEditingObjective(null);
    setShowModal(true);
  };

  const getStatusBadge = (objective, stats) => {
    if (!stats) {
      return <span className="text-xs text-text-muted">—</span>;
    }

    const { rate, status } = stats;

    if (status === 'ACHIEVED') {
      return (
        <span className="flex items-center gap-1 text-xs text-teal">
          <CheckCircle size={12} />
          {t('objectives.achieved')} ({rate}%)
        </span>
      );
    } else if (status === 'IN_PROGRESS') {
      return (
        <span className="flex items-center gap-1 text-xs text-amber">
          <TrendingUp size={12} />
          {t('objectives.inProgress')} ({rate}%)
        </span>
      );
    } else {
      return (
        <span className="flex items-center gap-1 text-xs text-alert">
          <AlertTriangle size={12} />
          {t('objectives.failed')} ({rate}%)
        </span>
      );
    }
  };

  return (
    <Layout>
      <PageHeader
        eyebrow={t('objectives.eyebrow')}
        title={t('objectives.title')}
        action={
          canManage && (
            <button onClick={openCreateModal} className="btn-primary">
              <Plus size={16} />
              {t('objectives.addObjective')}
            </button>
          )
        }
      />

      <div className="p-8 space-y-6">
        {/* Filtres */}
        <div className="panel p-5">
          <div className="eyebrow mb-3">{t('reports.filters')}</div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <select
              value={filterLine}
              onChange={(e) => {
                setFilterLine(e.target.value);
                setCurrentPage(0);
              }}
              className="input-field !py-2"
            >
              <option value="all">{t('objectives.filterAll')}</option>
              {productionLines.map((line) => (
                <option key={line} value={line}>
                  {line}
                </option>
              ))}
            </select>

            <select
              value={filterPeriod}
              onChange={(e) => {
                setFilterPeriod(e.target.value);
                setCurrentPage(0);
              }}
              className="input-field !py-2"
            >
              <option value="all">{t('objectives.filterAllPeriods')}</option>
              {PERIODS.map((p) => (
                <option key={p.value} value={p.value}>
                  {t(p.label_key)}
                </option>
              ))}
            </select>

            <select
              value={filterActive ? 'active' : 'inactive'}
              onChange={(e) => {
                setFilterActive(e.target.value === 'active');
                setCurrentPage(0);
              }}
              className="input-field !py-2"
            >
              <option value="active">{t('objectives.activeOnly')}</option>
              <option value="inactive">{t('common.inactive')}</option>
            </select>
          </div>
        </div>

        {/* Liste des objectifs */}
        <div className="panel">
          <div className="p-5 border-b border-border flex items-center justify-between gap-4">
            <div className="eyebrow">
              {objectivesData.totalElements} {t('objectives.title').toLowerCase()}
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="table-industrial w-full">
              <thead>
                <tr>
                  <th>{t('objectives.line')}</th>
                  <th>{t('objectives.period')}</th>
                  <th>{t('objectives.targetQuantity')}</th>
                  <th>{t('objectives.targetQualityRate')}</th>
                  <th>{t('objectives.maxDefects')}</th>
                  <th>{t('objectives.startDate')}</th>
                  <th>{t('objectives.progress')}</th>
                  <th>{t('objectives.status')}</th>
                  {canManage && <th></th>}
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={9} className="text-center text-text-muted py-8">
                      <Loader2 className="animate-spin inline-block mr-2" size={16} />
                      {t('common.loading')}
                    </td>
                  </tr>
                )}
                {!loading && objectivesData.content.length === 0 && (
                  <tr>
                    <td colSpan={9} className="text-center text-text-muted py-8">
                      {t('objectives.noObjectives')}
                    </td>
                  </tr>
                )}
                {objectivesData.content.map((obj) => (
                  <tr key={obj.id}>
                    <td className="font-medium">{obj.line}</td>
                    <td>
                      <span className="font-mono text-xs uppercase text-text-muted">
                        {t(PERIODS.find((p) => p.value === obj.period)?.label_key || 'objectives.monthly')}
                      </span>
                    </td>
                    <td className="font-mono">{obj.targetQuantity?.toLocaleString()}</td>
                    <td className="font-mono text-teal">{obj.targetQualityRate}%</td>
                    <td className="font-mono text-alert">{obj.maxDefectRate}%</td>
                    <td className="font-mono text-xs text-text-muted">
                      {obj.startDate}
                      {obj.endDate && ` → ${obj.endDate}`}
                    </td>
                    <td>
                      {objectiveStats[obj.id] ? (
                        <div className="text-sm">
                          <div className="font-mono">
                            {objectiveStats[obj.id].achieved?.toLocaleString()} / {obj.targetQuantity?.toLocaleString()}
                          </div>
                          <div className="text-xs text-text-muted">
                            {objectiveStats[obj.id].rate}% réalisé
                          </div>
                        </div>
                      ) : (
                        <span className="text-text-muted text-xs">—</span>
                      )}
                    </td>
                    <td>
                      {obj.active ? (
                        getStatusBadge(obj, objectiveStats[obj.id])
                      ) : (
                        <span className="text-xs text-text-faint">{t('common.inactive')}</span>
                      )}
                    </td>
                    {canManage && (
                      <td>
                        <div className="flex items-center gap-1.5 justify-end">
                          <button
                            onClick={() => handleToggleStatus(obj.id, obj.active)}
                            className={`p-2 rounded-lg transition-colors ${
                              obj.active
                                ? 'hover:bg-surface-3 hover:text-alert text-text-muted'
                                : 'hover:bg-surface-3 hover:text-teal text-text-muted'
                            }`}
                            title={obj.active ? t('common.inactive') : t('common.active')}
                          >
                            {obj.active ? <XCircle size={15} /> : <CheckCircle size={15} />}
                          </button>
                          <button
                            onClick={() => handleEdit(obj)}
                            className="p-2 rounded-lg hover:bg-surface-3 hover:text-amber text-text-muted transition-colors"
                            title={t('common.edit')}
                          >
                            <Edit2 size={15} />
                          </button>
                          <button
                            onClick={() => handleDelete(obj.id)}
                            className="p-2 rounded-lg hover:bg-surface-3 hover:text-alert text-text-muted transition-colors"
                            title={t('common.delete')}
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {objectivesData.totalPages > 1 && (
            <div className="p-4 border-t border-border flex items-center justify-between">
              <div className="text-sm text-text-muted">
                {t('reports.page')} {objectivesData.currentPage + 1} {t('reports.of')} {objectivesData.totalPages}
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  disabled={currentPage === 0}
                  className="btn-secondary text-sm disabled:opacity-50"
                >
                  <ChevronLeft size={14} />
                  {t('reports.previous')}
                </button>
                <button
                  onClick={() => setCurrentPage((p) => Math.min(objectivesData.totalPages - 1, p + 1))}
                  disabled={currentPage === objectivesData.totalPages - 1}
                  className="btn-secondary text-sm disabled:opacity-50"
                >
                  {t('reports.next')}
                  <ChevronRight size={14} />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Modal Création/Édition */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingObjective(null);
          resetForm();
        }}
        title={editingObjective ? t('objectives.editObjective') : t('objectives.addObjective')}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-text mb-1">
              {t('objectives.line')} *
            </label>
            <select
              value={formData.line}
              onChange={(e) => setFormData({ ...formData, line: e.target.value })}
              className="input-field"
              required
            >
              <option value="">{t('objectives.selectLine')}</option>
              {productionLines.map((line) => (
                <option key={line} value={line}>
                  {line}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-text mb-1">
              {t('objectives.period')} *
            </label>
            <select
              value={formData.period}
              onChange={(e) => setFormData({ ...formData, period: e.target.value })}
              className="input-field"
              required
            >
              {PERIODS.map((p) => (
                <option key={p.value} value={p.value}>
                  {t(p.label_key)}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-text mb-1">
              {t('objectives.targetQuantity')} *
            </label>
            <input
              type="number"
              value={formData.targetQuantity}
              onChange={(e) => setFormData({ ...formData, targetQuantity: e.target.value })}
              className="input-field"
              min="1"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-text mb-1">
                {t('objectives.targetQualityRate')}
              </label>
              <input
                type="number"
                value={formData.targetQualityRate}
                onChange={(e) => setFormData({ ...formData, targetQualityRate: e.target.value })}
                className="input-field"
                min="0"
                max="100"
                step="0.1"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-1">
                {t('objectives.maxDefects')}
              </label>
              <input
                type="number"
                value={formData.maxDefectRate}
                onChange={(e) => setFormData({ ...formData, maxDefectRate: e.target.value })}
                className="input-field"
                min="0"
                max="100"
                step="0.1"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-text mb-1">
                {t('objectives.startDate')} *
              </label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                className="input-field"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-1">
                {t('objectives.endDate')}
              </label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                className="input-field"
              />
            </div>
          </div>

          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={formData.active}
              onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
              className="accent-amber"
            />
            <span className="text-sm text-text-muted">{t('objectives.active')}</span>
          </label>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={() => {
                setShowModal(false);
                setEditingObjective(null);
                resetForm();
              }}
              className="btn-secondary flex-1"
            >
              {t('common.cancel')}
            </button>
            <button type="submit" className="btn-primary flex-1">
              <Target size={16} />
              {editingObjective ? t('common.save') : t('common.confirm')}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  );
}
