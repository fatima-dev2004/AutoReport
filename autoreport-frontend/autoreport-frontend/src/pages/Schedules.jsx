import React, { useEffect, useState } from 'react';
import { CalendarPlus, Trash2, Power, X } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useLanguage } from '../context/LanguageContext';
import { listSchedules, createSchedule, updateSchedule, deleteSchedule } from '../api/schedules';

const TYPES = ['PRODUCTION', 'QUALITE', 'AUTRE'];

const getPRESETS = (t) => [
  { label: t('schedules.presetDaily'), cron: '0 0 8 * * *' },
  { label: t('schedules.presetWeekly'), cron: '0 0 8 * * MON' },
  { label: t('schedules.presetMonthly'), cron: '0 0 8 1 * *' },
];

export default function Schedules() {
  const { t } = useLanguage();
  const PRESETS = getPRESETS(t);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    reportType: 'PRODUCTION',
    cronExpression: PRESETS[0].cron,
    recipients: '',
    active: true,
  });

  const refresh = () => {
    setLoading(true);
    listSchedules()
      .then(setSchedules)
      .catch(() => setSchedules([]))
      .finally(() => setLoading(false));
  };

  useEffect(refresh, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await createSchedule(form);
      setForm({ reportType: 'PRODUCTION', cronExpression: PRESETS[0].cron, recipients: '', active: true });
      setShowForm(false);
      refresh();
    } catch (err) {
      setError(err.response?.data?.error || t('schedules.createError'));
    } finally {
      setSubmitting(false);
    }
  };

  const toggleActive = async (s) => {
    await updateSchedule(s.id, { ...s, active: !s.active });
    refresh();
  };

  const handleDelete = async (s) => {
    if (!confirm(t('schedules.deleteConfirm'))) return;
    await deleteSchedule(s.id);
    refresh();
  };

  return (
    <Layout>
      <PageHeader
        eyebrow={t('schedules.eyebrow')}
        title={t('schedules.title')}
        action={
          <button className="btn-primary" onClick={() => setShowForm((v) => !v)}>
            {showForm ? <X size={16} /> : <CalendarPlus size={16} />}
            {showForm ? t('common.close') : t('schedules.addSchedule')}
          </button>
        }
      />

      <div className="p-8 space-y-6">
        {showForm && (
          <form onSubmit={handleCreate} className="panel p-5 grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
            <div>
              <label className="eyebrow block mb-1.5">{t('schedules.reportType')}</label>
              <select
                className="input-field"
                value={form.reportType}
                onChange={(e) => setForm((f) => ({ ...f, reportType: e.target.value }))}
              >
                {TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="eyebrow block mb-1.5">{t('schedules.frequency')}</label>
              <select
                className="input-field"
                value={form.cronExpression}
                onChange={(e) => setForm((f) => ({ ...f, cronExpression: e.target.value }))}
              >
                {PRESETS.map((p) => (
                  <option key={p.cron} value={p.cron}>
                    {p.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="eyebrow block mb-1.5">{t('schedules.recipientsLabel')}</label>
              <input
                required
                placeholder={t('schedules.recipientsPlaceholder')}
                className="input-field"
                value={form.recipients}
                onChange={(e) => setForm((f) => ({ ...f, recipients: e.target.value }))}
              />
            </div>
            {error && <div className="md:col-span-4 text-sm text-alert">{error}</div>}
            <div className="md:col-span-4">
              <button type="submit" disabled={submitting} className="btn-primary">
                {submitting ? t('schedules.creating') : t('schedules.createButton')}
              </button>
            </div>
          </form>
        )}

        <div className="panel">
          <div className="overflow-x-auto">
            <table className="table-industrial w-full">
              <thead>
                <tr>
                  <th>{t('schedules.type')}</th>
                  <th>{t('schedules.frequencyCron')}</th>
                  <th>{t('schedules.recipients')}</th>
                  <th>{t('schedules.status')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={5} className="text-center text-text-muted py-8">
                      {t('common.loading')}
                    </td>
                  </tr>
                )}
                {!loading && schedules.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-center text-text-muted py-8">
                      {t('schedules.noSchedulesMessage')}
                    </td>
                  </tr>
                )}
                {schedules.map((s) => (
                  <tr key={s.id}>
                    <td className="font-medium">{s.reportType}</td>
                    <td className="font-mono text-xs text-text-muted">{s.cronExpression}</td>
                    <td className="text-text-muted text-sm">{s.recipients}</td>
                    <td>
                      <span
                        className={`font-mono text-[11px] uppercase tracking-wide px-2 py-1 rounded-lg border ${
                          s.active ? 'text-teal border-teal/30 bg-teal/10' : 'text-text-faint border-border bg-surface-2'
                        }`}
                      >
                        {s.active ? t('schedules.statusActive') : t('schedules.statusSuspended')}
                      </span>
                    </td>
                    <td>
                      <div className="flex items-center gap-1.5 justify-end">
                        <button
                          title={s.active ? t('schedules.suspend') : t('schedules.activate')}
                          onClick={() => toggleActive(s)}
                          className="p-2 rounded-lg hover:bg-surface-3 text-text-muted hover:text-amber transition-colors"
                        >
                          <Power size={15} />
                        </button>
                        <button
                          title={t('common.delete')}
                          onClick={() => handleDelete(s)}
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
