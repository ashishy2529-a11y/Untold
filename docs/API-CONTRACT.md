# API contract — current implementation and planned endpoints

Base path: `/api`. JSON requests and responses use UTF-8.

## Health
- `GET /api/health` — returns service status and timestamp.

## Authentication (implemented foundation)
- `GET /api/auth/csrf` — returns a CSRF token and sets the XSRF cookie.
- `POST /api/auth/register` — accepts `email` and a password of 12–72 characters; stores a BCrypt hash.
- `POST /api/auth/login` — authenticates and creates a server-side session.
- `GET /api/auth/me` — returns the signed-in account email.
- `POST /api/auth/logout` — invalidates the server-side session.

State-changing requests must include the CSRF token in `X-XSRF-TOKEN` and send cookies. Browser clients must use credentialed requests. Production must use HTTPS and set `COOKIE_SECURE=true`.

## Private expression (planned)
- `POST /api/journal`
- `GET /api/journal`
- `GET /api/journal/{id}`
- `PATCH /api/journal/{id}`
- `DELETE /api/journal/{id}`
- `POST /api/letters`
- `GET /api/letters`
- `DELETE /api/letters/{id}`

Every operation must derive the owner from the authenticated server-side principal. Never accept an owner ID from the browser as authorization.

## Community and safety (planned)
- `GET /api/community/feed`
- `POST /api/community/posts`
- `POST /api/reports`
- `POST /api/blocks/{userId}`
- `DELETE /api/blocks/{userId}`

Publishing must enter moderation according to configured policy. Anonymous public responses must not expose email, account IDs or private metadata.

## Response conventions
- 2xx for successful operations.
- 400 for invalid input, 401 for unauthenticated requests, 403 for forbidden actions, 404 for missing or inaccessible resources, 409 for state conflicts, 429 for rate limits.
- Use safe human-readable error messages and correlation IDs. Never return stack traces or sensitive values.

## Security requirements
All endpoints require server-side validation, authorization, rate limiting where appropriate, safe logging and automated tests. Private object endpoints must include cross-account access tests. Authentication is not complete for public launch until account recovery, rate limiting, monitoring and security review are added.
