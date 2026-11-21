# Architecture

## Stack
- Frontend: React 18, Vite, JavaScript and CSS.
- API: Java 21, Spring Boot 3.4, Spring Security and Bean Validation.
- Database: MySQL 8+ with Flyway migrations and Spring Data JPA.
- Frontend and API are separate deployable components.

## Logical layers
1. Browser UI: presentation, accessible controls and client-side validation.
2. API: authentication, authorization, validation, rate limits and business rules.
3. Domain services: journal, letters, community, moderation, consent, experiments and scheduling.
4. Persistence: repositories and ordered database migrations.
5. Operations: secrets, logs without private content, backups, monitoring and incident response.

## Authentication implemented
- Account registration and normalized email storage.
- BCrypt password hashing.
- Server-side HTTP sessions; HttpOnly and SameSite=Lax session cookie.
- CSRF cookie/token support for state-changing requests.
- Configurable cookie Secure flag and explicit CORS origin.
- Protected current-account endpoint and logout.
- React sign-in/register interface connected using credentialed requests and CSRF header.

## Private writing implemented
- THE VOID and THE UNSENT share an account-owned private-writing service.
- Each record is encrypted with AES-256-GCM using a fresh random 96-bit IV and 128-bit authentication tag.
- Encryption key is injected from `UNTOLD_DATA_ENCRYPTION_KEY`; it is not stored in source code.
- Reads, updates and deletes are scoped by both record ID and authenticated owner ID.
- React My Space loads the signed-in account's private entries and supports deletion.

## Planned API families
- `/api/auth/*`
- `/api/private-writing/{void|unsent}/*`
- `/api/community/*`
- `/api/discussions/*`
- `/api/reports/*`
- `/api/blocks/*`
- `/api/mirror/*`
- `/api/matches/*`
- `/api/experiments/*`
- `/api/time-capsules/*`
- `/api/admin/*`

## Important limitations
This is a development milestone, not a production security certification. Email verification, password reset, login rate limiting, account recovery, encryption key rotation/recovery, account-wide export/deletion and independent security review remain pending. The sample community feed is not live or moderated. Deployment, real-device verification and production secrets configuration have not been completed. Never use this prototype as emergency support.
