import apiClient from './client';

// GET /api/dashboard/kpis?start=&end=
export const getKpis = (start, end) =>
  apiClient
    .get('/api/dashboard/kpis', { params: { start, end } })
    .then((res) => res.data);

// POST /api/dashboard/download-report?start=&end=&enhanced=true
// Génère le rapport PDF de la période affichée et déclenche son téléchargement.
export const downloadDashboardReport = async (start, end) => {
  const res = await apiClient.post(
    '/api/dashboard/download-report',
    null,
    { params: { start, end, enhanced: true }, responseType: 'blob' }
  );

  // Récupère le nom de fichier renvoyé par le backend si présent
  const disposition = res.headers['content-disposition'];
  const match = disposition && disposition.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : `rapport-${start}-${end}.pdf`;

  const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
