/** True when built with `pnpm build:demo` (VITE_DEMO_MODE in .env.demo). */
export const isDemoMode = import.meta.env.VITE_DEMO_MODE === 'true';
