import { useEffect, useRef, useState } from 'react';
import { isDemoMode } from '../config/demoMode';
import { useSession } from '../lib/auth-client';

/**
 * Loads the Oracle USERS row for the signed-in Better Auth email.
 */
export function useOracleUser() {
  const { data: session, isPending: sessionPending } = useSession();
  const [oracleUser, setOracleUser] = useState(null);
  const [loading, setLoading] = useState(!isDemoMode);
  // Tracks the email already loaded so focus-triggered session refetches
  // (e.g. alt-tab) don't flip `loading` and remount skeletons.
  const loadedEmailRef = useRef(null);

  const email = session?.user?.email ?? null;

  useEffect(() => {
    if (isDemoMode) {
      setLoading(false);
      return undefined;
    }

    if (sessionPending) {
      return undefined;
    }

    if (!email) {
      loadedEmailRef.current = null;
      setOracleUser(null);
      setLoading(false);
      return undefined;
    }

    if (loadedEmailRef.current === email) {
      setLoading(false);
      return undefined;
    }

    let cancelled = false;
    setLoading(true);

    (async () => {
      try {
        const res = await fetch(`/api/users/by-email?email=${encodeURIComponent(email)}`);
        if (cancelled) return;
        if (res.ok) {
          setOracleUser(await res.json());
          loadedEmailRef.current = email;
        } else {
          setOracleUser(null);
        }
      } catch {
        if (!cancelled) setOracleUser(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [email, sessionPending]);

  return {
    session,
    sessionPending,
    oracleUser,
    loading: isDemoMode ? false : sessionPending || loading,
    displayName:
      oracleUser?.name || session?.user?.name || 'User',
    email: oracleUser?.email || session?.user?.email,
    oracleUserId: oracleUser?.id,
    role: oracleUser?.role,
  };
}
