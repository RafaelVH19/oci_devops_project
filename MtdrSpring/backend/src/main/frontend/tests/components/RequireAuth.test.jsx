/**
 * Pruebas del guard de rutas que exige sesión (better-auth) salvo en modo demo.
 */
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import RequireAuth from '@/components/RequireAuth';
import { useSession } from '@/lib/auth-client';

/** Permite activar/desactivar demo en runtime dentro de los mocks de Jest. */
const demoModeMock = { isDemoMode: false };

jest.mock('@/config/demoMode', () => ({
  get isDemoMode() {
    return demoModeMock.isDemoMode;
  },
}));

jest.mock('@/lib/auth-client', () => ({
  useSession: jest.fn(),
}));

/**
 * Monta RequireAuth dentro de un router con ruta protegida /app y login /login.
 * @param {string} initialPath - URL inicial (p. ej. para probar query ?redirect)
 */
function renderProtected(initialPath = '/app') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route
          path="/app"
          element={
            <RequireAuth>
              <div>Protected content</div>
            </RequireAuth>
          }
        />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('RequireAuth', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    demoModeMock.isDemoMode = false;
  });

  /**
   * Con VITE_DEMO_MODE, no se consulta sesión: el hijo se renderiza directo.
   */
  it('renders children in demo mode without checking session', () => {
    demoModeMock.isDemoMode = true;
    useSession.mockReturnValue({ data: null, isPending: false });

    renderProtected();
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  /**
   * Mientras better-auth resuelve la sesión, se muestra el estado de carga.
   */
  it('shows loading state while session is pending', () => {
    useSession.mockReturnValue({ data: null, isPending: true });

    renderProtected();
    expect(screen.getByText(/checking session/i)).toBeInTheDocument();
  });

  /**
   * Sin sesión, React Router redirige a /login (aquí vemos la página de login del test).
   */
  it('redirects to login when there is no session', () => {
    useSession.mockReturnValue({ data: null, isPending: false });

    renderProtected('/app?tab=tasks');
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  /**
   * Con sesión válida, el contenido protegido es visible.
   */
  it('renders children when session exists', () => {
    useSession.mockReturnValue({
      data: { user: { email: 'alex@test.com' } },
      isPending: false,
    });

    renderProtected();
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });
});
