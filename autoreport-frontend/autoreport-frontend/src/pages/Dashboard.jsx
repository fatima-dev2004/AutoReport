import React, { useEffect, useMemo, useState, useRef } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  LineChart,
  Line,
} from 'recharts';
import { Download, FileSpreadsheet, FileText, Loader2, AlertTriangle, Database } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import KpiCard from '../components/KpiCard';
import { getKpis } from '../api/dashboard';
import { useToast } from '../components/Toast';
import { useLanguage } from '../context/LanguageContext';
import { generateDashboardPDF, addChartToPDF, savePDF } from '../utils/pdfGenerator';
import { generateDashboardExcel } from '../utils/excelGenerator';

const todayISO = () => new Date().toISOString().slice(0, 10);
const daysAgoISO = (n) => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
};

export default function Dashboard() {
  const toast = useToast();
  const { t } = useLanguage();
  const [range, setRange] = useState({ start: daysAgoISO(30), end: todayISO() });
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState(false);
  const [downloadingExcel, setDownloadingExcel] = useState(false);

  // Refs pour capturer les graphiques
  const dailyChartRef = useRef(null);
  const lineChartRef = useRef(null);

  const loadDemoData = () => {
    const demoDaily = {
      '2026-07-01': 4200,
      '2026-07-02': 4600,
      '2026-07-03': 4800,
      '2026-07-04': 5100,
      '2026-07-05': 4700,
      '2026-07-06': 5250,
      '2026-07-07': 5600,
      '2026-07-08': 5900,
      '2026-07-09': 5350,
    };

    const demoData = {
      totalProduction: 53000,
      totalDefects: 620,
      qualityRate: 97.8,
      defectRate: 2.2,
      dailyProduction: demoDaily,
      productionByLine: {
        'Ligne A': 18000,
        'Ligne B': 16500,
        'Ligne C': 18500,
      },
      anomalies: [
        { day: '2026-07-06', line: 'Ligne B', reason: 'Baisse qualité détectée', severity: 'high' },
        { day: '2026-07-08', line: 'Ligne C', reason: 'Pic de défauts sur la ligne', severity: 'mid' },
      ],
    };

    setData(demoData);
    setLoading(false);
    setError('');
    toast.success(t('dashboard.demoData'));
  };

  const handleDownloadPDF = async () => {
    if (!data) {
      toast.error(t('dashboard.exportError'));
      return;
    }

    setDownloading(true);
    toast.success(t('dashboard.generatingPDF'));

    try {
      // Générer le PDF avec design professionnel
      const doc = await generateDashboardPDF(data, range);

      // Ajouter une nouvelle page pour les graphiques
      doc.addPage();
      let yPos = 20;

      // En-tête
      doc.setFontSize(18);
      doc.setTextColor(15, 23, 42);
      doc.setFont('helvetica', 'bold');
      doc.text('Graphiques de production', 20, yPos);
      yPos += 15;

      // Capturer et ajouter le graphique de production journalière
      if (dailyChartRef.current) {
        yPos = await addChartToPDF(doc, dailyChartRef.current, t('dashboard.dailyProduction'), yPos);
      }

      // Vérifier si on a besoin d'une nouvelle page
      if (yPos > 200) {
        doc.addPage();
        yPos = 20;
      }

      // Capturer et ajouter le graphique de production par ligne
      if (lineChartRef.current) {
        await addChartToPDF(doc, lineChartRef.current, t('dashboard.productionByLine'), yPos);
      }

      // Sauvegarder le PDF
      const filename = `Rapport_Dashboard_${range.start}_${range.end}.pdf`;
      savePDF(doc, filename);

      toast.success(t('dashboard.pdfSuccess'));
    } catch (err) {
      console.error('Erreur génération PDF:', err);
      toast.error(t('dashboard.pdfError'));
    } finally {
      setDownloading(false);
    }
  };

  const handleDownloadExcel = () => {
    if (!data) {
      toast.error(t('dashboard.exportError'));
      return;
    }

    setDownloadingExcel(true);
    toast.success(t('dashboard.generatingExcel'));

    try {
      const filename = generateDashboardExcel(data, range);
      toast.success(`${t('dashboard.excelSuccess')} : ${filename}`);
    } catch (err) {
      console.error('Erreur génération Excel:', err);
      toast.error(t('dashboard.excelError'));
    } finally {
      setDownloadingExcel(false);
    }
  };

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getKpis(range.start, range.end)
      .then((res) => !cancelled && setData(res))
      .catch(() => !cancelled && setError("Impossible de charger les indicateurs. Vérifiez que le backend est démarré."))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [range.start, range.end]);

  const dailySeries = useMemo(() => {
    if (!data?.dailyProduction) return [];
    return Object.entries(data.dailyProduction).map(([date, qty]) => ({ date: date.slice(5), qty }));
  }, [data]);

  const lineSeries = useMemo(() => {
    if (!data?.productionByLine) return [];
    return Object.entries(data.productionByLine).map(([line, qty]) => ({ line, qty }));
  }, [data]);

  const projectionSeries = useMemo(() => {
    if (!dailySeries.length) return [];
    const base = dailySeries.slice(-7);
    const lastValue = base[base.length - 1]?.qty || 0;
    return [
      ...base,
      ...Array.from({ length: 5 }, (_, index) => ({
        date: `+${index + 1}`,
        qty: Math.round(lastValue * (1 + (index + 1) * 0.015)),
        projected: true,
      })),
    ];
  }, [dailySeries]);

  return (
    <Layout>
      <PageHeader
        eyebrow={t('dashboard.eyebrow')}
        title={t('dashboard.title')}
        action={
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 font-mono text-xs">
              <input
                type="date"
                value={range.start}
                onChange={(e) => setRange((r) => ({ ...r, start: e.target.value }))}
                className="input-field !py-1.5 !w-auto"
              />
              <span className="text-text-faint">→</span>
              <input
                type="date"
                value={range.end}
                onChange={(e) => setRange((r) => ({ ...r, end: e.target.value }))}
                className="input-field !py-1.5 !w-auto"
              />
            </div>
            
            {/* Boutons de téléchargement */}
            <div className="flex items-center gap-2">
              <button
                onClick={handleDownloadExcel}
                disabled={downloadingExcel || loading}
                className="btn-secondary !py-1.5 flex items-center gap-2 disabled:opacity-60"
                title={t('dashboard.downloadExcel')}
              >
                {downloadingExcel ? (
                  <Loader2 size={16} className="animate-spin" />
                ) : (
                  <FileSpreadsheet size={16} />
                )}
                <span className="hidden sm:inline">{t('dashboard.downloadExcel')}</span>
              </button>
              
              <button
                onClick={handleDownloadPDF}
                disabled={downloading || loading}
                className="btn-primary !py-1.5 flex items-center gap-2 disabled:opacity-60"
                title={t('dashboard.downloadPDF')}
              >
                {downloading ? (
                  <Loader2 size={16} className="animate-spin" />
                ) : (
                  <FileText size={16} />
                )}
                <span className="hidden sm:inline">{t('dashboard.downloadPDF')}</span>
              </button>
            </div>
          </div>
        }
      />

      <div className="p-8 space-y-6">
        {error && (
          <div className="text-sm text-alert bg-alert/10 border border-alert/30 rounded-lg px-4 py-3">{error}</div>
        )}

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard
            eyebrow={t('dashboard.totalProduction')}
            value={loading ? '—' : formatNumber(data?.totalProduction)}
            unit={t('dashboard.units')}
            accent="amber"
          />
          <KpiCard
            eyebrow={t('dashboard.totalDefects')}
            value={loading ? '—' : formatNumber(data?.totalDefects)}
            unit={t('dashboard.units')}
            accent="alert"
          />
          <KpiCard
            eyebrow={t('dashboard.qualityRate')}
            value={loading ? '—' : `${data?.qualityRate ?? 0}`}
            unit="%"
            percent={data?.qualityRate}
            accent="teal"
          />
          <KpiCard
            eyebrow={t('dashboard.defectRate')}
            value={loading ? '—' : `${data?.defectRate ?? 0}`}
            unit="%"
            percent={data?.defectRate}
            accent="alert"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-4">
          <div className="panel p-5 lg:col-span-3" ref={dailyChartRef}>
            <div className="eyebrow mb-4">{t('dashboard.dailyProduction')}</div>
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={dailySeries}>
                <defs>
                  <linearGradient id="fillAmber" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#2563EB" stopOpacity={0.4} />
                    <stop offset="100%" stopColor="#2563EB" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke="#E2E8F0" strokeDasharray="2 4" vertical={false} />
                <XAxis dataKey="date" stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} />
                <YAxis stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} width={40} />
                <Tooltip content={<ChartTooltip suffix={` ${t('dashboard.units')}`} />} />
                <Area type="monotone" dataKey="qty" stroke="#2563EB" strokeWidth={2} fill="url(#fillAmber)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>

          <div className="panel p-5 lg:col-span-2" ref={lineChartRef}>
            <div className="eyebrow mb-4">{t('dashboard.productionByLine')}</div>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={lineSeries}>
                <CartesianGrid stroke="#E2E8F0" strokeDasharray="2 4" vertical={false} />
                <XAxis dataKey="line" stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} />
                <YAxis stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} width={40} />
                <Tooltip content={<ChartTooltip suffix={` ${t('dashboard.units')}`} />} />
                <Bar dataKey="qty" fill="#0D9488" radius={[2, 2, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {!loading && (
          <div className="grid grid-cols-1 xl:grid-cols-[1.2fr_0.8fr] gap-4">
            <div className="panel p-5">
              <div className="eyebrow mb-4">{t('dashboard.projection')}</div>
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={projectionSeries}>
                  <CartesianGrid stroke="#E2E8F0" strokeDasharray="2 4" vertical={false} />
                  <XAxis dataKey="date" stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} />
                  <YAxis stroke="#94A3B8" fontSize={11} fontFamily="JetBrains Mono" tickLine={false} axisLine={false} width={40} />
                  <Tooltip content={<ChartTooltip suffix={` ${t('dashboard.units')}`} />} />
                  <Line type="monotone" dataKey="qty" stroke="#F59E0B" strokeWidth={2} dot={{ r: 3 }} />
                  <Line type="monotone" dataKey="qty" stroke="#EF4444" strokeWidth={2} strokeDasharray="5 5" data={projectionSeries.filter((item) => item.projected)} />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="panel p-5">
              <div className="eyebrow mb-4">{t('dashboard.anomalies')}</div>
              <div className="space-y-3">
                {(data?.anomalies || []).length > 0 ? (
                  (data?.anomalies || []).map((anomaly, index) => (
                    <div key={index} className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-3">
                      <AlertTriangle size={16} className="text-red-500 mt-0.5" />
                      <div>
                        <div className="text-sm font-medium text-red-700">{anomaly.line}</div>
                        <div className="text-xs text-red-600">{anomaly.day}</div>
                        <div className="text-sm text-text-muted mt-1">{anomaly.reason}</div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-sm text-text-muted">Aucune anomalie détectée pour cette période.</div>
                )}
              </div>
            </div>
          </div>
        )}

        {!loading && dailySeries.length === 0 && (
          <div className="panel p-8 text-center">
            <div className="eyebrow mb-2">{t('dashboard.noData')}</div>
            <p className="text-sm text-text-muted">
              {t('dashboard.noDataMessage')}
            </p>
          </div>
        )}
      </div>
    </Layout>
  );
}

function ChartTooltip({ active, payload, label, suffix = '' }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-surface-2 border border-border rounded-lg px-3 py-2 text-xs font-mono">
      <div className="text-text-faint mb-1">{label}</div>
      <div className="text-amber font-semibold">{formatNumber(payload[0].value)}{suffix}</div>
    </div>
  );
}

function formatNumber(n) {
  if (n == null) return '0';
  return new Intl.NumberFormat('fr-FR').format(n);
}
