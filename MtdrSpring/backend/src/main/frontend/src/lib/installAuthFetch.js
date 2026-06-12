import { authClient } from './auth-client';

// Better Auth endpoints authenticate via session cookie, not Bearer token.
// They are also what getToken() itself calls, so intercepting them would recurse.
const EXCLUDED = /^\/api\/auth\//;

let cached = { token: null, exp: 0 };

function tokenExpiryMs(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return (payload.exp ?? 0) * 1000;
  } catch {
    return 0;
  }
}

async function getToken(nativeFetch) {
  if (cached.token && Date.now() < cached.exp - 30_000) return cached.token;

  let token = null;
  try {
    if (typeof authClient.token === 'function') {
      const { data } = await authClient.token();
      token = data?.token ?? null;
    }
    if (!token) {
      const res = await nativeFetch('/api/auth/token');
      if (res.ok) token = (await res.json())?.token ?? null;
    }
  } catch {
    token = null;
  }

  cached = { token, exp: token ? tokenExpiryMs(token) : 0 };
  return token;
}

/**
 * Wraps window.fetch so every same-origin request carries the Better Auth JWT.
 * The Spring backend rejects unauthenticated API calls, and call sites across
 * the app use bare fetch() — this keeps them working without touching each one.
 */
export function installAuthFetch() {
  const nativeFetch = window.fetch.bind(window);

  window.fetch = async (input, init = {}) => {
    const url = typeof input === 'string' ? input : input?.url ?? '';
    const isRelative = url.startsWith('/') && !url.startsWith('//');
    if (!isRelative || EXCLUDED.test(url)) {
      return nativeFetch(input, init);
    }

    const headers = new Headers(
      init.headers ?? (typeof input !== 'string' ? input?.headers : undefined)
    );
    if (!headers.has('Authorization')) {
      const token = await getToken(nativeFetch);
      if (token) headers.set('Authorization', `Bearer ${token}`);
    }

    return nativeFetch(input, { ...init, headers });
  };
}
