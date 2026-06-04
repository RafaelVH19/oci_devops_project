/**
 * Pruebas del banner fijo que avisa que la app corre en modo demo.
 */
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import DemoBanner from '@/components/DemoBanner';

describe('DemoBanner', () => {
  /**
   * Verifica el copy del banner y que los enlaces apunten a las rutas dev y dashboard.
   * MemoryRouter es necesario porque DemoBanner usa <Link> de react-router.
   */
  it('shows demo preview message and navigation links', () => {
    render(
      <MemoryRouter>
        <DemoBanner />
      </MemoryRouter>
    );

    expect(screen.getByText('Demo preview')).toBeInTheDocument();
    expect(screen.getByText(/sample data only/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Dev view' })).toHaveAttribute('href', '/app');
    expect(screen.getByRole('link', { name: 'Dashboard' })).toHaveAttribute('href', '/dashboard');
  });
});
