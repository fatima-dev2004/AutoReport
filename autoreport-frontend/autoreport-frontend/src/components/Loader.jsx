import React from 'react';
import { Loader2 } from 'lucide-react';

export default function Loader({ size = 'md', text = 'Chargement...' }) {
  const sizes = {
    sm: 16,
    md: 24,
    lg: 32,
    xl: 48,
  };

  return (
    <div className="flex flex-col items-center justify-center gap-3 py-8">
      <Loader2 size={sizes[size]} className="animate-spin text-blue-600" strokeWidth={2} />
      {text && <p className="text-sm text-text-muted">{text}</p>}
    </div>
  );
}

export function LoaderOverlay({ text = 'Chargement...' }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm">
      <div className="bg-white rounded-xl shadow-2xl px-8 py-6 flex flex-col items-center gap-4">
        <Loader2 size={32} className="animate-spin text-blue-600" strokeWidth={2} />
        <p className="text-sm font-medium text-text">{text}</p>
      </div>
    </div>
  );
}

export function InlineLoader({ text }) {
  return (
    <div className="flex items-center gap-2 text-text-muted">
      <Loader2 size={16} className="animate-spin" />
      {text && <span className="text-sm">{text}</span>}
    </div>
  );
}
