import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import PageHeader from '../PageHeader';

describe('PageHeader', () => {
  it('affiche le titre correctement', () => {
    render(<PageHeader title="Test Page" />);
    expect(screen.getByText('Test Page')).toBeInTheDocument();
  });

  it('affiche l\'eyebrow si fourni', () => {
    render(<PageHeader eyebrow="Test Eyebrow" title="Test Page" />);
    expect(screen.getByText('Test Eyebrow')).toBeInTheDocument();
  });

  it('affiche l\'action si fournie', () => {
    render(
      <PageHeader 
        title="Test Page" 
        action={<button>Action Button</button>}
      />
    );
    expect(screen.getByText('Action Button')).toBeInTheDocument();
  });

  it('affiche tous les éléments ensemble', () => {
    render(
      <PageHeader
        eyebrow="Eyebrow Text"
        title="Page Title"
        action={<button>Click Me</button>}
      />
    );

    expect(screen.getByText('Eyebrow Text')).toBeInTheDocument();
    expect(screen.getByText('Page Title')).toBeInTheDocument();
    expect(screen.getByText('Click Me')).toBeInTheDocument();
  });
});
