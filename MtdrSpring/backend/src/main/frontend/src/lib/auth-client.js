import { createAuthClient } from "better-auth/react";
import { jwtClient } from "better-auth/client/plugins";

export const authClient = createAuthClient({
  baseURL: import.meta.env.VITE_AUTH_URL ?? "",
  plugins: [jwtClient()]
});

//for debugging
if (typeof window !== 'undefined') {
  window.authClient = authClient;
}

export const { signIn, signOut, useSession } = authClient;