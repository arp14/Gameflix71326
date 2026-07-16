# JWT Analysis — GameFlix Login

## A real token issued by this implementation

```
eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqd3R1c2VyIiwidXNlcklkIjoxLCJpYXQiOjE3ODQwNjg3MjYsImV4cCI6MTc4NDA2ODczMX0.hWDjaja7iTpAEPbMG61k26VyiLDklXwweRTqCmMsjyJMrU8yEpQec4wtHlkvEVDf
```

A JWT is three base64url-encoded segments separated by `.`: **header.payload.signature**.

### Header (base64-decoded, no secret required)

```json
{"alg":"HS384"}
```

Just the signing algorithm. (JJWT picked HS384 automatically based on the secret's byte length — a longer key unlocks a stronger HMAC variant.)

### Payload / claims (base64-decoded, no secret required)

```json
{"sub":"jwtuser","userId":1,"iat":1784068726,"exp":1784068731}
```

- `sub` — subject (the username)
- `userId` — a custom claim added by `JwtService`
- `iat` — issued-at (Unix seconds)
- `exp` — expiry (Unix seconds); the filter (`JwtAuthFilter`) rejects the token once `exp` has passed

### Signature

```
HMACSHA384(base64url(header) + "." + base64url(payload), secretKey)
```

A MAC (Message Authentication Code) over the header and payload, computed with the server's secret key. Anyone can *recompute* this if they know the secret; nobody can forge a valid one without it.

## What a JWT does and does NOT protect

**Does protect:** integrity and authenticity of the claims. If a client tampers with the payload (e.g., changes `userId` from `1` to `2` to impersonate another user), the signature no longer matches what `JwtAuthFilter` recomputes during `parseAndValidate`, and the request is rejected with 401. This is what we verified directly: a token signed with one secret fails verification against a service holding a different secret (`JwtServiceTest.rejectsTokenSignedWithADifferentSecret`), and a garbage string fails outright (`rejectsGarbageToken`).

**Does NOT protect:** confidentiality of the payload. **A JWT is signed, not encrypted.** Base64 is an encoding, not a cipher — anyone holding the token (a browser extension, a proxy log, a coworker glancing at devtools) can decode the payload with zero effort and no secret, exactly as done above with plain `base64 -d`. This is precisely why:

- `RegisterRequest`/`credentials.password_hash` are never put in a JWT claim — a password hash sitting in a claim would be exposed to every party the token passes through, not just the server.
- The claims here are limited to non-sensitive identifiers (`username`, `userId`) — nothing an attacker could weaponize just by *reading* the token, only by trying to *forge* one.

A JWT also does not protect against replay within its validity window: if a valid, unexpired token is stolen (e.g., via XSS or a compromised network), it can be reused by the attacker until it expires. That's what the expiry (`exp`) is for — it bounds the damage window rather than eliminating it. (Production systems typically add refresh-token rotation and/or a revocation list on top of this; this assignment's scope is expiry-bounded access tokens only.)

## Why the signing secret must be an environment variable, not hard-coded

The signature is the *entire* security guarantee of a JWT — whoever holds the signing secret can mint tokens claiming to be **any** user (set `userId` to anything, sign it, done). If the secret is a string literal in source code:

1. It ships to everyone who can read the repository — including this repo's public GitHub history, forever, even if later "fixed" (old commits still have it).
2. It's identical across every environment (dev/test/prod) unless someone remembers to hand-edit the source per environment, which defeats the purpose and is easy to forget.
3. Rotating a compromised secret means a code change and redeploy, instead of just updating a config value.

This codebase reads it via `${jwt.secret}` → `JWT_SECRET` environment variable with **no default** (`src/main/resources/application.properties`), so the app fails to start rather than silently running with a guessable secret. `Keys.hmacShaKeyFor(...)` additionally throws at startup if the provided secret is too short for HMAC-SHA (under 256 bits / 32 characters), so a weak secret fails loudly instead of producing a token that's crackable by brute force.
