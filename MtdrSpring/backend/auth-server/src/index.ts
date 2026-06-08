import 'dotenv/config';
import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { cors } from 'hono/cors';
import { auth } from './auth';
import { syncAuthUserPassword } from './auth-db';

const app = new Hono();
const port = Number(process.env.AUTH_SERVER_PORT ?? 3001);

const trustedOrigins = [
  'http://localhost:5173',
  'http://localhost:8080',
  process.env.BETTER_AUTH_URL,
  ...(process.env.TRUSTED_ORIGINS ?? '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean),
].filter((origin): origin is string => Boolean(origin));

app.use(
  '*',
  cors({
    origin: (origin) => {
      if (!origin) return trustedOrigins[0];
      return trustedOrigins.includes(origin) ? origin : trustedOrigins[0];
    },
    credentials: true,
    allowHeaders: ['Content-Type', 'Authorization'],
    allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  }),
);

app.on(['POST', 'GET'], '/api/auth/*', (c) => auth.handler(c.req.raw));

/** For manager invites later: creates a Better Auth user (no public sign-up). */
app.post('/internal/users', async (c) => {
  const secret = process.env.INVITE_API_SECRET;
  if (!secret || c.req.header('x-invite-secret') !== secret) {
    return c.json({ error: 'Unauthorized' }, 401);
  }

  const body = await c.req.json<{
    email?: string;
    password?: string;
    name?: string;
    oracleUserId?: number;
  }>();
  const email = body.email?.trim();
  const password = body.password;
  const name = body.name?.trim();
  const oracleUserId = body.oracleUserId;

  if (!email || !password || !name) {
    return c.json({ error: 'email, password, and name are required' }, 400);
  }

  try {
    const createResult = await auth.api.createUser({
      body: {
        email,
        password,
        name,
        role: 'user',
        data:
          oracleUserId != null
            ? { oracleUserId }
            : undefined,
      },
    });

    if (createResult.error) {
      return c.json({ error: createResult.error }, 400);
    }

    return c.json({ user: createResult.data?.user ?? createResult.data, created: true }, 201);
  } catch {
    // User already exists — sync password below
  }

  const synced = await syncAuthUserPassword(email, password, name, oracleUserId);
  if (!synced) {
    return c.json({ error: 'Could not create or update user' }, 400);
  }

  return c.json(
    { user: { email, name, id: synced.userId }, created: false, passwordUpdated: true },
    200,
  );
});

app.get('/health', (c) => c.json({ ok: true }));

serve({ fetch: app.fetch, port }, () => {
  console.log(`Auth server listening on http://localhost:${port}`);
});


