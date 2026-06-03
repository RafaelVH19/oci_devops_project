import 'dotenv/config';
import { syncAuthUserPassword } from '../src/auth-db';

const email = process.env.RESET_EMAIL ?? 'manager@lumen.dev';
const password = process.env.RESET_PASSWORD ?? 'LumenDev1!';
const name = process.env.RESET_NAME ?? 'Demo Manager';

async function main() {
  const synced = await syncAuthUserPassword(email, password, name);
  if (!synced) {
    console.error(`No Better Auth user found for ${email}. Run npm run seed first.`);
    process.exit(1);
  }
  console.log(`Web login updated for ${email}`);
  console.log(`Password: ${password}`);
}

main();
