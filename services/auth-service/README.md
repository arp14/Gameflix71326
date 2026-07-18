# auth-service

Owns `auth_db` (see `../../db/auth_db.sql`). The only service that
*issues* JWTs — `games-service` and `reviews-service` only validate
them, sharing this service's `JWT_SECRET`.

## Endpoints

| Method | Path | Auth required | Notes |
|---|---|---|---|
| POST | `/api/users` | No | Register (username, email, displayName, password; BCrypt-hashed) |
| POST | `/api/sessions` | No | Log in, returns a JWT |
| GET | `/api/me` | Yes | Current user's profile |

## Running standalone

```
export DB_URL=jdbc:mysql://localhost:3306/auth_db
export DB_USERNAME=<your db user>
export DB_PASSWORD=<your db password>
export JWT_SECRET=<32+ char shared secret>
mvn spring-boot:run
```

Listens on port **8081** by default (override with `SERVER_PORT`).

## Running via Docker Compose

From `services/`:
```
cp .env.example .env   # fill in real values
docker compose up
```
Starts `auth-service` plus `games-service`, each with its own MySQL
container (`auth_db` and `games_db` respectively — fully separate from
each other and from the monolith's database).

## Verified

Directly against a running instance: register, duplicate-username
rejection (409), wrong-password rejection (401), successful login
returns a valid token, `/api/me` requires that token (401 without one or
with a garbage one, 200 with a valid one). Also verified the actual
point of this whole design: a token issued here was used directly
against a separately-running `games-service` instance and accepted,
proving the shared-secret validation works across services with zero
coordination beyond the secret itself.
