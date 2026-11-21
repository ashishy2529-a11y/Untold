# Architecture

## Initial stack
- Frontend: React 18, Vite, JavaScript and CSS.
- API: Java 21, Spring Boot 3.4, REST, Bean Validation.
- Database target: MySQL 8+.
- API and frontend are separate deployable components.

## Logical layers
1. Browser UI: presentation, accessible controls and client-side validation.
2. API: authentication, authorization, validation, rate limits and business rules.
3. Domain services: journal, letters, community, moderation, consent, experiments and scheduling.
4. Persistence: repositories and migrations.
5. Operations: secrets, logs without private content, backups, monitoring and incident response.

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

## Current implementation
The API currently exposes only `GET /api/health`. The frontend is a UI prototype and stores demo notes in browser local storage. No real account system, server persistence, feed service or secure private-writing workflow is implemented yet.
