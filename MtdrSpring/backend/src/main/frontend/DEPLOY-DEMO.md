# Publicar demo (Dev + Dashboard con datos mock)

Build de solo frontend, sin Spring ni Oracle. Incluye `/app` y `/dashboard` con datos de ejemplo.

## 1. Probar en local

```bash
cd MtdrSpring/backend/src/main/frontend
pnpm install
pnpm dev:demo
```

Abre:

- Dev: http://localhost:5173/app
- Dashboard: http://localhost:5173/dashboard

## 2. Build de producción

```bash
pnpm build:demo
```

Salida en `build/`. Para previsualizar:

```bash
pnpm preview:demo
```

## 3. Vercel (recomendado, gratis)

1. Sube el repo a GitHub (si aún no está).
2. Entra en [vercel.com](https://vercel.com) → **Add New Project** → importa el repo.
3. Configura el proyecto:
   - **Root Directory**: `MtdrSpring/backend/src/main/frontend`
   - **Build Command**: `pnpm build:demo`
   - **Output Directory**: `build`
   - **Install Command**: `pnpm install`
4. Deploy. Te dará una URL tipo `https://tu-proyecto.vercel.app`.

Rutas para compartir:

| Vista | URL |
|-------|-----|
| Inicio | `/` |
| Dev (tareas) | `/app` |
| Dashboard | `/dashboard` |

## 4. Netlify (alternativa)

- Base directory: `MtdrSpring/backend/src/main/frontend`
- Build: `pnpm build:demo`
- Publish: `build`
- Añade redirect SPA: `/* /index.html 200` (en `_redirects` o UI de Netlify)

## 5. GitHub Pages

En `vite.config.js` puede hacer falta `base: '/nombre-repo/'`. Usa Vercel/Netlify si quieres menos configuración.

---

**Importante:** usa siempre `build:demo` (no `build` normal) para el hosting público. El banner naranja confirma que está en modo demo.
