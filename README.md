# UNTOLD
**Every story has a place.**

UNTOLD is a privacy-conscious space for private expression, anonymous community and reflection. It is designed around consent, user control and safety—not engagement at any cost.

## Project status
This repository contains the initial application scaffold:
- React + Vite frontend with a responsive UNTOLD dashboard and working local prototype interactions.
- Spring Boot backend starter with health endpoint and API foundation.
- MySQL schema draft and product/security documentation.

**Important:** The current frontend stores prototype entries in browser local storage. It is not a secure private journal service and must not be used for sensitive or real personal writing. The backend is a starter, not production authentication or a production-ready moderation system.

## Repository layout
- `frontend/` — React application
- `backend/` — Java 21 / Spring Boot API starter
- `database/` — MySQL schema draft
- `docs/` — product, architecture, privacy and development notes

## Run locally

### Frontend
Requires Node.js 20.19+ or 22.12+.
```bash
cd frontend
npm install
npm run dev
```

### Backend
Requires Java 21 and Maven.
```bash
cd backend
mvn spring-boot:run
```
Health check: `GET http://localhost:8080/api/health`

### Database
Review `database/schema.sql` and configure a local MySQL database before enabling persistence. Never commit credentials.

## Product principles
1. Private by default; publishing is an explicit action.
2. Anonymous to other members does not mean technically untraceable.
3. No sale of private writing and no ad targeting based on it.
4. No diagnosis or claims to provide therapy.
5. Reporting, blocking, moderation, deletion and appeals are core product requirements.
6. Human experiments are voluntary and transparently described.

## Planned first release
1. Account and secure session foundation.
2. THE VOID.
3. THE UNSENT.
4. SOMEONE, SOMEWHERE.
5. Reporting, blocking and moderator workflows.

See [Product specification](docs/PRODUCT.md), [Architecture](docs/ARCHITECTURE.md), [Privacy and safety](docs/PRIVACY-AND-SAFETY.md), [API contract](docs/API-CONTRACT.md), and [Development plan](docs/ROADMAP.md).
