import React, { useState, useEffect } from 'react';
import {
  History,
  Search,
  Filter,
  Download,
  LogIn,
  FileText,
  Upload,
  Mail,
  User,
  Calendar,
  Clock,
} from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import Loader from '../components/Loader';
import { getAuditLogs, getAuditStats, exportAuditLogsCsv } from '../api/auditlog';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';

export default function AuditLog() {
  const { t } = useLanguage();
  const toast = useToast();
  const [logs, setLogs] = useState([]);
  const [filteredLogs, setFilteredLogs] = useState([]);
  const [stats, setStats] = useState({});
  const [search, setSearch] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [filterUser, setFilterUser] = useState('all');
  const [dateRange, setDateRange] = useState({
    start: '',
    end: '',
  });
  const [loading, setLoading] = useState(true);

  // Charger les logs depuis l'API
  useEffect(() => {
    loadLogs();
    loadStats();
  }, []);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const response = await getAuditLogs({
        type: filterType !== 'all' ? filterType : undefined,
        user: filterUser !== 'all' ? filterUser : undefined,
        startDate: dateRange.start || undefined,
        endDate: dateRange.end || undefined,
        search: search || undefined,
      });
      setLogs(response.data);
      setFilteredLogs(response.data);
    } catch (err) {
      console.error('Error loading audit logs:', err);
      // Fallback sur données mock si backend pas prêt
      const mockData = generateMockLogs();
      setLogs(mockData);
      setFilteredLogs(mockData);
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await getAuditStats();
      setStats(response.data);
    } catch (err) {
      console.error('Error loading stats:', err);
    }
  };
  // Fonction de génération mock (fallback si backend pas prêt)
  const generateMockLogs = () => {
    const types = [
      { value: 'login', label: t('audit.typeLogin'), icon: LogIn, color: 'text-blue-600' },
      { value: 'report', label: t('audit.typeReport'), icon: FileText, color: 'text-green-600' },
      { value: 'import', label: t('audit.typeImport'), icon: Upload, color: 'text-purple-600' },
      { value: 'email', label: t('audit.typeEmail'), icon: Mail, color: 'text-orange-600' },
      { value: 'download', label: t('audit.typeDownload'), icon: Download, color: 'text-teal-600' },
    ];

    const users = ['admin@sews.com', 'manager@sews.com', 'employe@sews.com'];

    const mockData = Array.from({ length: 50 }, (_, i) => {
      const type = types[Math.floor(Math.random() * types.length)];
      const user = users[Math.floor(Math.random() * users.length)];
      const date = new Date();
      date.setHours(date.getHours() - Math.floor(Math.random() * 168));

      const details = {
        login: `${t('audit.loggedInFrom')} ${['Chrome', 'Firefox', 'Edge'][Math.floor(Math.random() * 3)]}`,
        report: `${t('audit.reportGeneratedFor')} ${date.toLocaleDateString()}`,
        import: `${t('audit.importedLines')} ${Math.floor(Math.random() * 500) + 50}`,
        email: `${t('audit.emailSentTo')} ${Math.floor(Math.random() * 5) + 1} ${t('audit.recipientCount')}`,
        download: `${t('audit.downloadType')} ${['PDF', 'Excel'][Math.floor(Math.random() * 2)]}`,
      };

      return {
        id: i + 1,
        type: type.value,
        typeLabel: type.label,
        icon: type.icon,
        iconColor: type.color,
        user,
        userName: user.split('@')[0],
        timestamp: date.toISOString(),
        details: details[type.value],
        ipAddress: `192.168.1.${Math.floor(Math.random() * 255)}`,
      };
    });

    return mockData.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
  };

  // Appliquer les filtres localement
  useEffect(() => {
    loadLogs();
  }, [search, filterType, filterUser, dateRange]);

  const exportLogs = async () => {
    try {
      toast.info(t('audit.exporting'));
      const response = await exportAuditLogsCsv({
        type: filterType !== 'all' ? filterType : undefined,
        user: filterUser !== 'all' ? filterUser : undefined,
        startDate: dateRange.start || undefined,
        endDate: dateRange.end || undefined,
      });

      // Créer un lien de téléchargement
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `audit-log-${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      
      toast.success(t('audit.exportSuccess'));
    } catch (err) {
      // Fallback sur export local si backend pas prêt
      const csv = [
        ['Date', 'Heure', 'Type', 'Utilisateur', 'Détails', 'IP'],
        ...filteredLogs.map((log) => {
          const date = new Date(log.timestamp);
          return [
            date.toLocaleDateString('fr-FR'),
            date.toLocaleTimeString('fr-FR'),
            log.typeLabel,
            log.user,
            log.details,
            log.ipAddress,
          ];
        }),
      ]
        .map((row) => row.join(';'))
        .join('\n');

      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit-log-${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      
      toast.success(t('audit.exportSuccessLocal'));
    }
  };

  const uniqueUsers = [...new Set(logs.map((log) => log.user))];

  return (
    <Layout>
      <PageHeader
        eyebrow={t('audit.eyebrow')}
        title={t('audit.title')}
        action={
          <button onClick={exportLogs} className="btn-primary">
            <Download size={16} />
            {t('common.export')}
          </button>
        }
      />

      <div className="p-8 space-y-6">
        {/* Filtres */}
        <div className="panel p-5">
          <div className="flex flex-wrap gap-4">
            {/* Recherche */}
            <div className="flex-1 min-w-64">
              <div className="relative">
                <Search
                  size={15}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-text-faint"
                />
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder={t('audit.searchPlaceholder')}
                  className="input-field pl-9 !py-2"
                />
              </div>
            </div>

            {/* Filtre par type */}
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="input-field !py-2 w-48"
            >
              <option value="all">{t('audit.allTypes')}</option>
              <option value="login">{t('audit.typeLogin')}</option>
              <option value="report">{t('audit.typeReport')}</option>
              <option value="import">{t('audit.typeImport')}</option>
              <option value="email">{t('audit.typeEmail')}</option>
              <option value="download">{t('audit.typeDownload')}</option>
            </select>

            {/* Filtre par utilisateur */}
            <select
              value={filterUser}
              onChange={(e) => setFilterUser(e.target.value)}
              className="input-field !py-2 w-48"
            >
              <option value="all">{t('audit.filterUser')}</option>
              {uniqueUsers.map((user) => (
                <option key={user} value={user}>
                  {user}
                </option>
              ))}
            </select>

            {/* Filtre par date */}
            <input
              type="date"
              value={dateRange.start}
              onChange={(e) =>
                setDateRange({ ...dateRange, start: e.target.value })
              }
              className="input-field !py-2 w-40"
            />
            <span className="text-text-faint self-center">→</span>
            <input
              type="date"
              value={dateRange.end}
              onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
              className="input-field !py-2 w-40"
            />
          </div>
        </div>

        {/* Statistiques rapides */}
        <div className="grid grid-cols-5 gap-4">
          <div className="panel p-4 text-center">
            <div className="text-2xl font-display font-bold text-blue-600 mb-1">
              {stats.login || logs.filter((l) => l.type === 'login').length}
            </div>
            <div className="text-xs text-text-muted">Connexions</div>
          </div>
          <div className="panel p-4 text-center">
            <div className="text-2xl font-display font-bold text-green-600 mb-1">
              {stats.report || logs.filter((l) => l.type === 'report').length}
            </div>
            <div className="text-xs text-text-muted">Rapports</div>
          </div>
          <div className="panel p-4 text-center">
            <div className="text-2xl font-display font-bold text-purple-600 mb-1">
              {stats.import || logs.filter((l) => l.type === 'import').length}
            </div>
            <div className="text-xs text-text-muted">Imports</div>
          </div>
          <div className="panel p-4 text-center">
            <div className="text-2xl font-display font-bold text-orange-600 mb-1">
              {stats.email || logs.filter((l) => l.type === 'email').length}
            </div>
            <div className="text-xs text-text-muted">Emails</div>
          </div>
          <div className="panel p-4 text-center">
            <div className="text-2xl font-display font-bold text-teal-600 mb-1">
              {stats.download || logs.filter((l) => l.type === 'download').length}
            </div>
            <div className="text-xs text-text-muted">Téléchargements</div>
          </div>
        </div>

        {/* Liste des logs */}
        <div className="panel">
          <div className="p-5 border-b border-border">
            <div className="eyebrow">
              {filteredLogs.length} {t('audit.eventsFound')}
            </div>
          </div>

          <div className="divide-y divide-border">
            {loading ? (
              <div className="p-8 text-center text-text-muted">Chargement...</div>
            ) : filteredLogs.length === 0 ? (
              <div className="p-8 text-center text-text-muted">
                {t('audit.noEventsFound')}
              </div>
            ) : (
              filteredLogs.slice(0, 100).map((log) => {
                const Icon = log.icon;
                return (
                  <div
                    key={log.id}
                    className="p-4 hover:bg-surface-2/60 transition-colors"
                  >
                    <div className="flex items-start gap-4">
                      <div className="shrink-0">
                        <div className="h-10 w-10 rounded-lg bg-surface-2 flex items-center justify-center">
                          <Icon size={18} className={log.iconColor} />
                        </div>
                      </div>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-4 mb-1">
                          <div>
                            <div className="font-medium text-sm">{log.typeLabel}</div>
                            <div className="text-sm text-text-muted">{log.details}</div>
                          </div>
                          <div className="text-right shrink-0">
                            <div className="flex items-center gap-1.5 text-xs text-text-muted mb-0.5">
                              <Calendar size={12} />
                              {new Date(log.timestamp).toLocaleDateString('fr-FR')}
                            </div>
                            <div className="flex items-center gap-1.5 text-xs text-text-muted">
                              <Clock size={12} />
                              {new Date(log.timestamp).toLocaleTimeString('fr-FR')}
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center gap-4 text-xs text-text-faint">
                          <div className="flex items-center gap-1.5">
                            <User size={12} />
                            <span className="font-mono">{log.user}</span>
                          </div>
                          <div className="flex items-center gap-1.5">
                            <span>IP:</span>
                            <span className="font-mono">{log.ipAddress}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {filteredLogs.length > 100 && (
            <div className="p-4 text-center text-sm text-text-muted border-t border-border">
              Affichage des 100 premiers résultats sur {filteredLogs.length}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
