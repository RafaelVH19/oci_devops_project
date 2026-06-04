# Lumen auth server (Better Auth)

Email/password login only. **No public sign-up** — users are created via manager invite (`/internal/users`) or `npm run seed`.

## Setup

```bash
cd backend/auth-server
cp .env.example .env
# Edit BETTER_AUTH_SECRET (min 32 chars); INVITE_API_SECRET must match Spring (application.properties)
npm install
npm run migrate
npm run dev
```

In another terminal, start Spring Boot, then create the integrated dev user (Oracle + Better Auth):

```bash
npm run seed
```

Default dev user after seed:

- Email: `manager@lumen.dev`
- Password: printed by the seed command (temporary password from `/invite-user`)

If login says **invalid password**, the web account may predate the integrated invite. Try:

```bash
# Known dev password
npm run reset-password

# Or the password shown in the invite toast
RESET_PASSWORD=your-temp-password npm run reset-password
```

Then restart the auth server if it was already running (`npm run dev`).

## Run with frontend

1. Auth server: `npm run dev` (port 3001)
2. Spring Boot: port 8080
3. Vite: `npm run dev` in `src/main/frontend` (proxies `/api/auth` → 3001)

## Run with Docker Compose (recomendado)

Auth y Spring en Docker (`backend/docker-compose.yml`). En **producción el :3001 no se publica**; en dev usa el override:

```bash
cd backend
mvn clean package   # o verify
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
cd auth-server && npm run seed
```

- App: http://localhost:8080  
- Auth interno: `http://auth-server:3001` (solo red Docker; Spring usa `AUTH_SERVER_URL`)  
- `:3001` en el host solo con `docker-compose.dev.yml` (depuración / `reset-password`)  
- `BETTER_AUTH_URL`: `http://localhost:8080` (lo que ve el navegador)

`.\buildImgContainer.ps1` aplica el modo dev (incluye `docker-compose.dev.yml`).

**Producción (OCI/K8s):** ver [../DEPLOY-AUTH.md](../DEPLOY-AUTH.md) — ClusterIP, sin LoadBalancer en 3001.

## Run with Docker (solo Spring en contenedor)

Spring en **:8080** proxies `/api/auth` a `http://host.docker.internal:3001`. Ahí el auth-server debe correr en el **host**:

1. `auth-server/.env`: `BETTER_AUTH_URL=http://localhost:8080`
2. `npm run dev` (puerto 3001)
3. Contenedor Spring
4. `npm run seed`

Sin auth en el host, login devuelve 502.

## Manager invite

The dashboard calls Spring `POST /invite-user`, which:

1. Saves the user in **Oracle** (`USERS`) with a BCrypt password hash.
2. Calls `POST /internal/users` on this server so they can sign in on the web.

`INVITE_API_SECRET` in `.env` must match `auth.server.invite-secret` in Spring.

## Invite email

Spring sends HTML invites via **Resend** (`RESEND_API_KEY` in `application-local.properties` or Docker `-e`). Login button URL is taken from the browser (`window.location.origin`) or `LUMEN_PUBLIC_URL` in production. No SMTP form in the UI.
