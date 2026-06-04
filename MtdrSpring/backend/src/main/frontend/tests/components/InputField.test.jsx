/**
 * Pruebas del componente InputField (campos de login y formularios).
 */
import { render, screen } from '@testing-library/react';
import InputField from '@/components/InputField';

describe('InputField', () => {
  /**
   * variant="underline" muestra label + input con borde inferior (estilo login).
   * El id del input se deriva del label ("Email" → id="email").
   */
  it('renders label and input with underline variant', () => {
    render(
      <InputField
        label="Email"
        type="email"
        placeholder="you@example.com"
        variant="underline"
        value=""
        onChange={() => {}}
      />
    );

    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('you@example.com')).toHaveAttribute('type', 'email');
    expect(screen.getByLabelText('Email')).toHaveAttribute('id', 'email');
  });

  /**
   * Sin variant, el componente usa el estilo "boxed" (fondo gris, bordes redondeados).
   */
  it('renders boxed variant by default', () => {
    render(
      <InputField
        label="Password"
        type="password"
        value=""
        onChange={() => {}}
      />
    );

    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toHaveAttribute('id', 'password');
  });
});
