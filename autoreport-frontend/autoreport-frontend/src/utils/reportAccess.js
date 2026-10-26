const REPORT_ACCESS_KEY = 'autoreport_report_access';
const REPORT_CATALOG_KEY = 'autoreport_report_catalog';

export const DEFAULT_REPORT_CATALOG = [
  { id: 'daily', title: 'Rapport quotidien' },
  { id: 'weekly', title: 'Rapport hebdomadaire' },
  { id: 'line-a', title: 'Production ligne A' },
  { id: 'quality', title: 'Qualité et défauts' },
  { id: 'maintenance', title: 'Suivi maintenance' },
  { id: 'monthly', title: 'Rapport mensuel' },
];

export function readReportAssignments() {
  try {
    const raw = localStorage.getItem(REPORT_ACCESS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

export function writeReportAssignments(assignments) {
  localStorage.setItem(REPORT_ACCESS_KEY, JSON.stringify(assignments));
}

export function readReportCatalog() {
  try {
    const raw = localStorage.getItem(REPORT_CATALOG_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : DEFAULT_REPORT_CATALOG;
  } catch {
    return DEFAULT_REPORT_CATALOG;
  }
}

export function writeReportCatalog(reportList) {
  localStorage.setItem(REPORT_CATALOG_KEY, JSON.stringify(reportList));
}

export function getVisibleReportIdsForUser(email) {
  if (!email) return [];
  const assignments = readReportAssignments();
  const visible = assignments[email] || [];
  return Array.isArray(visible) ? visible.map(String) : [];
}

export function setVisibleReportIdsForUser(email, reportIds) {
  const assignments = readReportAssignments();
  assignments[email] = Array.from(new Set((reportIds || []).map(String)));
  writeReportAssignments(assignments);
}

export function syncReportCatalogFromReports(reports = []) {
  const catalog = reports
    .map((report) => ({
      id: String(report.id ?? report.title),
      title: report.title || report.name || 'Rapport non nommé',
    }))
    .filter((report, index, arr) => arr.findIndex((item) => item.id === report.id) === index);

  if (catalog.length) {
    writeReportCatalog(catalog);
  }

  return catalog.length ? catalog : readReportCatalog();
}
