# games-service

Owns `games_db` (see `../../db/games_db.sql`). First service extracted
in Phase 2 - a standalone Spring Boot module, own `pom.xml`, no
dependency on the monolith at the repo root.

## Endpoints

| Method | Path | Auth required | Notes |
|---|---|---|---|
| GET | `/api/games` | No | List all games |
| GET | `/api/games/{id}` | No | Get one game — added beyond what the monolith has, so `reviews-service` (and anonymous browsing) can look up a single game |
| POST | `/api/games` | Yes | Create a game |
| PUT | `/api/games/{id}` | Yes | Update a game's title/genre |
| DELETE | `/api/games/{id}` | Yes | Delete a game |

"Auth required" means an `Authorization: Bearer <token>` header with a
valid JWT signed by the same `JWT_SECRET` this service is configured
with — the token itself is issued by `auth-service` (not yet built);
this service only ever validates, never issues.

## Running standalone

```
export DB_URL=jdbc:mysql://localhost:3306/games_db
export DB_USERNAME=<your db user>
export DB_PASSWORD=<your db password>
export JWT_SECRET=<32+ char shared secret>
mvn spring-boot:run
```

Listens on port **8082** by default (override with `SERVER_PORT`).

## Running via Docker Compose

From `services/`:
```
cp .env.example .env   # fill in real values
docker compose up
```
This starts `games-service` plus its own MySQL container (`games_db`
only — completely separate from the monolith's database).

## Verified

Directly against a running instance: full CRUD works with a valid
token; `POST`/`PUT`/`DELETE` return 401 without one; `GET` (list and
by-id) stay open regardless; a nonexistent id returns 404 on
`GET`/`PUT`/`DELETE`; a garbage token returns 401.
