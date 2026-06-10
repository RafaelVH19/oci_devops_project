import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const db = new Database(path.join(__dirname, '..', 'data', 'auth.sqlite'));
const springUrl = process.env.SPRING_URL ?? process.env.SPRING_URL_INTERNAL ?? 'http://localhost:8080';

const email = process.env.DELETE_EMAIL;
if (!email) {
  console.error('Usage: DELETE_EMAIL=user@example.com npm run delete-user');
  process.exit(1);
}

const normalizedEmail = email.trim().toLowerCase();
const user = db
  .prepare('SELECT id, email, name, oracleUserId FROM user WHERE lower(email) = ?')
  .get(normalizedEmail) as { id: string; email: string; name: string; oracleUserId: number | null } | undefined;

if (!user) {
  console.error(`No user found in Better Auth for email: ${normalizedEmail}`);
  process.exit(1);
}

// Delete from Oracle via Spring
if (user.oracleUserId != null) {
  console.log(`Deleting Oracle user id=${user.oracleUserId} via Spring...`);
  const res = await fetch(`${springUrl.replace(/\/$/, '')}/deleteUser/${user.oracleUserId}`, { method: 'DELETE' });
  if (res.ok) {
    console.log(`  Oracle user deleted.`);
  } else {
    console.warn(`  Spring responded ${res.status}: ${await res.text()}`);
    console.warn('  Continuing with Better Auth deletion anyway.');
  }
} else {
  console.warn('  No oracleUserId on this user — skipping Oracle deletion.');
}

// Delete from Better Auth
const deleteAll = db.transaction(() => {
  const accounts = db.prepare('DELETE FROM account WHERE userId = ?').run(user.id);
  const sessions = db.prepare('DELETE FROM session WHERE userId = ?').run(user.id);
  const users = db.prepare('DELETE FROM user WHERE id = ?').run(user.id);
  return { accounts: accounts.changes, sessions: sessions.changes, users: users.changes };
});

const result = deleteAll();
console.log(`Deleted Better Auth user: ${user.name} <${user.email}>`);
console.log(`  user rows: ${result.users}, account rows: ${result.accounts}, session rows: ${result.sessions}`);
