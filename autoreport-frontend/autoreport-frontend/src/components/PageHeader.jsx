import React from 'react';

export default function PageHeader({ eyebrow, title, description, action }) {
  return (
    <div className="h-24 border-b border-border bg-surface/60 backdrop-blur sticky top-0 z-10 flex items-center justify-between px-8 md:px-10">
      <div>
        {eyebrow && <div className="eyebrow mb-1.5 tracking-[0.26em] text-[11px]">{eyebrow}</div>}
        <h1 className="font-display font-bold text-3xl md:text-4xl tracking-[-0.03em] leading-none">{title}</h1>
      </div>
      {action}
    </div>
  );
}
