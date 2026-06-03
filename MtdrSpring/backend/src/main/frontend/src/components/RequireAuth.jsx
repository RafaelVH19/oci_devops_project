import { Navigate, useLocation } from 'react-router-dom';
import { useSession } from '../lib/auth-client';
import { isDemoMode } from '../config/demoMode';

function RequireAuth({ children }) {
  const location = useLocation();
  const { data: session, isPending } = useSession();

  if (isDemoMode) {
    return children;
  }

  if (isPending) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-white text-[#2a1814]">
        <p className="text-sm text-[#2a1814]/60">Checking session…</p>
      </div>
    );
  }

  if (!session) {
    const redirect = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?redirect=${redirect}`} replace />;
  }

  return children;
}

export default RequireAuth;
