import React from 'react';
import { ArrowRight, Database, FileBarChart2, ServerCog, CalendarClock } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';

export default function About() {
  const steps = [
    { icon: FileBarChart2, title: 'Frontend React', text: 'Interface utilisateur moderne, navigation, tableaux, graphiques, formulaires de gestion.' },
    { icon: ServerCog, title: 'API REST', text: 'Point d’entrée unique pour les données et les opérations métiers exposées au frontend.' },
    { icon: Database, title: 'Spring Boot + MySQL', text: 'Services backend, persistance, sécurisation et logique de génération de rapports.' },
    { icon: CalendarClock, title: 'JasperReports / POI / Scheduler', text: 'PDF, exports Excel, planification des envois et automatisation métier.' },
  ];

  return (
    <Layout>
      <PageHeader eyebrow="Architecture" title="À propos" />

      <div className="p-8 space-y-6 max-w-6xl">
        <div className="panel p-6">
          <div className="grid grid-cols-1 xl:grid-cols-[1.2fr_0.8fr] gap-6 items-center">
            <div>
              <div className="eyebrow mb-3">Vue d’ensemble</div>
              <h2 className="text-3xl font-display font-bold text-text mb-3">AutoReport System</h2>
              <p className="text-text-muted leading-relaxed">
                Le système centralise les données de production, automatise les rapports, et permet à l’administration
                de superviser les accès, les importations et les performances en temps réel.
              </p>
            </div>

            <svg viewBox="0 0 520 280" className="w-full h-auto rounded-2xl border border-border bg-surface-2 p-3">
              <rect x="20" y="30" width="130" height="70" rx="16" fill="#dbeafe" stroke="#60a5fa" />
              <text x="85" y="68" textAnchor="middle" fill="#1e3a8a" fontSize="18" fontWeight="700">React</text>
              <text x="85" y="88" textAnchor="middle" fill="#1e3a8a" fontSize="12">Frontend</text>

              <rect x="195" y="30" width="130" height="70" rx="16" fill="#dcfce7" stroke="#22c55e" />
              <text x="260" y="68" textAnchor="middle" fill="#166534" fontSize="18" fontWeight="700">REST</text>
              <text x="260" y="88" textAnchor="middle" fill="#166534" fontSize="12">API</text>

              <rect x="370" y="30" width="130" height="70" rx="16" fill="#fef3c7" stroke="#f59e0b" />
              <text x="435" y="68" textAnchor="middle" fill="#92400e" fontSize="18" fontWeight="700">Spring</text>
              <text x="435" y="88" textAnchor="middle" fill="#92400e" fontSize="12">Boot</text>

              <rect x="195" y="150" width="130" height="70" rx="16" fill="#f3e8ff" stroke="#a855f7" />
              <text x="260" y="188" textAnchor="middle" fill="#6b21a8" fontSize="18" fontWeight="700">MySQL</text>

              <rect x="370" y="150" width="130" height="70" rx="16" fill="#fee2e2" stroke="#ef4444" />
              <text x="435" y="188" textAnchor="middle" fill="#991b1b" fontSize="16" fontWeight="700">Jasper</text>

              <path d="M150 65 L195 65" stroke="#64748b" strokeWidth="3" markerEnd="url(#arrow)" />
              <path d="M325 65 L370 65" stroke="#64748b" strokeWidth="3" markerEnd="url(#arrow)" />
              <path d="M260 100 L260 150" stroke="#64748b" strokeWidth="3" markerEnd="url(#arrow)" />
              <path d="M435 100 L435 150" stroke="#64748b" strokeWidth="3" markerEnd="url(#arrow)" />
              <path d="M20 220 L500 220" stroke="#64748b" strokeWidth="3" markerEnd="url(#arrow)" /><text x="260" y="245" textAnchor="middle" fill="#475569" fontSize="14">Scheduler / automatisation</text>
              <defs>
                <marker id="arrow" markerWidth="10" markerHeight="10" refX="6" refY="3" orient="auto">
                  <path d="M0,0 L0,6 L6,3 z" fill="#64748b" />
                </marker>
              </defs>
            </svg>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          {steps.map(({ icon: Icon, title, text }) => (
            <div key={title} className="panel p-5">
              <div className="mb-3 h-10 w-10 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center">
                <Icon size={20} />
              </div>
              <div className="font-semibold text-text mb-2">{title}</div>
              <p className="text-sm text-text-muted leading-relaxed">{text}</p>
            </div>
          ))}
        </div>

        <div className="panel p-6">
          <div className="eyebrow mb-3">Flux principal</div>
          <div className="flex flex-wrap items-center gap-3 text-sm text-text-muted">
            <span className="px-3 py-2 rounded-lg bg-surface-2 border border-border">Frontend</span>
            <ArrowRight size={16} />
            <span className="px-3 py-2 rounded-lg bg-surface-2 border border-border">API REST</span>
            <ArrowRight size={16} />
            <span className="px-3 py-2 rounded-lg bg-surface-2 border border-border">Spring Boot</span>
            <ArrowRight size={16} />
            <span className="px-3 py-2 rounded-lg bg-surface-2 border border-border">MySQL</span>
            <ArrowRight size={16} />
            <span className="px-3 py-2 rounded-lg bg-surface-2 border border-border">Jasper / Excel</span>
          </div>
        </div>
      </div>
    </Layout>
  );
}
