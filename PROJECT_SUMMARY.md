# GameFlix — Detailed Project Summary

A running account of everything built in this project: the graded Spring
Boot assignment (Module 4–6 steps 5–9), and the React frontend built on
top of it afterward. Written to be a complete, standalone record — no
need to cross-reference chat history to understand what exists and why.

---

## Part 1: The graded assignment (Steps 5–9)

### Step 5 — JWT authentication (Module 4, 20 pts) — DONE

- `POST /api/users` registers a user (username, email, displayName,
  password). Passwords are hashed with BCrypt via `spring-security-crypto`
  — deliberately **not** the full `spring-boot-starter-security`, which
  would auto-configure a filter chain and 401 every endpoint by default.
- `POST /api/sessions` logs in and, on success, issues a JWT
  (`JwtService`, using `io.jsonwebtoken`/jjwt, HMAC-signed with a key
  derived from the `JWT_SECRET` env var — HS384 is auto-selected based on
  key length).
- `GET /api/me` is the protected endpoint: a custom `JwtAuthFilter`
  (`OncePerRequestFilter`) validates the `Authorization: Bearer <token>`
  header and attaches `userId`/`username` as request attributes. It's
  registered via a `FilterRegistrationBean` scoped to specific URL
  patterns only — not applied globally — so unrelated endpoints aren't
  affected.
- `JWT_ANALYSIS.md` is the written analysis: a real decoded token example
  (header `{"alg":"HS384"}`, payload with `sub`/`userId`/`iat`/`exp`),
  explains that JWTs are signed, not encrypted (anyone can base64-decode
  the payload — it protects integrity/authenticity, not confidentiality),
  and why the signing secret must be an environment variable rather than
  hardcoded (anyone with the secret can forge valid tokens).

### Step 6 — Dockerize (Module 5, 20 pts) — DONE, verified live

- `Dockerfile`: multi-stage build — a `maven:3.9-eclipse-temurin-17`
  stage compiles the jar, then it's copied into a slim
  `eclipse-temurin:17-jre-alpine` runtime stage (no build tooling shipped
  in the final image).
- `docker-compose.yml`: a `mysql:8.0` service (credentials from `.env`,
  never hardcoded) with a healthcheck, and the `app` service with
  `depends_on: condition: service_healthy` so it doesn't race MySQL's
  startup; a named volume for MySQL data persistence.
- `.env.example` is the template (`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`,
  `MYSQL_ROOT_PASSWORD`, `JWT_SECRET`) — `.env` itself is gitignored.
- Verified end-to-end by running it live: Hibernate created the schema,
  the app connected to the containerized MySQL, and `GET /api/games`
  returned 200.

### Step 7 — CI/CD (Module 5, 20 pts) — DONE, verified green

- `.github/workflows/deploy.yml`: on every push, runs the test suite,
  builds the jar, builds a Docker image, tags it with both the commit SHA
  and `latest`, pushes to GHCR (GitHub Container Registry, auth via the
  automatic `secrets.GITHUB_TOKEN`), and runs a stub deploy step.
- The workflow spins up an **ephemeral `mysql:8.0` service container**
  scoped to the job so `GameflixApplicationTests.contextLoads()` (which
  boots a full Spring context needing a real DB) has something to connect
  to — GitHub's hosted runners have no network path to the real
  `w2-vmistserver.ad.psu.edu`. These throwaway CI-only credentials live
  only for the ~1 minute job lifetime, unlike `secrets.GITHUB_TOKEN`,
  which is a real, meaningful secret handled properly via GitHub Actions
  secrets.
- Image names are lowercased explicitly in the workflow (`arp14/Gameflix71326`
  has a capital G; Docker image names must be all-lowercase).
- `deploy-local.sh` is the equivalent shell-script fallback for anyone
  who can't use GitHub Actions: test → build jar → Docker build tagged
  with `git rev-parse HEAD` → stub deploy echo.
- First CI run failed exactly as expected (no network path to the real
  DB); the fix (ephemeral MySQL service container) was pushed and the
  second run passed completely — confirmed via the GitHub Actions API.

### Step 8 — Supply-chain audit & CRA incident memo (Module 5, 20 pts) — DONE

- Generated a CycloneDX SBOM (`sbom.cdx.json`) with **Syft**, scanning the
  built jar directly — 55 components catalogued (Spring, Hibernate,
  Jackson, Tomcat, MySQL connector, jjwt, logging libraries, etc.), not
  just what's declared in `pom.xml`.
- Scanned it with **Trivy** (`trivy-report.json`/`.txt`): found 55 known
  vulnerabilities (4 CRITICAL / 21 HIGH / 18 MEDIUM / 12 LOW), all in
  third-party dependencies, none in GameFlix's own code.
- Remediated what was safely fixable: bumped `spring-boot-starter-parent`
  `3.3.2` → `3.3.13` (same minor line, no breaking changes — full test
  suite re-run to confirm). That alone cut the count to 34 (3 CRITICAL /
  14 HIGH / 9 MEDIUM / 8 LOW).
- `CRA_INCIDENT_MEMO.md` documents all of this, plus:
  - A **simulated CRA (Cyber Resilience Act) Article 14 incident report**
    for the worst remaining finding (a Tomcat digest-auth bypass),
    walking through the real 24-hour/72-hour/14-day disclosure timeline a
    CRA-covered manufacturer would follow.
  - An **exploitability analysis**: checked each remaining
    CRITICAL/HIGH finding against GameFlix's actual configuration and
    code (no HTTP/2 enabled, no Tomcat-managed auth, no `web.xml`
    security constraints, no Jackson default typing anywhere in the
    codebase) and concluded none of them are currently reachable — a
    materially different, more precise claim than "no critical
    vulnerabilities," since the vulnerable code still ships in the jar
    and could become reachable if the app's configuration changes later.
- Also fixed a pre-existing typo in `.env.example` (`DDB_NAME` and
  leading spaces from a earlier retyping) as a drive-by.

### Step 9 — Backend unit test (Module 6, 10 pts) — DONE

- `JwtServiceTest`: 4 tests covering valid claims/format, expired-token
  rejection, wrong-secret rejection, and garbage-token rejection.
- `UserServiceTest`: 3 tests for `UserService.login()` — happy path,
  missing-credential-record edge case, wrong-password failure — using
  `@ExtendWith(MockitoExtension.class)` with mocked repositories/encoder/
  JWT service, no Spring context or real DB needed.
- Verified non-vacuous by temporarily mutating `login()` to skip the
  password check: 2 tests failed with real assertion errors and Mockito
  flagged an unused stub on the third, confirming the tests actually
  exercise the logic rather than passing trivially.

---

## Part 2: React frontend (built after the assignment steps, in phases)

Not part of the graded assignment, but built on top of it once steps 5–9
were complete. Vite + React in `frontend/`, talking to the existing
backend via same-origin `/api/...` calls (the dev server proxies `/api/*`
to `localhost:8080`, so no CORS configuration was needed on the backend).

### Phase 1, step 1 — Scaffold

- `react-router-dom` routing to Home/Games/Login/Register pages, a
  `NavBar`, and a thin `api/client.js` fetch wrapper that stores the JWT
  in `localStorage` and attaches it as a Bearer header automatically.

### Phase 1, step 2 — Login/Register wired to the real backend

- `AuthContext` holds the logged-in user, persists the JWT, and restores
  the session on page load via `GET /api/me`.
- Real forms against `/api/sessions` and `/api/users`, with error
  handling for wrong password (401, `message` field) and duplicate
  username (409, `error` field — a different response shape, handled
  explicitly in the API client).
- `NavBar` reflects logged-in state with a logout control.
- Verified end-to-end with a local MariaDB instance and a scripted
  Playwright browser run: register → redirect to login → login → nav bar
  updates → session persists across reload → logout clears it → wrong
  password shows the right error.

### Phase 1, step 3 — Games page

- Lists games from `GET /api/games` (open to everyone); logged-in users
  get an "add a game" form (`POST /api/games`).
- At this point the "logged-in only" gate was UI-only — the backend
  endpoint itself had no auth check yet.

### Backend hardening — require a JWT to create a game

- Closed the gap above: extended `JwtAuthFilter`/`JwtFilterConfig` to
  cover `/api/games` too, but with a `shouldNotFilter` exemption so `GET
  /api/games` (browsing) stays open to anonymous users — only `POST` now
  requires a valid token.
- Verified directly: `GET` with no token still 200s, `POST` with no
  token now 401s, `POST` with a valid token still 201s, `GET /api/me` is
  unaffected. Also re-ran the full Playwright flow to confirm no
  regression through the actual UI.

### Profile page + edit/delete for games

- Backend: added `PUT /api/games/{id}` and `DELETE /api/games/{id}`
  (404 if the id doesn't exist), both covered by the same JWT
  requirement as creation.
- Frontend: a `/profile` route showing real data from `GET /api/me`,
  protected by a new `RequireAuth` wrapper — the first **route-level**
  protection in the frontend (redirects to `/login` if not authenticated,
  rather than just hiding UI). `GamesPage` gained inline Edit (toggles a
  form, calls `PUT`) and Delete (confirms, calls `DELETE`) controls for
  logged-in users, with a stable `data-game-id` attribute on each list
  item for reliable targeting.
- Verified end-to-end via Playwright: logged-out visit to `/profile`
  redirects to `/login`; logged in, it shows the real
  username/email/display name; adding, renaming, and deleting a game all
  reflect immediately in the list; logged-out users see no Edit/Delete
  buttons at all.

### Project README

- Added a root `README.md` covering setup for both backend (IntelliJ or
  Docker Compose) and frontend (`npm run dev`), the full API endpoint
  table, and a mapping from assignment steps 5–9 to the files that
  satisfy them.

---

## Current state

Everything above is committed. Steps 5–9 of the assignment are complete
and verified; the React frontend covers register/login, session
persistence, browsing games, full CRUD on games for logged-in users (both
UI-gated and now server-enforced), and a profile page.

**In progress:** Phase 2 — converting the monolith into microservices,
built alongside it in `services/` without touching the working monolith
at the repo root at all. Decisions locked in: three services (Auth,
Games, Reviews — Reviews replacing the earlier "hotel" domain from the
original conceptual ERD discussion, which was never actually
implemented), one MySQL server with three separate schemas rather than
three separate DB instances, and no cross-service foreign keys (a
service calls another service's API instead of querying its database
directly).

Done so far:
- **Schema design** (`db/auth_db.sql`, `games_db.sql`, `reviews_db.sql`,
  `db/README.md`) — verified directly against MariaDB, including that
  the constraints (foreign key, rating CHECK, unique-review-per-user)
  actually reject bad data, not just that the DDL runs.
- **`services/` skeleton** — one folder per service, each with a README
  describing its planned responsibility, owned schema, and port
  (Auth 8081, Games 8082, Reviews 8083, with a future gateway on 8080 so
  the frontend's existing `/api/*` proxy target doesn't need to change).

Not yet built: any actual service code. Games service is next — planned
as the first extraction since nothing else depends on it, to be verified
working standalone before moving on to Auth and then Reviews.
