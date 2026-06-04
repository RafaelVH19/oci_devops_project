# Auth en producción (OCI / Kubernetes)

El login web usa **Better Auth** (`auth-server`). En producción:

- Los usuarios solo entran por la **URL pública de Spring** (`/api/auth` vía proxy).
- El puerto **3001 no se publica** en internet.
- Spring llama al auth por red interna: `AUTH_SERVER_URL=http://lumen-auth-service:3001`.

## 1. Imágenes Docker

Construir y subir **dos** imágenes al registry (mismos placeholders que Spring):

```bash
cd backend
mvn clean package -DskipTests

# Spring + auth (mismo tag, p. ej. git SHA)
TAG=$(git rev-parse --short HEAD)
source build.sh   # Maven + push todolistapp-springboot:$TAG y lumen-auth-server:$TAG

# OCI DevOps: build_spec.yaml ejecuta build.sh, undeploy.sh y deploy.sh
```

## 2. Secretos en el cluster

```bash
kubectl create secret generic lumen-auth-secrets \
  --from-literal=better-auth-secret='genera-min-32-caracteres' \
  --from-literal=invite-api-secret='mismo-valor-en-spring-y-auth'
```

`invite-api-secret` debe coincidir con `INVITE_API_SECRET` / `auth.server.invite-secret`.

## 3. Manifiestos

Sustituir en los YAML:

| Placeholder | Ejemplo |
|-------------|---------|
| `%DOCKER_REGISTRY%` | `phx.ocir.io/.../repo` |
| `%IMAGE_VERSION%` | tag de imagen (p. ej. SHA de git; lo fija `build.sh`) |
| `%LUMEN_PUBLIC_URL%` | **`http://<IP-del-LoadBalancer>`** (lo normal en OCI; sin barra final). Dominio opcional. |
| `%OCI_REGION%` | región OCI |

`deploy.sh` sustituye placeholders. **No necesitas definir la IP a mano** en el primer pipeline: despliega Spring, espera la IP del service `todolistapp-springboot-service` y usa `http://esa-ip` para auth y Spring. Si ya conoces la IP: `export LUMEN_PUBLIC_URL=http://150.136.x.x`.

Orden recomendado:

```bash
kubectl apply -f src/main/resources/todolistapp-auth-server.yaml
kubectl apply -f src/main/resources/todolistapp-springboot.yaml
```

- `todolistapp-auth-server.yaml` — Deployment + **ClusterIP** `:3001` + PVC SQLite.
- `todolistapp-springboot.yaml` — `SPRING_PROFILES_ACTIVE=prod`, `AUTH_SERVER_URL`, `LUMEN_PUBLIC_URL`.

Solo el **LoadBalancer de Spring** (puerto 80 → 8080) queda expuesto.

## 4. Variables de entorno (resumen)

| Variable | Dónde | Uso |
|----------|--------|-----|
| `BETTER_AUTH_URL` | auth-server | URL pública de la app (cookies, redirects) |
| `BETTER_AUTH_SECRET` | auth-server | Secreto Better Auth (≥32 chars) |
| `INVITE_API_SECRET` | auth + Spring | Invite / `POST /internal/users` |
| `AUTH_SERVER_URL` | Spring | URL **interna** del auth (`http://lumen-auth-service:3001`) |
| `LUMEN_PUBLIC_URL` | Spring | Enlaces en correos de invite |
| `TRUSTED_ORIGINS` | auth (opcional) | CORS extra, separado por comas |

## 5. Desarrollo local

| Modo | Comando | Puerto 3001 en host |
|------|---------|---------------------|
| Dev (scripts, debug) | `docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build` | Sí |
| Prod-like | `docker compose up -d --build` | No (seed vía `:8080`) |

`.\buildImgContainer.ps1` usa el modo **dev** (incluye `docker-compose.dev.yml`).

## 6. Usuario inicial en prod

No uses `npm run seed` en producción desde tu laptop salvo que tengas acceso al cluster.

1. Despliega auth + Spring.
2. Crea el primer manager con `POST /invite-user` (dashboard o curl contra la URL pública).
3. Spring crea Oracle + cuenta Better Auth vía `AUTH_SERVER_URL` interno.
