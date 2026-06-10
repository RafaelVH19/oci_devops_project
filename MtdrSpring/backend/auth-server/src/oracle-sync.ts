import fetch from 'node-fetch';
import { auth } from './auth';
import { syncAuthUserPassword } from './auth-db';

const springUrl = process.env.SPRING_URL ?? process.env.SPRING_URL_INTERNAL ?? 'http://localhost:8080';

function randPass(len = 12) {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%';
  let s = '';
  for (let i = 0; i < len; i++) s += chars[Math.floor(Math.random() * chars.length)];
  return s;
}

export async function syncUsersFromSpring(options: { retries?: number; delayMs?: number } = {}): Promise<void> {
  const retries = options.retries ?? 60;
  const delayMs = options.delayMs ?? 5000;

  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      const res = await fetch(`${springUrl.replace(/\/$/, '')}/users`);
      if (!res.ok) {
        console.warn('Could not fetch users from Spring:', res.status, await res.text());
        if (attempt < retries) {
          console.log(`Retrying in ${delayMs}ms (attempt ${attempt}/${retries})`);
          await new Promise((r) => setTimeout(r, delayMs));
          continue;
        }
        return;
      }

      const users = await res.json();
      if (!Array.isArray(users)) return;

      for (const u of users) {
        const email = (u.email ?? '').toString().trim().toLowerCase();
        const name = (u.name ?? '').toString().trim();
        if (!email) continue;

        try {
          const password = randPass();
          const result = await auth.api.createUser({ body: { email, password, name, role: 'user', data: { oracleUserId: u.id } } });
          if (result.error) {
            // user probably exists — no problem
          } else {
            console.log(`Created auth user for ${email}`);
          }
        } catch (e) {
          // ignore individual failures
        }
      }

      // success
      return;
    } catch (e: any) {
      console.warn('Oracle sync attempt failed:', e?.message ?? e);
      if (attempt < retries) {
        console.log(`Retrying in ${delayMs}ms (attempt ${attempt}/${retries})`);
        await new Promise((r) => setTimeout(r, delayMs));
        continue;
      }
      return;
    }
  }
}
