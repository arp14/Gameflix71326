# GameFlix frontend

React + Vite app for GameFlix. Talks to the Spring Boot backend (run
separately, see the repo root) via same-origin `/api/...` paths — the dev
server proxies those to `http://localhost:8080` (see `vite.config.js`), so no
CORS configuration is needed on the backend.

## Running locally

1. Start the backend first (from the repo root): run it in IntelliJ, or
   `docker compose up`, so it's listening on `localhost:8080`.
2. Then, in this `frontend/` directory:
   ```
   npm install
   npm run dev
   ```
3. Open the URL Vite prints (typically `http://localhost:5173`).

## Building for production

```
npm run build
```

Outputs static files to `dist/`.
