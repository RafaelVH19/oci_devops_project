import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dbPath = path.join(__dirname, '..', 'data', 'auth.sqlite');
const db = new Database(dbPath, { readonly: true });

console.log('=== auth.sqlite tables ===');
console.log(db.prepare("SELECT name FROM sqlite_master WHERE type='table'").all());

console.log('\n=== user rows (first 200) ===');
try {
  const users = db.prepare('SELECT id, email, name, oracleUserId, createdAt, updatedAt FROM user ORDER BY id LIMIT 200').all();
  console.log(users);
} catch (e: any) {
  console.warn('Could not read user table:', e?.message ?? e);
}

console.log('\n=== account rows (first 200) ===');
try {
  const accounts = db.prepare('SELECT id, userId, providerId, password, createdAt, updatedAt FROM account ORDER BY id LIMIT 200').all();
  console.log(accounts);
} catch (e: any) {
  console.warn('Could not read account table:', e?.message ?? e);
}

console.log('\n=== user + account join (credential provider) ===');
try {
  const joined = db.prepare(`SELECT u.id as userId, u.email, u.name, a.providerId, a.password, a.updatedAt
    FROM user u
    LEFT JOIN account a ON a.userId = u.id AND a.providerId = 'credential'
    ORDER BY u.id LIMIT 200`).all();
  console.log(joined);
} catch (e: any) {
  console.warn('Could not run join:', e?.message ?? e);
}
