import React from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '../context/LanguageContext';

export default function NotFound() {
  const { t } = useLanguage();
  
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-3">
      <div className="font-display font-bold text-6xl text-amber">404</div>
      <p className="text-text-muted">{t('notFound.message')}</p>
      <Link to="/" className="btn-secondary mt-2">
        {t('notFound.backHome')}
      </Link>
    </div>
  );
}
