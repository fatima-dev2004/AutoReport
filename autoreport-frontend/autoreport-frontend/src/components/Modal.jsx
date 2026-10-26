import React, { useEffect } from 'react';
import { X } from 'lucide-react';

export default function Modal({ isOpen, onClose, title, children, size = 'md' }) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const sizes = {
    sm: 'max-w-md',
    md: 'max-w-2xl',
    lg: 'max-w-4xl',
    xl: 'max-w-6xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      />
      <div
        className={`relative bg-white rounded-xl shadow-2xl w-full ${sizes[size]} max-h-[90vh] overflow-hidden flex flex-col animate-fade-in`}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="font-display font-bold text-xl text-text">{title}</h2>
          <button
            onClick={onClose}
            className="p-2 rounded-lg hover:bg-surface-2 text-text-muted hover:text-text transition-colors"
          >
            <X size={20} />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}

export function ModalFooter({ children, className = '' }) {
  return (
    <div className={`px-6 py-4 border-t border-border flex items-center justify-end gap-3 ${className}`}>
      {children}
    </div>
  );
}
