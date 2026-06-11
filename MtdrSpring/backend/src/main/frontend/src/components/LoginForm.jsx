import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { primaryAuthButtonClass } from '../constants/authStyles';
import { signIn } from '../lib/auth-client';
import { isDemoMode } from '../config/demoMode';
import InputField from './InputField';

function LoginForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const redirectTo = searchParams.get('redirect') || '/app';

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (isDemoMode) {
      navigate(redirectTo.startsWith('/') ? redirectTo : '/app');
      return;
    }

    setSubmitting(true);
    try {
      const { error: signInError } = await signIn.email({
        email: email.trim(),
        password,
      });

      if (signInError) {
        setError(signInError.message ?? 'Invalid email or password.');
        return;
      }

      let target = redirectTo.startsWith('/') ? redirectTo : '/app';
      try {
        const profileRes = await fetch(
          `/api/users/by-email?email=${encodeURIComponent(email.trim())}`
        );
        if (profileRes.ok) {
          const profile = await profileRes.json();
          if (profile?.role === 'MANAGER' && (target === '/app' || target === '/login')) {
            target = '/dashboard';
          }
        }
      } catch {
        /* use default target */
      }
      navigate(target, { replace: true });
    } catch {
      setError('Could not sign in. Is the auth server running?');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <form className="mt-8 space-y-6 text-left" onSubmit={handleSubmit}>
        <InputField
          label="Email"
          type="email"
          name="email"
          placeholder="you@company.com"
          variant="underline"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          autoComplete="email"
        />
        <InputField
          label="Password"
          type="password"
          name="password"
          placeholder="••••••••"
          variant="underline"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          autoComplete="current-password"
        />

        {error ? (
          <p className="text-sm text-red-600" role="alert">
            {error}
          </p>
        ) : null}

        <button
          type="submit"
          className={primaryAuthButtonClass}
          disabled={submitting}
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="mt-8 text-center text-sm text-[#2a1814]/60">
        Need help?{' '}
        <a
          href="mailto:support@lumen.app"
          className="font-medium text-[#2a1814] underline decoration-[#2a1814]/30 underline-offset-2 transition hover:decoration-[#2a1814]"
        >
          Contact support
        </a>
      </p>

      {isDemoMode ? (
        <p className="mt-4 text-center text-xs text-[#2a1814]/45">
          <Link to="/app" className="hover:text-[#2a1814]">
            Developer
          </Link>
          <span className="mx-2">·</span>
          <Link to="/dashboard" className="hover:text-[#2a1814]">
            Dashboard
          </Link>
        </p>
      ) : null}
    </>
  );
}

export default LoginForm;
