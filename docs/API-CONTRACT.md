# API contract — current implementation and planned endpoints

Base path: `/api`. JSON requests and responses use UTF-8.

## Health
- `GET /api/health` — service status and timestamp.

## Authentication
- `GET /api/auth/csrf` — returns a CSRF token and sets the XSRF cookie.
- `POST /api/auth/register` — accepts email and a password of 12–72 characters; stores a BCrypt hash.
- `POST /api/auth/login` — authenticates and creates a server-side session.
- `GET /api/auth/me` — returns the signed-in account email.
- `POST /api/auth/logout` — invalidates the server-side session.

## Private writing
For THE VOID use `/api/private-writing/void`; for THE UNSENT use `/api/private-writing/unsent`.
- `POST /api/private-writing/{type}` — create with `{"title":"...","content":"..."}`.
- `GET /api/private-writing/{type}` — list the signed-in user's records.
- `GET /api/private-writing/{type}/{id}` — read an owned record.
- `PUT /api/private-writing/{type}/{id}` — update an owned record.
- `DELETE /api/private-writing/{type}/{id}` — delete an owned record.

The API encrypts content with AES-256-GCM before persistence. The encryption key is supplied via `UNTOLD_DATA_ENCRYPTION_KEY`; keep a protected backup. The application cannot decrypt old records if the key is lost. Every operation derives owner identity from the authenticated principal and scopes the database lookup by owner ID.

## Community and safety (planned)
- `GET /api/community/feed`
- `POST /api/community/posts`
- `POST /api/reports`
- `POST /api/blocks/{userId}`
- `DELETE /api/blocks/{userId}`

Community publishing must enter moderation according to configured policy. Anonymous public responses must not expose email, account IDs or private metadata.

## Client security requirements
State-changing requests must send cookies and `X-XSRF-TOKEN`. Browser clients must use credentialed requests. Production must use HTTPS and `COOKIE_SECURE=true`. CORS must be restricted to the exact frontend origin.

## Remaining launch requirements
Email verification, password recovery, rate limiting, audit/monitoring, encryption key rotation and recovery planning, account export/deletion, moderation operations and independent security review are not yet complete. Do not treat this development API as production-ready.
