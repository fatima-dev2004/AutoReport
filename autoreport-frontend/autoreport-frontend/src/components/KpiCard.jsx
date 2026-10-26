import React from 'react';

const SEGMENTS = 20;

export default function KpiCard({ eyebrow, value, unit, accent = 'amber', percent, footnote }) {
  const filledCount = percent != null ? Math.round((Math.min(Math.max(percent, 0), 100) / 100) * SEGMENTS) : null;

  const accentClass =
    accent === 'teal' ? 'text-teal' : accent === 'alert' ? 'text-alert' : 'text-amber';
  const segFilledClass =
    accent === 'teal' ? 'bg-teal' : accent === 'alert' ? 'bg-alert' : 'bg-amber';

  return (
    <div className="panel p-5 flex flex-col gap-4">
      <div className="eyebrow">{eyebrow}</div>
      <div className="flex items-baseline gap-1.5">
        <span className={`font-mono font-semibold text-3xl ${accentClass}`}>{value}</span>
        {unit && <span className="text-text-muted text-sm">{unit}</span>}
      </div>

      {percent != null && (
        <div className="rail-segments">
          {Array.from({ length: SEGMENTS }).map((_, i) => (
            <span key={i} className={i < filledCount ? `filled ${segFilledClass}` : ''} />
          ))}
        </div>
      )}

      {footnote && <div className="text-xs text-text-faint">{footnote}</div>}
    </div>
  );
}
