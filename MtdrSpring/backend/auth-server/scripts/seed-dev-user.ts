/**
 * Creates a dev user in Oracle (via Spring) and Better Auth in one step.
 * Requires Spring Boot on :8080 and auth-server on :3001 with matching INVITE_API_SECRET.
 */
import 'dotenv/config';

const springUrl = process.env.SPRING_URL ?? 'http://localhost:8080';
const authServerUrl = process.env.AUTH_SERVER_URL ?? 'http://localhost:3001';
const email = process.env.SEED_EMAIL ?? 'manager@lumen.dev';
const name = process.env.SEED_NAME ?? 'Demo Manager';

async function syncWebLogin(oracleUserId?: number) {
  const secret = process.env.INVITE_API_SECRET ?? 'dev-invite-secret-change-me';
  const password = process.env.SEED_PASSWORD ?? 'LumenDev1!';
  const sync = await fetch(`${authServerUrl.replace(/\/$/, '')}/internal/users`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-invite-secret': secret,
    },
    body: JSON.stringify({ email, password, name, oracleUserId }),
  });
  if (sync.ok) {
    console.log(`Web login password set to: ${password}`);
    return true;
  }
  console.log('Could not sync auth password:', await sync.text());
  return false;
}

async function main() {
  const res = await fetch(`${springUrl.replace(/\/$/, '')}/invite-user`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name,
      email,
      role: 'MANAGER',
      workMode: 'REMOTE',
    }),
  });

  if (res.status === 409) {
    console.log(`Oracle user already exists: ${email}`);
    console.log('Syncing web login password via auth-server…');
    await syncWebLogin();
    return;
  }

  if (!res.ok) {
    const text = await res.text();
    console.error(`Seed failed (${res.status}):`, text);
    process.exit(1);
  }

  const data = await res.json();
  console.log(`Invited: ${email}`);
  console.log(`Temporary password: ${data.temporaryPassword}`);
  console.log(`Auth account: ${data.authAccountCreated ? 'yes' : 'no (is auth-server running?)'}`);
  console.log(`Oracle user id: ${data.user?.id}`);
}

main();
