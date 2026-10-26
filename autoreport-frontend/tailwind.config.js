/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class', // Activer le mode sombre avec la classe 'dark'
  theme: {
    extend: {
      colors: {
        bg: '#F8FAFC',
        surface: '#FFFFFF',
        'surface-2': '#F1F5F9',
        'surface-3': '#E2E8F0',
        border: '#E2E8F0',
        text: {
          DEFAULT: '#0F172A',
          muted: '#64748B',
          faint: '#94A3B8',
        },
        amber: {
          DEFAULT: '#2563EB',
          dim: '#93C5FD',
        },
        teal: {
          DEFAULT: '#0D9488',
          dim: '#5EEAD4',
        },
        alert: {
          DEFAULT: '#DC2626',
          dim: '#FCA5A5',
        },
        // Couleurs pour le mode sombre
        'dark-bg': '#0F172A',
        'dark-surface': '#1E293B',
        'dark-surface-2': '#334155',
        'dark-surface-3': '#475569',
        'dark-border': '#334155',
        'dark-text': {
          DEFAULT: '#F1F5F9',
          muted: '#94A3B8',
          faint: '#64748B',
        },
      },
      fontFamily: {
        display: ['"Big Shoulders Display"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      backgroundImage: {
        grid: 'linear-gradient(rgba(15,23,42,0.035) 1px, transparent 1px), linear-gradient(90deg, rgba(15,23,42,0.035) 1px, transparent 1px)',
        'grid-dark': 'linear-gradient(rgba(241,245,249,0.035) 1px, transparent 1px), linear-gradient(90deg, rgba(241,245,249,0.035) 1px, transparent 1px)',
        hazard: 'repeating-linear-gradient(135deg, rgba(37,99,235,0.9) 0 10px, rgba(248,250,252,0.9) 10px 20px)',
        'hazard-dark': 'repeating-linear-gradient(135deg, rgba(59,130,246,0.9) 0 10px, rgba(15,23,42,0.9) 10px 20px)',
      },
      backgroundSize: {
        grid: '28px 28px',
      },
    },
  },
  plugins: [],
};
