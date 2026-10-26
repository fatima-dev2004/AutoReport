import * as XLSX from 'xlsx';

/**
 * Génère et télécharge un fichier Excel du rapport Dashboard
 */
export function generateDashboardExcel(data, dateRange) {
  // Créer un nouveau classeur
  const workbook = XLSX.utils.book_new();

  // ============ FEUILLE 1 : INDICATEURS GÉNÉRAUX ============
  const kpiData = [
    ['INDICATEURS GÉNÉRAUX', ''],
    ['', ''],
    ['Production Totale', data.totalProduction || 0],
    ['Défauts Constatés', data.totalDefects || 0],
    ['Taux de Qualité', `${data.qualityRate || 0}%`],
    ['Taux de Défaut', `${data.defectRate || 0}%`],
    ['', ''],
    ['Période analysée', ''],
    ['Date début', new Date(dateRange.start).toLocaleDateString('fr-FR')],
    ['Date fin', new Date(dateRange.end).toLocaleDateString('fr-FR')],
  ];

  const kpiSheet = XLSX.utils.aoa_to_sheet(kpiData);
  
  // Style de la feuille KPI
  kpiSheet['!cols'] = [{ wch: 25 }, { wch: 20 }];
  
  XLSX.utils.book_append_sheet(workbook, kpiSheet, 'Indicateurs');

  // ============ FEUILLE 2 : PRODUCTION PAR LIGNE ============
  if (data.productionByLine && Object.keys(data.productionByLine).length > 0) {
    const lineHeaders = ['Ligne', 'Production', 'Défauts'];
    const lineData = Object.entries(data.productionByLine).map(([line, value]) => {
      const production = typeof value === 'number' ? value : (value.qty || value.quantity || 0);
      const defects = typeof value === 'object' ? (value.defects || 0) : 0;
      
      return [line, production, defects];
    });

    const productionSheet = XLSX.utils.aoa_to_sheet([
      ['PRODUCTION PAR LIGNE'],
      [],
      lineHeaders,
      ...lineData,
    ]);

    // Largeur des colonnes
    productionSheet['!cols'] = [{ wch: 15 }, { wch: 15 }, { wch: 15 }];

    XLSX.utils.book_append_sheet(workbook, productionSheet, 'Production par Ligne');
  }

  // ============ FEUILLE 3 : PRODUCTION JOURNALIÈRE ============
  if (data.dailyProduction && data.dailyProduction.length > 0) {
    const dailyHeaders = ['Date', 'Production', 'Défauts'];
    const dailyData = data.dailyProduction.map(day => [
      new Date(day.date).toLocaleDateString('fr-FR'),
      day.quantity || 0,
      day.defects || 0,
    ]);

    const dailySheet = XLSX.utils.aoa_to_sheet([
      ['PRODUCTION JOURNALIÈRE'],
      [],
      dailyHeaders,
      ...dailyData,
    ]);

    dailySheet['!cols'] = [{ wch: 12 }, { wch: 15 }, { wch: 15 }];

    XLSX.utils.book_append_sheet(workbook, dailySheet, 'Production Journalière');
  }

  // ============ TÉLÉCHARGEMENT ============
  const fileName = `Rapport_Dashboard_${new Date().toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, fileName);
  
  return fileName;
}

/**
 * Génère un export Excel des rapports
 */
export function generateReportsExcel(reports) {
  const workbook = XLSX.utils.book_new();

  const headers = ['ID', 'Type', 'Date', 'Production', 'Défauts', 'Qualité %', 'Statut'];
  const data = reports.map(report => [
    report.id,
    report.type || 'Production',
    new Date(report.date).toLocaleDateString('fr-FR'),
    report.totalProduction || 0,
    report.totalDefects || 0,
    report.qualityRate || 0,
    report.status || 'Généré',
  ]);

  const sheet = XLSX.utils.aoa_to_sheet([
    ['LISTE DES RAPPORTS'],
    [],
    headers,
    ...data,
  ]);

  sheet['!cols'] = [
    { wch: 8 },
    { wch: 12 },
    { wch: 12 },
    { wch: 12 },
    { wch: 12 },
    { wch: 10 },
    { wch: 12 },
  ];

  XLSX.utils.book_append_sheet(workbook, sheet, 'Rapports');

  const fileName = `Rapports_${new Date().toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, fileName);
  
  return fileName;
}

/**
 * Génère un export Excel des utilisateurs
 */
export function generateUsersExcel(users) {
  const workbook = XLSX.utils.book_new();

  const headers = ['ID', 'Nom', 'Email', 'Rôle', 'Statut', 'Date création'];
  const data = users.map(user => [
    user.id,
    user.name || user.username,
    user.email,
    user.role || 'User',
    user.active ? 'Actif' : 'Inactif',
    new Date(user.createdAt || Date.now()).toLocaleDateString('fr-FR'),
  ]);

  const sheet = XLSX.utils.aoa_to_sheet([
    ['LISTE DES UTILISATEURS'],
    [],
    headers,
    ...data,
  ]);

  sheet['!cols'] = [
    { wch: 8 },
    { wch: 20 },
    { wch: 30 },
    { wch: 12 },
    { wch: 10 },
    { wch: 15 },
  ];

  XLSX.utils.book_append_sheet(workbook, sheet, 'Utilisateurs');

  const fileName = `Utilisateurs_${new Date().toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, fileName);
  
  return fileName;
}

/**
 * Génère un export Excel du journal d'audit
 */
export function generateAuditLogExcel(logs) {
  const workbook = XLSX.utils.book_new();

  const headers = ['ID', 'Action', 'Utilisateur', 'Détails', 'Date'];
  const data = logs.map(log => [
    log.id,
    log.action,
    log.username || log.user || 'Système',
    log.details || '',
    new Date(log.timestamp || log.createdAt).toLocaleString('fr-FR'),
  ]);

  const sheet = XLSX.utils.aoa_to_sheet([
    ['JOURNAL D\'AUDIT'],
    [],
    headers,
    ...data,
  ]);

  sheet['!cols'] = [
    { wch: 8 },
    { wch: 20 },
    { wch: 20 },
    { wch: 40 },
    { wch: 20 },
  ];

  XLSX.utils.book_append_sheet(workbook, sheet, 'Audit Log');

  const fileName = `Journal_Audit_${new Date().toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, fileName);
  
  return fileName;
}
