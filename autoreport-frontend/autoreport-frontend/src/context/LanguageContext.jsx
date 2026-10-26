import React, { createContext, useContext, useState, useEffect } from 'react';
import fr from '../locales/fr.json';
import en from '../locales/en.json';

const translations = { fr, en };

const LanguageContext = createContext();

function getInitialLanguage() {
  try {
    const stored = localStorage.getItem('language');
    if (stored === 'fr' || stored === 'en') {
      return stored;
    }
  } catch (error) {
    // ignore storage access errors
  }

  const browserLanguage = navigator.language || navigator.userLanguage || 'en';
  return browserLanguage.toLowerCase().startsWith('fr') ? 'fr' : 'en';
}

export function LanguageProvider({ children }) {
  const [language, setLanguage] = useState(getInitialLanguage);

  useEffect(() => {
    document.documentElement.lang = language;
    localStorage.setItem('language', language);
  }, [language]);

  const t = (key) => {
    const keys = key.split('.');
    let value = translations[language];
    
    for (const k of keys) {
      value = value?.[k];
    }
    
    return value || key;
  };

  const toggleLanguage = () => {
    setLanguage((prev) => (prev === 'fr' ? 'en' : 'fr'));
  };

  const value = {
    language,
    setLanguage,
    toggleLanguage,
    t,
    isEnglish: language === 'en',
    isFrench: language === 'fr',
  };

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
