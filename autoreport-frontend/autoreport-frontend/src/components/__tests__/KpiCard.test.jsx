import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import KpiCard from '../KpiCard';
import { TrendingUp } from 'lucide-react';

describe('KpiCard', () => {
  it('affiche correctement le titre et la valeur', () => {
    render(
      <KpiCard
        title="Production Totale"
        value="15,000"
        icon={TrendingUp}
        color="blue"
      />
    );

    expect(screen.getByText('Production Totale')).toBeInTheDocument();
    expect(screen.getByText('15,000')).toBeInTheDocument();
  });

  it('affiche le sous-titre si fourni', () => {
    render(
      <KpiCard
        title="Taux de Qualité"
        value="98.5%"
        subtitle="+2.5% vs mois dernier"
        icon={TrendingUp}
        color="green"
      />
    );

    expect(screen.getByText('+2.5% vs mois dernier')).toBeInTheDocument();
  });

  it('applique la bonne classe de couleur', () => {
    const { container } = render(
      <KpiCard
        title="Défauts"
        value="150"
        icon={TrendingUp}
        color="red"
      />
    );

    const iconContainer = container.querySelector('.bg-alert\\/10');
    expect(iconContainer).toBeInTheDocument();
  });

  it('affiche correctement sans sous-titre', () => {
    render(
      <KpiCard
        title="Production"
        value="1000"
        icon={TrendingUp}
        color="blue"
      />
    );

    expect(screen.getByText('Production')).toBeInTheDocument();
    expect(screen.getByText('1000')).toBeInTheDocument();
  });
});
