# games-service (not yet implemented)

Owns `games_db` (see `../../db/games_db.sql`). Will expose:

- `GET /api/games` — list all games, no auth required
- `POST /api/games` — create a game, requires a valid JWT
- `PUT /api/games/{id}` — update a game, requires a valid JWT
- `DELETE /api/games/{id}` — delete a game, requires a valid JWT

This is a direct extraction of `GameController`/`GameService`/`Game`
from the monolith at the repo root, plus its own copy of the
`JwtService`/`JwtAuthFilter` pattern (same `JWT_SECRET`). First service
to be built in Phase 2, since nothing else depends on it yet.

Planned port: **8082**. See `../README.md` for the overall Phase 2 plan.
