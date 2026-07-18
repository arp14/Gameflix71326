# auth-service (not yet implemented)

Owns `auth_db` (see `../../db/auth_db.sql`). Will expose:

- `POST /api/users` — register (username, email, displayName, password;
  BCrypt-hashed)
- `POST /api/sessions` — log in, returns a JWT
- `GET /api/me` — current user's profile, requires a valid JWT

Direct extraction of `AuthController`/`ProfileController`/`UserService`/
`User`/`Credential` from the monolith at the repo root, plus its own
`JwtService` (the one that actually *issues* tokens — the other two
services only *validate* them, using the same shared `JWT_SECRET`).

Planned port: **8081**. See `../README.md` for the overall Phase 2 plan.
