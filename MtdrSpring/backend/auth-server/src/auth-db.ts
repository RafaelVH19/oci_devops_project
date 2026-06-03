import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';
import { hashPassword } from 'better-auth/crypto';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const authDbPath = path.join(__dirname, '..', 'data', 'auth.sqlite');
export const authDb = new Database(authDbPath);

export async function syncAuthUserPassword(
  email: string,
  password: string,
  name?: string,
  oracleUserId?: number,
): Promise<{ userId: string; passwordUpdated: boolean } | null> {
  const normalizedEmail = email.trim().toLowerCase();
  const row = authDb
    .prepare('SELECT id, name FROM user WHERE lower(email) = ?')
    .get(normalizedEmail) as { id: string; name: string } | undefined;

  if (!row?.id) {
    return null;
  }

  const hash = await hashPassword(password);
  const now = new Date().toISOString();

  authDb
    .prepare(
      `UPDATE account SET password = ?, updatedAt = ?
       WHERE userId = ? AND providerId = 'credential'`,
    )
    .run(hash, now, row.id);

  if (name?.trim()) {
    authDb.prepare('UPDATE user SET name = ?, updatedAt = ? WHERE id = ?').run(name.trim(), now, row.id);
  }

  if (oracleUserId != null) {
    authDb
      .prepare('UPDATE user SET oracleUserId = ?, updatedAt = ? WHERE id = ?')
      .run(oracleUserId, now, row.id);
  }

  return { userId: row.id, passwordUpdated: true };
}
