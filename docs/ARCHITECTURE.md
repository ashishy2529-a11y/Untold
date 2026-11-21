# Architecture

## Initial stack
- Frontend: React 18, Vite, JavaScript and CSS.
- API: Java 21, Spring Boot 3.4, REST, Spring Security and Bean Validation.
- Database: MySQL 8+ with Flyway migrations and Spring Data JPA.
- API and frontend are separate deployable components.

## Logical layers
1. Browser UI: presentation, accessible controls and client-side validation.
2. API: authentication, authorization, validation, rate limits and business rules.
3. Domain services: journal, letters, community, moderation, consent, experiments and scheduling.
4. Persistence: repositories and ordered database migrations.
5. Operations: secrets, logs without private content, backups, monitoring and incident response.

## Authentication foundation implemented
- Account registration and normalized email storage.
- BCrypt password hashing.
- Server-side HTTP sessions; the session cookie is HttpOnly and SameSite=Lax.
- CSRF cookie/token support for state-changing requests.
- Configurable cookie Secure flag and explicit CORS origin.
- Flyway migration for the account table.
- Auth endpoints: `GET /api/auth/csrf`, `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`.

## API families (planned)
- `/api/auth/*`
- `/api/journal/*`
- `/api/letters/*`
- `/api/community/*`
- `/api/discussions/*`
- `/api/reports/*`
- `/api/blocks/*`
- `/api/mirror/*`
- `/api/matches/*`
- `/api/experiments/*`
- `/api/time-capsules/*`
- `/api/admin/*`

## Authorization rule
Every private-object read, update and delete must authenticate the requester and enforce ownership on the server. Never trust a client-supplied owner ID or anonymous display name as proof of identity.

## Current limitations
Authentication is an initial foundation, not a production security certification. Email verification, password reset, login rate limiting, account recovery and a security review are still pending. The frontend remains a UI prototype and its demo notes are stored in browser local storage. There are no server-side journal/letter endpoints, encryption service, live community feed or moderation workflows yet. Do not enter real sensitive writing into the prototype.
