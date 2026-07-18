# GameflixMicro database design (Phase 2)

Three schemas, one per planned microservice: `auth_db`, `games_db`,
`reviews_db`. Run against a single MySQL server (see the deployment note
below), but treat them as fully separate databases — no service connects
to any schema but its own.

## Files

- `auth_db.sql` — `users` + `credentials`, owned by the Auth service
- `games_db.sql` — `games`, owned by the Games service
- `reviews_db.sql` — `reviews`, owned by the Reviews service

Apply all three the same way, e.g.:
```
mysql -u root < db/auth_db.sql
mysql -u root < db/games_db.sql
mysql -u root < db/reviews_db.sql
```

## Why one MySQL server instead of three

A "pure" microservices deployment would give each service its own
database *instance* for full failure isolation and independent scaling.
For this project, one server with three schemas is a deliberate,
documented compromise: it keeps the principle that actually matters here
— each service only ever reads/writes its own schema, never another's —
without tripling the operational overhead (three MySQL containers, three
sets of credentials, three healthchecks) for a project this size. If this
were a production system, the schemas would be the natural seam to split
onto separate servers later, since nothing depends on them being
co-located.

## Why `reviews.game_id` and `reviews.user_id` aren't foreign keys

They point at rows in `games_db` and `auth_db` — different databases the
Reviews service has no direct access to. MySQL can't enforce a foreign
key across databases in this setup (and shouldn't be asked to: that would
recreate exactly the tight coupling splitting these services apart is
meant to avoid). Referential integrity is enforced in application code
instead:

- **`game_id`**: the Reviews service calls the Games service's API
  (`GET /api/games/{id}`) to confirm the game exists before saving a
  review.
- **`user_id`**: not re-validated at all — it comes from an already-signed,
  already-verified JWT claim, which is a stronger integrity guarantee
  than a database lookup would give anyway (a row existing in `users`
  doesn't prove the request is really from that user; a valid signed
  token does).

## Other design choices worth knowing about

- **`reviews` has a UNIQUE constraint on `(game_id, user_id)`** — one
  review per user per game. This is a product decision, not a technical
  requirement; drop the constraint if you'd rather allow a user to leave
  multiple reviews over time.
- **`rating` has a CHECK constraint (1–5)** — enforced at the database
  level in addition to whatever validation the service layer does, so
  invalid data can't sneak in through any path that isn't the API (a
  manual `INSERT`, a future second service writing to the same table,
  etc).
