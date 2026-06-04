/**
 * Pruebas del formulario de login: UI, modo demo, errores de auth y redirect por rol.
 */
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import LoginForm from '@/components/LoginForm';
import { signIn } from '@/lib/auth-client';

const demoModeMock = { isDemoMode: false };
const mockNavigate = jest.fn();

jest.mock('@/config/demoMode', () => ({
  get isDemoMode() {
    return demoModeMock.isDemoMode;
  },
}));

jest.mock('@/lib/auth-client', () => ({
  signIn: {
    email: jest.fn(),
  },
}));

/** Sustituye useNavigate para comprobar a qué ruta navega el formulario sin salir del test. */
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

function renderLogin(path = '/login') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <LoginForm />
    </MemoryRouter>
  );
}

describe('LoginForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    demoModeMock.isDemoMode = false;
    global.fetch = jest.fn();
  });

  /** Comprueba que el formulario monta los campos mínimos y el botón de envío. */
  it('renders email and password fields', () => {
    renderLogin();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  /**
   * En demo, el submit solo navega al redirect de la URL (?redirect=...) sin llamar a signIn.
   * Se rellenan campos para pasar la validación HTML5 required del formulario.
   */
  it('navigates on submit in demo mode without calling auth', async () => {
    demoModeMock.isDemoMode = true;
    const user = userEvent.setup();
    renderLogin('/login?redirect=/dashboard');

    await user.type(screen.getByLabelText('Email'), 'demo@test.com');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(signIn.email).not.toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  /**
   * Si better-auth devuelve error, se muestra en un párrafo con role="alert".
   */
  it('shows error when sign-in fails', async () => {
    signIn.email.mockResolvedValue({
      error: { message: 'Invalid email or password.' },
    });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByLabelText('Email'), 'bad@test.com');
    await user.type(screen.getByLabelText('Password'), 'wrong');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Invalid email or password.');
    });
  });

  /**
   * Tras login OK, consulta /users/by-email; si el rol es MANAGER y el destino es /app,
   * redirige a /dashboard en lugar del redirect por defecto.
   */
  it('redirects managers to dashboard after successful sign-in', async () => {
    signIn.email.mockResolvedValue({ error: null });
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ role: 'MANAGER' }),
    });

    const user = userEvent.setup();
    renderLogin('/login?redirect=/app');

    await user.type(screen.getByLabelText('Email'), 'manager@test.com');
    await user.type(screen.getByLabelText('Password'), 'secret');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    });
  });
});
