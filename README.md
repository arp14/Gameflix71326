# GameFlix

A Spring Boot 3 (Java 17) + React application built as a semester-long
assignment: user auth with JWT, MySQL persistence, Docker packaging, a
CI/CD pipeline, a supply-chain security audit, and a React frontend on top.

**Two versions live in this repo.** Everything below describes **v1**,
the monolith at the repo root — this is the graded assignment deliverable
and the one the frontend talks to by default. **v2**, a microservices
split (Auth/Games/Reviews services + an nginx gateway), lives alongside
it in [`services/`](services/README.md) without touching v1 at all —
both are complete, and the same React frontend works unmodified against
either one; see that directory for its own docs.

## Tech stack

- **Backend:** Spring Boot 3.3.13, Spring Data JPA + Hibernate, MySQL
  (`mysql-connector-j`), BCrypt password hashing (`spring-security-crypto`,
  not the full Spring Security starter), JWT via `io.jsonwebtoken` (jjwt)
- **Frontend:** React + Vite, `react-router-dom`
- **Testing:** JUnit 5 + Mockito
- **Packaging/CI:** Docker (multi-stage build), Docker Compose, GitHub Actions

## Project layout

```
src/main/java/com/gameflix/
  controller/   AuthController, GameController, ProfileController, ReviewController
  service/      UserService, GameService, ReviewService
  security/     JwtService, JwtAuthFilter, JwtFilterConfig
  model/        User, Credential, Game, Review
  repository/   Spring Data JPA repositories
  dto/          Request/response DTOs for auth and reviews

frontend/
  src/pages/       HomePage, LoginPage, RegisterPage, GamesPage, ProfilePage
  src/components/  NavBar, RequireAuth
  src/context/      AuthContext (JWT storage + session restore)
  src/api/          Thin fetch wrapper (client.js)

db/         Phase 2 database schemas (auth_db, games_db, reviews_db)
services/   Phase 2 microservices (v2) - see services/README.md
```

## Running the backend

Requires a MySQL-compatible database. Two ways to run it:

### Option A — IntelliJ / plain Maven

Set these environment variables (in your IntelliJ run configuration, or
your shell) before starting:

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:mysql://w2-vmistserver.ad.psu.edu:3306/gameflix` |
| `DB_USERNAME` | DB username | `arp14` |
| `DB_PASSWORD` | DB password | *(required, no default)* |
| `JWT_SECRET` | HMAC signing secret, 32+ chars | *(required, no default)* |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `3600000` (1 hour) |

Then run `GameflixApplication`, or `mvn spring-boot:run` from the repo root.
The app listens on `localhost:8080`.

### Option B — Docker Compose (includes its own MySQL, no external DB needed)

```
cp .env.example .env   # then fill in real values
docker compose up
```

This starts a local MySQL container plus the app, wired together with a
healthcheck so the app waits for the database to be ready.

## Running the frontend

In a separate terminal, with the backend already running on port 8080:

```
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (typically `http://localhost:5173`). The dev
server proxies `/api/*` requests to `localhost:8080`, so no CORS setup is
needed.

## API endpoints

| Method | Path | Auth required | Notes |
|---|---|---|---|
| POST | `/api/users` | No | Register (username, email, displayName, password) |
| POST | `/api/sessions` | No | Log in, returns a JWT |
| GET | `/api/me` | Yes | Current user's profile |
| GET | `/api/games` | No | List all games |
| POST | `/api/games` | Yes | Create a game |
| PUT | `/api/games/{id}` | Yes | Update a game's title/genre |
| DELETE | `/api/games/{id}` | Yes | Delete a game |
| GET | `/api/reviews?gameId={id}` | No | List reviews for a game |
| POST | `/api/reviews` | Yes | Post a review (`gameId`, `rating` 1-5, `comment`) |

"Auth required" means a `Authorization: Bearer <token>` header with a
valid, unexpired JWT obtained from `POST /api/sessions`.

## Assignment steps completed

| Step | Topic | Where to look |
|---|---|---|
| 5 | JWT issuance/validation, protected endpoint | `security/`, [JWT_ANALYSIS.md](JWT_ANALYSIS.md) |
| 6 | Dockerize (multi-stage build, Compose, no hardcoded creds) | `Dockerfile`, `docker-compose.yml` |
| 7 | CI/CD (build/test/image/tag/stub deploy) | `.github/workflows/deploy.yml`, `deploy-local.sh` |
| 8 | Supply-chain audit (SBOM + vuln scan) & CRA incident memo | [CRA_INCIDENT_MEMO.md](CRA_INCIDENT_MEMO.md), `sbom.cdx.json`, `trivy-report.*` |
| 9 | Backend unit tests | `src/test/java/com/gameflix/` |

Beyond the original assignment scope: a React frontend (auth flow, games
list with full CRUD for logged-in users, a profile page, and per-game
reviews) was added on top, described above — plus a full microservices
version (`services/`), covered separately.

## Environment variables reference

See `.env.example` for the full list used by Docker Compose. The backend
itself only ever reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`,
and `JWT_EXPIRATION_MS` — none of them are hardcoded, and `JWT_SECRET` /
`DB_PASSWORD` have no defaults, so the app fails fast at startup if they're
missing rather than silently running insecurely.
