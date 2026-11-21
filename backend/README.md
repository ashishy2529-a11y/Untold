# UNTOLD API

Java 21 + Spring Boot 3.4.

## Local development
1. Start MySQL 8 and create a database named `untold`.
2. Generate a dedicated 32-byte AES key and Base64-encode it. For example, generate 32 random bytes with a password manager or secure random generator; never reuse the CI example key.
3. Set environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/untold`
   - `DB_USERNAME=your_local_user`
   - `DB_PASSWORD=your_local_password`
   - `COOKIE_SECURE=false` for local HTTP only
   - `CORS_ALLOWED_ORIGIN=http://localhost:5173`
   - `UNTOLD_DATA_ENCRYPTION_KEY=<base64-encoded 32-byte key>`
4. Run `mvn spring-boot:run` from this directory.

Flyway creates the account and private-writing tables. The older root `database/schema.sql` is a design draft; do not apply it manually to a fresh database alongside Flyway.

## Authentication endpoints
- `GET /api/auth/csrf` — obtains a CSRF token and sets the XSRF cookie.
- `POST /api/auth/register` — JSON: `{"email":"you@example.com","password":"at-least-12-characters"}`.
- `POST /api/auth/login` — same JSON shape; creates a server-side session.
- `GET /api/auth/me` — authenticated account email.
- `POST /api/auth/logout` — invalidates the session.

## Private writing endpoints
Use `/api/private-writing/void` for THE VOID and `/api/private-writing/unsent` for THE UNSENT:
- `POST /api/private-writing/{type}` — create with `{"title":"...","content":"..."}`.
- `GET /api/private-writing/{type}` — list only the signed-in user's records.
- `GET /api/private-writing/{type}/{id}` — read an owned record.
- `PUT /api/private-writing/{type}/{id}` — update an owned record.
- `DELETE /api/private-writing/{type}/{id}` — delete an owned record.

Private content is encrypted by the API using AES-256-GCM with a random per-record IV. The key is supplied only through `UNTOLD_DATA_ENCRYPTION_KEY`; losing it makes existing content unreadable. Back it up securely and never commit a production key. Ownership is enforced by querying with both record ID and authenticated account ID.

For state-changing requests, send cookies and the `X-XSRF-TOKEN` header using the token returned by `/api/auth/csrf`. Browser requests must include credentials. In production, use HTTPS and set `COOKIE_SECURE=true`.

## Important limits before public launch
This is an early development foundation, not a production security certification. Email verification, password reset, login throttling/rate limiting, account recovery, key rotation and recovery planning, security monitoring, deployment secret management, full integration tests and independent security review remain required. The prototype community feed is not live or moderated. Do not use the service for emergency support.
