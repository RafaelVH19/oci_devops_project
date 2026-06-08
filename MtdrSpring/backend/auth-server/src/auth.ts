import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';
import { betterAuth } from 'better-auth';
import { admin, jwt } from 'better-auth/plugins';
import { authDbPath } from './auth-db';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const trustedOrigins = [
  'http://localhost:5173',
  'http://localhost:8080',
  process.env.BETTER_AUTH_URL,
].filter((origin): origin is string => Boolean(origin));

export const auth = betterAuth({
  appName: 'Lumen',
  secret: process.env.BETTER_AUTH_SECRET,
  baseURL: process.env.BETTER_AUTH_URL ?? 'http://localhost:3001',
  database: new Database(authDbPath),
  trustedOrigins,
  user: {
    additionalFields: {
      oracleUserId: {
        type: 'number',
        required: false,
        input: false,
      },
    },
  },
  emailAndPassword: {
    enabled: true,
    disableSignUp: true,
    minPasswordLength: 8,
  },
    disabledPaths: ['/sign-up/email'],
  plugins: [admin(), jwt()],
});
