import { Navigate } from 'react-router-dom';
import { useOracleUser } from '../hooks/useOracleUser';
import { isDemoMode } from '../config/demoMode';

function RequireManager({ children }) {
  const { loading, role } = useOracleUser();

  if (isDemoMode) return children;

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-white text-[#2a1814]">
        <p className="text-sm text-[#2a1814]/60">Checking permissions…</p>
      </div>
    );
  }

  if (role === 'DEVELOPER') return <Navigate to="/app" replace />;

  return children;
}

export default RequireManager;
