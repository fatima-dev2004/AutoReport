import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutGrid, FileStack, Users, CalendarClock, LogOut, Upload, Settings, History, User, MessageSquare, Target, Menu, X, Globe, Sun, Moon, Info, KeyRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { useTheme } from '../context/ThemeContext';
import sewsLogo from '../assets/sews-logo.png';

const NAV = (t) => [
  { to: '/', label: t('nav.dashboard'), icon: LayoutGrid, roles: ['ADMIN', 'MANAGER', 'USER'] },
  { to: '/reports', label: t('nav.reports'), icon: FileStack, roles: ['ADMIN', 'MANAGER', 'USER'] },
  { to: '/objectives', label: t('nav.objectives'), icon: Target, roles: ['ADMIN', 'MANAGER'] },
  { to: '/import', label: t('nav.import'), icon: Upload, roles: ['ADMIN', 'MANAGER'] },
  { to: '/chat', label: t('nav.chat'), icon: MessageSquare, roles: ['ADMIN', 'MANAGER', 'USER'] },
  { to: '/schedules', label: t('nav.schedules'), icon: CalendarClock, roles: ['ADMIN'] },
  { to: '/users', label: t('nav.users'), icon: Users, roles: ['ADMIN'] },
  { to: '/roles-permissions', label: t('nav.rolesPermissions'), icon: KeyRound, roles: ['ADMIN'] },
  { to: '/audit', label: t('nav.audit'), icon: History, roles: ['ADMIN'] },
  { to: '/settings', label: t('nav.settings'), icon: Settings, roles: ['ADMIN'] },
  { to: '/about', label: t('nav.about'), icon: Info, roles: ['ADMIN', 'MANAGER', 'USER'] },
];

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const { language, toggleLanguage, t } = useLanguage();
  const { theme, toggleTheme, isDark } = useTheme();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const closeMobileMenu = () => {
    setMobileMenuOpen(false);
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row">
      {/* Mobile Header */}
      <div className="lg:hidden bg-gradient-to-r from-[#0B1220] to-[#13224A] border-b border-white/10 sticky top-0 z-50">
        <div className="flex items-center justify-between px-4 h-16">
          <div className="flex items-center gap-2.5">
            <div className="bg-white rounded-lg px-2 py-1.5">
              <img src={sewsLogo} alt="SEWS CABIND MAROC" className="h-4 w-auto" />
            </div>
            <div className="leading-tight">
              <div className="font-display font-bold text-sm tracking-wide leading-none text-white">AUTOREPORT</div>
              <div className="text-[10px] uppercase tracking-[0.22em] text-blue-300/70">System</div>
            </div>
          </div>
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 rounded-lg text-white hover:bg-white/10 transition-colors"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Menu Overlay */}
      {mobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/50 z-40"
          onClick={closeMobileMenu}
        />
      )}

      {/* Sidebar - Desktop & Mobile */}
      <aside
        className={`
          fixed lg:static inset-y-0 left-0 z-40
          w-64 shrink-0 
          bg-gradient-to-b from-[#0B1220] via-[#0F1B33] to-[#13224A] 
          flex flex-col relative overflow-hidden
          transform transition-transform duration-300 ease-in-out
          ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        {/* Effets de fond subtils */}
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute -top-24 -left-24 h-64 w-64 rounded-full bg-blue-500/10 blur-3xl" />
          <div className="absolute bottom-0 -right-16 h-48 w-48 rounded-full bg-cyan-400/5 blur-3xl" />
        </div>

        {/* Logo - Desktop uniquement */}
        <div className="hidden lg:flex h-16 items-center gap-2.5 px-5 border-b border-white/10 relative z-10">
          <div className="bg-white rounded-lg px-2 py-1.5">
            <img src={sewsLogo} alt="SEWS CABIND MAROC" className="h-5 w-auto" />
          </div>
          <div className="leading-tight">
            <div className="font-display font-bold text-base tracking-wide leading-none text-white">AUTOREPORT</div>
            <div className="text-[11px] uppercase tracking-[0.22em] text-blue-300/70 -mt-0.5">System</div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-5 space-y-1 relative z-10 overflow-y-auto">
          {NAV(t).filter((item) => item.roles.includes(user?.role)).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={closeMobileMenu}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-blue-500/20 text-blue-300 border-l-2 border-blue-400 shadow-lg shadow-blue-500/10'
                    : 'text-slate-300 border-l-2 border-transparent hover:text-white hover:bg-white/5'
                }`
              }
            >
              <item.icon size={17} strokeWidth={2} />
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* User Profile & Logout */}
        <div className="p-3 border-t border-white/10 relative z-10 space-y-2">
          {/* Langue et Thème */}
          <div className="flex items-center gap-2 mb-3">
            {/* Bouton Langue */}
            <button
              onClick={toggleLanguage}
              className="flex-1 flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-all duration-200 text-slate-300 hover:text-white"
              title={language === 'fr' ? 'Switch to English' : t('layout.switchToFrench')}
            >
              <Globe size={16} strokeWidth={2} />
              <span className="text-xs font-medium uppercase">{language === 'fr' ? 'FR' : 'EN'}</span>
            </button>

            {/* Bouton Thème */}
            <button
              onClick={toggleTheme}
              className="flex-1 flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-all duration-200 text-slate-300 hover:text-white"
              title={isDark ? t('layout.lightMode') : t('layout.darkMode')}
            >
              {isDark ? <Sun size={16} strokeWidth={2} /> : <Moon size={16} strokeWidth={2} />}
              <span className="text-xs font-medium">{isDark ? 'Light' : 'Dark'}</span>
            </button>
          </div>

          <NavLink
            to="/profile"
            onClick={closeMobileMenu}
            className="block px-3 py-2 mb-2 rounded-lg hover:bg-white/5 transition-colors"
          >
            <div className="flex items-center gap-2.5">
              <div className="h-8 w-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-sm font-display font-bold">
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-sm font-medium truncate text-white">{user?.fullName}</div>
                <div className="text-[11px] uppercase tracking-[0.22em] text-slate-400">{roleLabel(user?.role, t)}</div>
              </div>
            </div>
          </NavLink>
          <button
            onClick={() => {
              closeMobileMenu();
              handleLogout();
            }}
            className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium text-slate-300 hover:text-red-300 hover:bg-white/5 transition-all duration-200"
          >
            <LogOut size={17} strokeWidth={2} />
            {t('nav.logout')}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 min-w-0 lg:mt-0 mt-0">{children}</main>
    </div>
  );
}

function roleLabel(role, t) {
  switch (role) {
    case 'ADMIN':
      return t('users.admin');
    case 'MANAGER':
      return t('users.manager');
    case 'USER':
      return t('users.user');
    default:
      return '';
  }
}
