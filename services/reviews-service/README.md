# reviews-service (not yet implemented)

Owns `reviews_db` (see `../../db/reviews_db.sql`). Will expose:

- `GET /api/reviews?gameId={id}` — list reviews for a game, no auth
  required
- `POST /api/reviews` — post a review, requires a valid JWT

The only genuinely new service — nothing to extract from the monolith,
since reviews don't exist there yet. `POST /api/reviews` will call
`games-service`'s `GET /api/games/{id}` synchronously to confirm the
game exists before saving (see `../../db/README.md` for why `game_id`
isn't a foreign key, and why that check has to happen in code instead).
`user_id` is taken from the JWT claims directly, no separate call to
auth-service needed.

Planned port: **8083**. See `../README.md` for the overall Phase 2 plan.
