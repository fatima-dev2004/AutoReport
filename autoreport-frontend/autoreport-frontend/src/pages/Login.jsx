import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { AlertTriangle, ShieldCheck, Mail, Lock, ArrowRight, BarChart3, ShieldHalf, Factory, Zap, TrendingUp, Users, Award } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import sewsLogo from '../assets/sews-logo.png';

const QUICK_ACCOUNTS = [
  { label: 'login.accountAdmin', email: 'admin@sews.com', password: 'Admin@123', ready: true },
  { label: 'login.accountManager', email: 'manager@sews.com', password: 'Manager@123', ready: false },
  { label: 'login.accountEmployee', email: 'employe@sews.com', password: 'Employe@123', ready: false },
];

const getFEATURES = (t) => [
  { icon: BarChart3, label: t('login.featureAnalytics') },
  { icon: ShieldHalf, label: t('login.featureSecure') },
  { icon: Factory, label: t('login.featureProduction') },
];

const getSTATS = (t) => [
  { value: '99.9%', label: t('login.statAvailability'), icon: Zap, color: 'from-green-400 to-emerald-500' },
  { value: '2.5x', label: t('login.statTime'), icon: TrendingUp, color: 'from-blue-400 to-cyan-500' },
  { value: '500+', label: t('login.statUsers'), icon: Users, color: 'from-purple-400 to-pink-500' },
];

const getTRUST_BADGES = (t) => [
  { label: 'ISO 9001', sublabel: t('login.certIso') },
  { label: 'RGPD', sublabel: t('login.certRgpd') },
  { label: 'SSL', sublabel: t('login.certSsl') },
];

export default function Login() {
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (err) {
      setError(
        err.response?.status === 401 || err.response?.status === 403
          ? t('login.authError')
          : t('login.serverError')
      );
    } finally {
      setLoading(false);
    }
  };

  const fillAccount = (acc) => {
    setEmail(acc.email);
    setPassword(acc.password);
    setError(acc.ready ? '' : t('login.exampleAccount'));
  };

  const FEATURES = getFEATURES(t);
  const STATS = getSTATS(t);
  const TRUST_BADGES = getTRUST_BADGES(t);

  return (
    <div className="min-h-screen flex bg-white text-slate-900 font-body">
      {/* Panneau gauche — identité de marque */}
      <div className="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-gradient-to-br from-[#0B1220] via-[#0F1B33] to-[#13224A]">
        <GlowShapes />
        <div className="relative z-10 flex flex-col justify-between p-14 w-full text-white">
          <div className="flex items-center gap-2.5">
            <div className="bg-white rounded-md px-2.5 py-1.5">
              <img src={sewsLogo} alt="SEWS CABIND MAROC" className="h-6 w-auto" />
            </div>
            <div className="leading-tight">
              <div className="font-display font-bold text-base tracking-wide">AutoReport</div>
              <div className="text-[11px] uppercase tracking-[0.2em] text-blue-300/70">System</div>
            </div>
          </div>

          <div className="space-y-10">
            <div>
              <h1 className="font-display font-bold text-4xl leading-[1.15] mb-4">
                {t('login.headline')}
                <br />
                <span className="bg-gradient-to-r from-blue-400 to-cyan-300 bg-clip-text text-transparent">
                  {t('login.headlineHighlight')}
                </span>
              </h1>
              <p className="text-slate-300/80 text-[15px] max-w-sm leading-relaxed">
                {t('login.tagline')}
              </p>
            </div>

            {/* Statistiques animées */}
            <div className="grid grid-cols-3 gap-4">
              {STATS.map((stat, idx) => (
                <AnimatedStatCard key={stat.label} stat={stat} delay={idx * 150} />
              ))}
            </div>
          </div>

          <div className="space-y-5">
            {/* Features */}
            <div className="grid grid-cols-3 gap-4">
              {FEATURES.map((f) => (
                <div
                  key={f.label}
                  className="flex flex-col gap-2.5 bg-white/5 border border-white/10 rounded-lg px-3 py-3 backdrop-blur-sm hover:bg-white/8 transition-all duration-300 group"
                >
                  <f.icon size={18} className="text-blue-300 group-hover:scale-110 transition-transform" strokeWidth={1.8} />
                  <span className="text-xs text-slate-300 leading-snug">{f.label}</span>
                </div>
              ))}
            </div>

            {/* Trust Badges */}
            <div className="flex items-center justify-between pt-4 border-t border-white/10">
              <div className="flex items-center gap-2">
                <Award size={16} className="text-blue-300" />
                <span className="text-xs text-slate-400">{t('login.certifications')}</span>
              </div>
              <div className="flex items-center gap-3">
                {TRUST_BADGES.map((badge) => (
                  <div
                    key={badge.label}
                    className="flex flex-col items-center gap-0.5 px-2.5 py-1.5 rounded bg-white/5 border border-white/10"
                  >
                    <span className="text-[10px] font-bold text-white">{badge.label}</span>
                    <span className="text-[8px] text-slate-400">{badge.sublabel}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Panneau droit — formulaire de connexion */}
      <div className="flex-1 flex items-center justify-center px-6 py-12 bg-slate-50">
        <div className="w-full max-w-sm">
          <div className="flex lg:hidden items-center gap-2.5 mb-8">
            <div className="bg-white rounded-md px-2.5 py-1.5 border border-slate-200">
              <img src={sewsLogo} alt="SEWS CABIND MAROC" className="h-5 w-auto" />
            </div>
            <span className="font-display font-bold text-slate-900">AutoReport System</span>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm shadow-slate-200/60 p-8">
            <div className="flex items-center justify-center mb-5">
              <div className="h-12 w-12 rounded-full bg-blue-600 flex items-center justify-center shadow-lg shadow-blue-600/30">
                <ShieldCheck size={22} className="text-white" strokeWidth={2.2} />
              </div>
            </div>
            <h2 className="text-center font-display font-bold text-xl text-slate-900 mb-1">{t('login.title')}</h2>
            <p className="text-center text-sm text-slate-500 mb-6">{t('login.subtitle')}</p>

            {error && (
              <div className="flex items-start gap-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2.5 mb-4">
                <AlertTriangle size={15} className="mt-0.5 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3.5">
              <div>
                <label className="text-xs font-medium text-slate-600 block mb-1.5">{t('login.email')}</label>
                <div className="relative">
                  <Mail size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder={t('login.emailPlaceholder')}
                    className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none transition-all"
                    autoFocus
                  />
                </div>
              </div>

              <div>
                <label className="text-xs font-medium text-slate-600 block mb-1.5">{t('login.password')}</label>
                <div className="relative">
                  <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder={t('login.passwordPlaceholder')}
                    className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none transition-all"
                  />
                </div>
              </div>

              <div className="flex items-center justify-between text-xs pt-1">
                <label className="flex items-center gap-1.5 text-slate-500 cursor-pointer">
                  <input type="checkbox" className="rounded border-slate-300 text-blue-600 focus:ring-blue-500/30" />
                  {t('login.rememberMe')}
                </label>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm py-2.5 rounded-lg transition-colors disabled:opacity-50 mt-1"
              >
                {loading ? t('login.submitting') : t('login.submit')}
                {!loading && <ArrowRight size={16} />}
              </button>
            </form>

            <div className="mt-6 pt-5 border-t border-slate-100">
              <div className="grid grid-cols-3 gap-2">
                {QUICK_ACCOUNTS.map((acc) => (
                  <button
                    key={acc.label}
                    type="button"
                    onClick={() => fillAccount(acc)}
                    className="flex flex-col items-center gap-1.5 rounded-lg border border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 py-2.5 transition-colors"
                  >
                    <div className="h-7 w-7 rounded-full bg-slate-100 flex items-center justify-center text-[11px] font-semibold text-slate-500">
                      {t(acc.label)[0]}
                    </div>
                    <span className="text-[11px] font-medium text-slate-600">{t(acc.label)}</span>
                    <span className="text-[9px] font-mono text-slate-400 leading-none">
                      {acc.ready ? t('login.accountReal') : t('login.accountToCreate')}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          </div>

          <p className="text-center text-[11px] text-slate-400 mt-5">
            {t('login.copyright')}
          </p>
        </div>
      </div>
    </div>
  );
}

function GlowShapes() {
  return (
    <div className="absolute inset-0 pointer-events-none">
      <div className="absolute -top-24 -right-24 h-96 w-96 rounded-full bg-blue-500/20 blur-3xl animate-pulse" />
      <div className="absolute bottom-0 -left-16 h-72 w-72 rounded-full bg-cyan-400/10 blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
      <svg className="absolute inset-0 h-full w-full opacity-[0.06]" viewBox="0 0 500 900" xmlns="http://www.w3.org/2000/svg">
        {Array.from({ length: 8 }).map((_, i) => (
          <path
            key={i}
            d={`M ${-50 + i * 16} -50 C ${150 + i * 10} 200, ${100 - i * 8} 500, ${350 + i * 12} 950`}
            stroke="#93C5FD"
            strokeWidth="1.2"
            fill="none"
          />
        ))}
      </svg>
    </div>
  );
}

function AnimatedStatCard({ stat, delay }) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setIsVisible(true), delay);
    return () => clearTimeout(timer);
  }, [delay]);

  return (
    <div
      className={`relative overflow-hidden rounded-lg bg-gradient-to-br ${stat.color} p-[1px] transition-all duration-700 ${
        isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
      }`}
    >
      <div className="bg-[#0F1B33] rounded-lg px-3 py-3.5 backdrop-blur-sm h-full">
        <div className="flex items-start justify-between mb-2">
          <stat.icon size={18} className="text-white/80" strokeWidth={2} />
          <div className={`h-1.5 w-1.5 rounded-full bg-gradient-to-r ${stat.color} animate-pulse`} />
        </div>
        <div className="font-display font-bold text-2xl text-white mb-0.5">{stat.value}</div>
        <div className="text-[10px] text-slate-400 uppercase tracking-wider">{stat.label}</div>
      </div>
    </div>
  );
}
