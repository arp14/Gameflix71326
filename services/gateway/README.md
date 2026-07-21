# gateway

An nginx reverse proxy on port **8080** — the same port the monolith
uses — so the frontend can point at this whole microservices stack
without knowing (or caring) which service owns which path.

Chosen over Spring Cloud Gateway deliberately: this job is "forward
this path to that service," and a small nginx config does that with no
JVM to build, run, or maintain, versus pulling in an entire reactive-stack
Spring Boot module for the same result.

## Routing

| Path | Routes to |
|---|---|
| `/api/users` | `auth-service` |
| `/api/sessions` | `auth-service` |
| `/api/me` | `auth-service` |
| `/api/games`, `/api/games/{id}` | `games-service` |
| `/api/reviews` | `reviews-service` |

See `nginx.conf` — each route is a separate `location` block with
`proxy_pass` to the target service's container DNS name (e.g.
`http://auth-service:8081`), which only resolves inside the Docker
network these services share.

## Verified

Every route tested directly through port 8080 against all three
running services (register, login, `/api/me`, create/list games,
post/list reviews — correct responses and status codes throughout).
Then, more importantly: the actual React frontend (`../../frontend`),
completely unmodified, pointed at this gateway instead of the monolith
and driven through a real browser — register, login, the profile page,
browsing an existing game, and adding a new game through the UI all
worked. That's the actual point of building this: proving the
microservices stack is a genuine drop-in replacement for the monolith
from the frontend's perspective, not just a claim.
