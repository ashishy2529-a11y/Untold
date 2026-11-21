# API contract — initial draft

Base path: `/api`. JSON requests and responses use UTF-8. This contract is a design target; only `GET /api/health` exists in the current backend.

## Health
- `GET /api/health` — returns service status and timestamp.

## Authentication (planned)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/session`

Use generic authentication errors to avoid account enumeration. Do not return password hashes, session secrets or internal identifiers unnecessarily.

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
- Use a stable error object with a safe human-readable message and correlation ID. Never return stack traces or sensitive values.

## Security requirements
All endpoints require server-side validation, authorization, rate limiting where appropriate, safe logging and automated tests. Private object endpoints must include cross-account access tests.
