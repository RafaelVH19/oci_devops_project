import Database from 'better-sqlite3';

const db = new Database('data/auth.sqlite');
console.log('tables', db.prepare("SELECT name FROM sqlite_master WHERE type='table'").all());
console.log('account cols', db.prepare('PRAGMA table_info(account)').all());
console.log('user cols', db.prepare('PRAGMA table_info(user)').all());
