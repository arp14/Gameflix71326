# GameFlix microservices (Phase 2 / "v2")

This directory is the microservices version of GameFlix, built
**alongside** the working monolith at the repo root — not a replacement
for it. The root `pom.xml`/`src/`/`Dockerfile`/`docker-compose.yml` are
"v1" and stay exactly as they are, still graded, still working, still CI-
built by `.github/workflows/deploy.yml`. Nothing under `services/` can
break that: each service here is a fully standalone Maven project with
its own `pom.xml`, so building or running anything in this directory
never touches the root project's build.

## Status

Skeleton only right now — folders and this documentation, no code yet.
Each service will be filled in one at a time, starting with
`games-service` (lowest risk: nothing else depends on it), verified
working on its own before moving to the next.

## Services

| Service | Owns (schema) | Responsibility | Planned port |
|---|---|---|---|
| `auth-service` | `auth_db` | Register, log in, issue/validate JWTs, `/api/me` | 8081 |
| `games-service` | `games_db` | Games catalog CRUD | 8082 |
| `reviews-service` | `reviews_db` | Post/list reviews; calls `games-service` to confirm a game exists before saving one | 8083 |

A gateway (not yet built) will sit in front of all three on **port
8080** — the same port the monolith uses today — so the frontend's
existing `/api/*` proxy target doesn't need to change at all when
pointed at this stack instead. (Run one stack or the other, not both at
once — they'd collide on 8080.)

## Shared conventions across services

- **JWT validation**: each service gets its own copy of the
  `JwtService`/`JwtAuthFilter` pattern from the monolith (same shared
  `JWT_SECRET` env var), not a shared library — see the Phase 2 plan
  discussion for why duplication is preferred here over a shared
  dependency between services.
- **No cross-service database access**: each service only ever connects
  to its own schema (see `../db/README.md`). Any data a service needs
  from another service's domain comes from calling that service's API,
  never a direct DB query.
- **Database schemas**: already designed and verified in `../db/` (step
  1 of this phase) — `auth_db.sql`, `games_db.sql`, `reviews_db.sql`.

## Running this stack (once built)

```
cd services
docker compose up
```

(Compose file not yet added — comes with the first working service.)
