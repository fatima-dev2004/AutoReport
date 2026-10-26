import React, { useEffect, useRef, useState, useCallback } from 'react';
import { Search, Upload, FileDown, FileSpreadsheet, Loader2, CheckCircle2, XCircle, Filter, Calendar, User as UserIcon, ChevronLeft, ChevronRight, Send } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import { listReports, getReportFilters, importExcel, generateReport, generateEnhancedReport, downloadReport } from '../api/reports';
import { getVisibleReportIdsForUser } from '../utils/reportAccess';
import EmailVerificationModal from '../components/EmailVerificationModal';

const todayISO = () => new Date().toISOString().slice(0, 10);
const daysAgoISO = (n) => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
};

const FILTERS_PRESETS = [
  { label_key: "reports.today", getValue: () => ({ start: todayISO(), end: todayISO() }) },
  { label_key: 'reports.thisWeek', getValue: () => ({ start: daysAgoISO(7), end: todayISO() }) },
  { label_key: 'reports.thisMonth', getValue: () => ({ start: daysAgoISO(30), end: todayISO() }) },
  { label_key: 'reports.thisYear', getValue: () => ({ start: daysAgoISO(365), end: todayISO() }) },
];

export default function Reports() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const toast = useToast();
  const canManage = user?.role === 'ADMIN' || user?.role === 'MANAGER';
  const canViewAllReports = canManage || user?.role === 'ADMIN' || user?.role === 'MANAGER';

  // État pour la pagination serveur
  const [reportsData, setReportsData] = useState({
    content: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    size: 10,
  });

  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [filterUser, setFilterUser] = useState('all');
  const [filterType, setFilterType] = useState('all');
  const [dateRange, setDateRange] = useState({ start: '', end: '' });
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed pour le backend

  // Filtres disponibles
  const [availableFilters, setAvailableFilters] = useState({ users: [], types: [] });
  const filterUsers = Array.isArray(availableFilters?.users) ? availableFilters.users : [];
  const filterTypes = Array.isArray(availableFilters?.types) ? availableFilters.types : [];
  const displayReports = Array.isArray(reportsData?.content) ? reportsData.content : [];

  const [genRange, setGenRange] = useState({ start: daysAgoISO(7), end: todayISO() });
  const [generating, setGenerating] = useState(false);
  const [withDesign, setWithDesign] = useState(true);

  const [importState, setImportState] = useState({ status: 'idle', message: '' });
  const fileInputRef = useRef(null);

  // État pour la vérification email
  const [emailVerificationModal, setEmailVerificationModal] = useState({
    isOpen: false,
    reportId: null,
    reportTitle: '',
  });

  // Charger les rapports avec pagination serveur
  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listReports({
        page: currentPage,
        size: 10,
        search: search || undefined,
        userEmail: filterUser !== 'all' ? filterUser : undefined,
        type: filterType !== 'all' ? filterType : undefined,
        startDate: dateRange.start || undefined,
        endDate: dateRange.end || undefined,
        sortBy: 'generatedAt',
        sortDir: 'desc',
      });

      const rawContent = Array.isArray(data?.content) ? data.content : [];
      const visibleIds = user?.email ? getVisibleReportIdsForUser(user.email) : [];
      const filteredContent = user?.role && !canManage && visibleIds.length > 0
        ? rawContent.filter((report) => visibleIds.includes(String(report.id)))
        : rawContent;

      setReportsData({
        ...data,
        content: filteredContent,
        totalElements: filteredContent.length,
        totalPages: Math.max(1, Math.ceil(filteredContent.length / (data?.size || 10))),
      });
    } catch (error) {
      console.error('Erreur chargement rapports:', error);
      setReportsData({
        content: [],
        totalElements: 0,
        totalPages: 0,
        currentPage: 0,
        size: 10,
      });
    } finally {
      setLoading(false);
    }
  }, [currentPage, search, filterUser, filterType, dateRange]);

  // Charger les filtres disponibles
  const loadFilters = async () => {
    try {
      const filters = await getReportFilters();
      setAvailableFilters({
        users: Array.isArray(filters?.users) ? filters.users : [],
        types: Array.isArray(filters?.types) ? filters.types : [],
      });
    } catch (error) {
      console.error('Erreur chargement filtres:', error);
      setAvailableFilters({ users: [], types: [] });
    }
  };

  // Charger les filtres au montage
  useEffect(() => {
    loadFilters();
  }, []);

  // Recharger les rapports quand les paramètres changent
  useEffect(() => {
    refresh();
  }, [currentPage, filterUser, filterType, dateRange]);

  // Debounce pour la recherche
  useEffect(() => {
    const timer = setTimeout(() => {
      setCurrentPage(0); // Retour à la première page lors d'une recherche
      refresh();
    }, 350);
    return () => clearTimeout(timer);
  }, [search]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      if (withDesign) {
        await generateEnhancedReport(genRange.start, genRange.end);
      } else {
        await generateReport(genRange.start, genRange.end);
      }
      toast.success(t('reports.reportGenerated'));
      setCurrentPage(0); // Retour à la première page
      refresh();
    } catch (err) {
      toast.error(t('reports.reportGenerationFailed') + " " + (err.response?.data?.error || ''));
    } finally {
      setGenerating(false);
    }
  };

  const handleFile = async (file) => {
    if (!file) return;
    setImportState({ status: 'loading', message: `${t('reports.importing')} ${file.name}...` });
    try {
      const result = await importExcel(file);
      const hasErrors = result?.errors?.length > 0;
      setImportState({
        status: hasErrors ? 'error' : 'success',
        message: hasErrors
          ? `${result.recordsImported} ${t('reports.recordsImported')}, ${result.errors.length} erreur(s) : ${result.errors[0]}`
          : `${result.recordsImported} ${t('reports.recordsImported')} depuis ${result.filename}.`,
      });
      toast.success(`${t('reports.importSuccess')} ${result.recordsImported} ${t('reports.recordsImported')}`);
      setCurrentPage(0); // Retour à la première page
      refresh();
      loadFilters(); // Recharger les filtres car de nouveaux rapports peuvent avoir été créés
    } catch (err) {
      setImportState({
        status: 'error',
        message: err.response?.data?.error || "Le fichier est invalide ou une colonne requise est manquante.",
      });
      toast.error(t('reports.importFailed'));
    }
  };

  const applyPreset = (preset) => {
    const range = preset.getValue();
    setDateRange(range);
    setCurrentPage(0); // Retour à la première page
  };

  return (
    <Layout>
      <PageHeader eyebrow={t('reports.eyebrow')} title={t('reports.title')} />

      <div className="p-8 space-y-6">
        {canManage && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div className="panel p-5">
              <div className="eyebrow mb-3">{t('reports.importExcelTitle')}</div>
              <div
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => {
                  e.preventDefault();
                  handleFile(e.dataTransfer.files?.[0]);
                }}
                onClick={() => fileInputRef.current?.click()}
                className="border border-dashed border-border rounded-lg py-8 flex flex-col items-center justify-center gap-2 cursor-pointer hover:border-amber/60 transition-colors"
              >
                <Upload size={20} className="text-text-faint" />
                <div className="text-sm text-text-muted">
                  {t('reports.uploadHint')}
                </div>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".xlsx"
                  className="hidden"
                  onChange={(e) => handleFile(e.target.files?.[0])}
                />
              </div>
              {importState.status !== 'idle' && (
                <div
                  className={`mt-3 flex items-start gap-2 text-sm rounded-lg px-3 py-2.5 border ${
                    importState.status === 'error'
                      ? 'text-alert bg-alert/10 border-alert/30'
                      : importState.status === 'success'
                      ? 'text-teal bg-teal/10 border-teal/30'
                      : 'text-text-muted bg-surface-2 border-border'
                  }`}
                >
                  {importState.status === 'loading' && <Loader2 size={15} className="mt-0.5 animate-spin shrink-0" />}
                  {importState.status === 'success' && <CheckCircle2 size={15} className="mt-0.5 shrink-0" />}
                  {importState.status === 'error' && <XCircle size={15} className="mt-0.5 shrink-0" />}
                  <span>{importState.message}</span>
                </div>
              )}
            </div>

            <div className="panel p-5">
              <div className="eyebrow mb-3">{t('reports.generateReportTitle')}</div>
              <div className="flex items-center gap-2 mb-4">
                <input
                  type="date"
                  value={genRange.start}
                  onChange={(e) => setGenRange((r) => ({ ...r, start: e.target.value }))}
                  className="input-field"
                />
                <span className="text-text-faint text-sm">→</span>
                <input
                  type="date"
                  value={genRange.end}
                  onChange={(e) => setGenRange((r) => ({ ...r, end: e.target.value }))}
                  className="input-field"
                />
              </div>
              <label className="flex items-center gap-2 mb-4 text-sm text-text-muted cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={withDesign}
                  onChange={(e) => setWithDesign(e.target.checked)}
                  className="accent-amber"
                />
                {t('reports.withDesign')}
              </label>
              <button onClick={handleGenerate} disabled={generating} className="btn-primary w-full">
                {generating ? <Loader2 size={16} className="animate-spin" /> : <FileDown size={16} />}
                {generating ? t('reports.generating') : t('reports.generateButton')}
              </button>
            </div>
          </div>
        )}

        {/* Filtres avancés */}
        <div className="panel p-5">
          <div className="eyebrow mb-3">{t('reports.filters')}</div>
          
          {/* Presets rapides */}
          <div className="flex flex-wrap gap-2 mb-4">
            {FILTERS_PRESETS.map((preset) => (
              <button
                key={preset.label_key}
                onClick={() => applyPreset(preset)}
                className="btn-secondary text-xs"
              >
                {t(preset.label_key)}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div className="relative">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-faint" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder={t('reports.search')}
                className="input-field pl-9 !py-2"
              />
            </div>

            <select
              value={filterUser}
              onChange={(e) => {
                setFilterUser(e.target.value);
                setCurrentPage(0); // Retour à la première page
              }}
              className="input-field !py-2"
            >
              <option value="all">{t('reports.allUsers')}</option>
              {filterUsers.map((u) => (
                <option key={u} value={u}>{u}</option>
              ))}
            </select>

            <select
              value={filterType}
              onChange={(e) => {
                setFilterType(e.target.value);
                setCurrentPage(0); // Retour à la première page
              }}
              className="input-field !py-2"
            >
              <option value="all">{t('reports.allTypes')}</option>
              {filterTypes.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>

            <div className="flex gap-2">
              <input
                type="date"
                value={dateRange.start}
                onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
                className="input-field !py-2"
                placeholder="Date début"
              />
              <input
                type="date"
                value={dateRange.end}
                onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
                className="input-field !py-2"
                placeholder="Date fin"
              />
            </div>
          </div>
        </div>

        <div className="panel">
          <div className="p-5 border-b border-border flex items-center justify-between gap-4">
            <div className="eyebrow">
              {reportsData.totalElements} {t('reports.reportsFound')}
            </div>
            {reportsData.totalElements > 0 && (
              <div className="text-sm text-text-muted">
                {t('reports.page')} {reportsData.currentPage + 1} {t('reports.of')} {reportsData.totalPages || 1}
              </div>
            )}
          </div>

          <div className="overflow-x-auto">
            <table className="table-industrial w-full">
              <thead>
                <tr>
                  <th>{t('reports.title_col')}</th>
                  <th>{t('reports.type')}</th>
                  <th>{t('reports.period')}</th>
                  <th>{t('reports.production')}</th>
                  <th>{t('reports.quality')}</th>
                  <th>{t('reports.generatedBy')}</th>
                  <th>{t('reports.date')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={8} className="text-center text-text-muted py-8">
                      <Loader2 className="animate-spin inline-block mr-2" size={16} />
                      {t('common.loading')}
                    </td>
                  </tr>
                )}
                {!loading && reportsData.content.length === 0 && (
                  <tr>
                    <td colSpan={8} className="text-center text-text-muted py-8">
                      {t('reports.noReports')}
                    </td>
                  </tr>
                )}
                {displayReports.map((r) => (
                  <tr key={r.id}>
                    <td className="font-medium">{r.title}</td>
                    <td>
                      <span className="font-mono text-[11px] uppercase tracking-wide text-text-muted">{r.type}</span>
                    </td>
                    <td className="font-mono text-xs text-text-muted">
                      {r.periodStart} → {r.periodEnd}
                    </td>
                    <td className="font-mono">{r.totalProduction ?? '—'}</td>
                    <td className="font-mono text-teal">{r.qualityRate != null ? `${r.qualityRate}%` : '—'}</td>
                    <td className="text-text-muted">{r.generatedByEmail}</td>
                    <td className="font-mono text-xs text-text-muted">{formatDate(r.generatedAt)}</td>
                    <td>
                      <div className="flex items-center gap-1.5 justify-end">
                        <button
                          title={t('reports.sendEmail')}
                          onClick={() => {
                            setEmailVerificationModal({
                              isOpen: true,
                              reportId: r.id,
                              reportTitle: r.title,
                            });
                          }}
                          className="p-2 rounded-lg hover:bg-surface-3 hover:text-blue-500 text-text-muted transition-colors"
                        >
                          <Send size={15} />
                        </button>
                        <button
                          title={t('reports.downloadPDF')}
                          onClick={() => {
                            downloadReport(r.id, 'pdf', `${r.title}.pdf`);
                            toast.success(t('reports.pdfDownloaded'));
                          }}
                          className="p-2 rounded-lg hover:bg-surface-3 hover:text-amber text-text-muted transition-colors"
                        >
                          <FileDown size={15} />
                        </button>
                        <button
                          title={t('reports.downloadExcel')}
                          onClick={() => {
                            downloadReport(r.id, 'excel', `${r.title}.xlsx`);
                            toast.success(t('reports.excelDownloaded'));
                          }}
                          className="p-2 rounded-lg hover:bg-surface-3 hover:text-teal text-text-muted transition-colors"
                        >
                          <FileSpreadsheet size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination serveur */}
          {reportsData.totalPages > 1 && (
            <div className="p-4 border-t border-border flex items-center justify-between">
              <div className="text-sm text-text-muted">
                {t('reports.showing')} {reportsData.content.length} {t('reports.on')} {reportsData.totalElements}
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setCurrentPage(0)}
                  disabled={currentPage === 0}
                  className="btn-secondary text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  title={t('reports.firstPage')}
                >
                  <ChevronLeft size={14} />
                  <ChevronLeft size={14} className="-ml-2" />
                </button>
                <button
                  onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                  disabled={currentPage === 0}
                  className="btn-secondary text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronLeft size={14} />
                  {t('reports.previous')}
                </button>
                
                {/* Numéros de pages */}
                <div className="flex gap-1">
                  {Array.from({ length: Math.min(5, reportsData.totalPages) }, (_, i) => {
                    let pageNum;
                    if (reportsData.totalPages <= 5) {
                      pageNum = i;
                    } else if (currentPage < 3) {
                      pageNum = i;
                    } else if (currentPage > reportsData.totalPages - 4) {
                      pageNum = reportsData.totalPages - 5 + i;
                    } else {
                      pageNum = currentPage - 2 + i;
                    }
                    
                    return (
                      <button
                        key={pageNum}
                        onClick={() => setCurrentPage(pageNum)}
                        className={`px-3 py-1 rounded text-sm ${
                          currentPage === pageNum
                            ? 'bg-amber text-white font-medium'
                            : 'hover:bg-surface-2 text-text-muted'
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  })}
                </div>

                <button
                  onClick={() => setCurrentPage(p => Math.min(reportsData.totalPages - 1, p + 1))}
                  disabled={currentPage === reportsData.totalPages - 1}
                  className="btn-secondary text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {t('reports.next')}
                  <ChevronRight size={14} />
                </button>
                <button
                  onClick={() => setCurrentPage(reportsData.totalPages - 1)}
                  disabled={currentPage === reportsData.totalPages - 1}
                  className="btn-secondary text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  title={t('reports.lastPage')}
                >
                  <ChevronRight size={14} />
                  <ChevronRight size={14} className="-ml-2" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Modal de vérification email */}
      <EmailVerificationModal
        isOpen={emailVerificationModal.isOpen}
        onClose={() => setEmailVerificationModal({ isOpen: false, reportId: null, reportTitle: '' })}
        reportId={emailVerificationModal.reportId}
        reportTitle={emailVerificationModal.reportTitle}
      />
    </Layout>
  );
}

function formatDate(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
}
