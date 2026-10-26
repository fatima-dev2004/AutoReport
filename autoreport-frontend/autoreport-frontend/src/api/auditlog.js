import { LogIn, FileText, Upload, Mail, Download, Settings as SettingsIcon } from 'lucide-react';
import client from './client';

// Backend : GET/POST /api/audit-logs, GET /api/audit-logs/stats, GET /api/audit-logs/export
const BASE = '/api/audit-logs';

// Correspondance entre la catégorie affichée côté UI et l'enum ActionType du backend
const TYPE_TO_ACTION = {
  login: 'LOGIN',
  report: 'GENERATE_REPORT',
  import: 'IMPORT_EXCEL',
  email: 'SEND_EMAIL',
  download: 'DOWNLOAD_REPORT',
};

const CATEGORY_META = {
  login: { typeLabel: 'Connexion', icon: LogIn, iconColor: 'text-blue-600' },
  report: { typeLabel: 'Rapport généré', icon: FileText, iconColor: 'text-green-600' },
  import: { typeLabel: 'Import Excel', icon: Upload, iconColor: 'text-purple-600' },
  email: { typeLabel: 'Email envoyé', icon: Mail, iconColor: 'text-orange-600' },
  download: { typeLabel: 'Téléchargement', icon: Download, iconColor: 'text-teal-600' },
  other: { typeLabel: 'Système', icon: SettingsIcon, iconColor: 'text-gray-500' },
};

// L'ActionType technique du backend -> catégorie affichée (doit rester alignée avec
// AuditLogService.toCategory côté Java)
const ACTION_TO_CATEGORY = {
  LOGIN: 'login',
  LOGOUT: 'login',
  GENERATE_REPORT: 'report',
  IMPORT_EXCEL: 'import',
  SEND_EMAIL: 'email',
  DOWNLOAD_REPORT: 'download',
};

const toCategory = (actionType) => ACTION_TO_CATEGORY[actionType] || 'other';

const mapLog = (dto) => {
  const category = toCategory(dto.actionType);
  const meta = CATEGORY_META[category];
  return {
    id: dto.id,
    type: category,
    typeLabel: meta.typeLabel,
    icon: meta.icon,
    iconColor: meta.iconColor,
    user: dto.userName || 'Système',
    userName: dto.userName,
    timestamp: dto.timestamp,
    details: dto.details,
    ipAddress: dto.ipAddress,
  };
};

// Récupérer les logs d'audit avec filtres et pagination.
// Le backend retourne une Page Spring ({ content: [...] }), on la met à plat ici
// pour que la page continue de recevoir un simple tableau via response.data.
export const getAuditLogs = async (params = {}) => {
  const query = {
    actionType: params.type && params.type !== 'all' ? TYPE_TO_ACTION[params.type] : undefined,
    startDate: params.startDate ? `${params.startDate}T00:00:00` : undefined,
    endDate: params.endDate ? `${params.endDate}T23:59:59` : undefined,
    page: 0,
    size: 200,
  };
  const res = await client.get(BASE, { params: query });
  const content = res.data?.content || [];
  return { data: content.map(mapLog) };
};

// Statistiques par type (utilisées pour les 5 cartes de résumé)
export const getAuditStats = async () => {
  const res = await client.get(`${BASE}/stats`);
  return { data: res.data };
};

// Export CSV
export const exportAuditLogsCsv = (params = {}) => {
  const query = {
    actionType: params.type && params.type !== 'all' ? TYPE_TO_ACTION[params.type] : undefined,
    startDate: params.startDate ? `${params.startDate}T00:00:00` : undefined,
    endDate: params.endDate ? `${params.endDate}T23:59:59` : undefined,
  };
  return client.get(`${BASE}/export`, {
    params: query,
    responseType: 'blob',
  });
};
