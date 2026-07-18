# reviews-service

Owns `reviews_db` (see `../../db/reviews_db.sql`). The only genuinely
new service in Phase 2 — nothing to extract from the monolith, since
reviews don't exist there.

## Endpoints

| Method | Path | Auth required | Notes |
|---|---|---|---|
| GET | `/api/reviews?gameId={id}` | No | List reviews for a game, newest first |
| POST | `/api/reviews` | Yes | Post a review (`gameId`, `rating` 1-5, `comment`) |

`userId` is never taken from the request body — it comes from the JWT
claims (same token `auth-service` issued), so a client can't post a
review as someone else.

## Why `POST` calls games-service

`game_id` isn't a foreign key here (it points at a row in `games_db`,
which this service has no access to — see `../../db/README.md`), so
before saving a review, this service calls `games-service`'s
`GET /api/games/{id}` to confirm the game actually exists. If
games-service is unreachable entirely (not just "game not found"), the
request fails with **503**, not a silent success — a review shouldn't
reference a game this service couldn't actually verify.

## Running standalone

```
export DB_URL=jdbc:mysql://localhost:3306/reviews_db
export DB_USERNAME=<your db user>
export DB_PASSWORD=<your db password>
export JWT_SECRET=<32+ char shared secret>
export GAMES_SERVICE_URL=http://localhost:8082   # or wherever games-service is running
mvn spring-boot:run
```

Listens on port **8083** by default (override with `SERVER_PORT`).
Requires `games-service` to actually be running and reachable at
`GAMES_SERVICE_URL` for `POST` to work.

## Running via Docker Compose

From `services/`:
```
cp .env.example .env   # fill in real values
docker compose up
```
Starts all three services (`auth-service`, `games-service`,
`reviews-service`) plus their own MySQL containers.
`GAMES_SERVICE_URL` is set automatically to the container network
address (`http://games-service:8082`).

## Verified

Full flow directly against running instances: registered/logged in via
`auth-service`, created a game via `games-service`, then — using that
same token — posted a review referencing it and it saved correctly.
Also checked: no token → 401; a nonexistent `gameId` → 404 (the live
call to games-service actually runs, not just trusted blindly); an
invalid rating (7) → 400; a second review from the same user for the
same game → 409 (the unique constraint, caught and translated to a
clean error); reads stay open with no token. Finally, stopped
games-service entirely and confirmed `POST` fails with 503 rather than
silently succeeding or crashing unhelpfully.
