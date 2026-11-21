# UNTOLD API

Java 21 + Spring Boot 3.4.

## Local development
1. Start MySQL 8 and create a database named `untold`.
2. Set environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/untold`
   - `DB_USERNAME=your_local_user`
   - `DB_PASSWORD=your_local_password`
   - `COOKIE_SECURE=false` for local HTTP only
   - `CORS_ALLOWED_ORIGIN=http://localhost:5173`
3. Run `mvn spring-boot:run` from this directory.

Flyway creates the initial account table. The older root `database/schema.sql` is a design draft; do not apply it manually to a fresh database alongside Flyway.

## Authentication endpoints
- `GET /api/auth/csrf` — obtains a CSRF token and sets the XSRF cookie.
- `POST /api/auth/register` — JSON: `{"email":"you@example.com","password":"at-least-12-characters"}`.
- `POST /api/auth/login` — same JSON shape; creates a server-side session.
- `GET /api/auth/me` — authenticated account email.
- `POST /api/auth/logout` — invalidates the session.

For state-changing requests, send cookies and the `X-XSRF-TOKEN` header using the token returned by `/api/auth/csrf`. Frontend requests must include credentials. In production, use HTTPS and set `COOKIE_SECURE=true`.

## Important limits before public launch
This is an initial authentication foundation, not a production security certification. Email verification, password reset, login throttling/rate limiting, account recovery, security monitoring, deployment secret management, integration tests and a security review remain required. No journal or letter API is connected yet. Do not submit real sensitive writing until server-side private storage, encryption, ownership tests and deletion flows are implemented and reviewed.
