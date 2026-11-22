# UNTOLD
**Every story has a place.**

UNTOLD is a privacy-conscious space for private expression, anonymous community and reflection. It is designed around consent, user control and safety—not engagement at any cost.

## Current development status

The repository now includes:
- Responsive React + Vite dashboard with account registration and sign-in UI.
- Spring Boot API with BCrypt password hashing, server-side sessions and CSRF protection.
- MySQL persistence managed by Flyway migrations.
- THE VOID and THE UNSENT create, list, read, update and delete API.
- AES-256-GCM encryption for private writing before it is stored in MySQL.
- Server-side ownership checks; one account cannot read, update or delete another account's private entries.
- My Space UI for viewing and deleting account-owned writing.
- GitHub Actions workflows for frontend build and backend integration tests.

The community submission, report, block and moderator decision APIs are implemented, and the frontend submits stories for human review. The moderation queue currently has no frontend dashboard; the first moderator must be provisioned manually by a trusted database operator. Other feature cards are entry points/placeholders; they are not complete production workflows.

## Repository layout
- `frontend/` — React application
- `backend/` — Java 21 / Spring Boot API
- `database/` — legacy schema design draft; current development tables are created by Flyway
- `docs/` — product, architecture, privacy and development notes

## Run locally

### 1. Start MySQL
Create a MySQL 8 database named `untold`. The backend uses Flyway to create the required tables.

### 2. Configure and run the backend
Requires Java 21 and Maven. Set these environment variables in your terminal (use your own database credentials and generate your own random 32-byte Base64 encryption key):

```bash
export DB_URL=jdbc:mysql://localhost:3306/untold
export DB_USERNAME=your_mysql_user
export DB_PASSWORD=your_mysql_password
export COOKIE_SECURE=false
export CORS_ALLOWED_ORIGIN=http://localhost:5173
export UNTOLD_DATA_ENCRYPTION_KEY=your_base64_encoded_32_byte_key
cd backend
mvn spring-boot:run
```

Never commit the encryption key. Back it up securely: if it is lost, existing encrypted private writing cannot be decrypted. Do not use the example CI key outside automated tests.

### 3. Configure and run the frontend
Requires Node.js 20.19+ or 22.12+.

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

The default frontend API base URL is `http://localhost:8080`. Health check: `GET http://localhost:8080/api/health`.

## Security and product principles
1. Private by default; publishing is an explicit action.
2. Anonymous to other members does not mean technically untraceable.
3. No sale of private writing and no ad targeting based on it.
4. No diagnosis or claims to provide therapy.
5. Reporting, blocking, moderation, deletion and appeals are core product requirements.
6. Human experiments are voluntary and transparently described.

## Not production-ready yet
Email verification, password reset, login throttling/rate limiting, key rotation and recovery procedures, account-wide export/deletion, security monitoring, independent security review, production deployment and real-device verification remain pending. Community publishing is review-gated, but the moderation operations are not production-ready: there is no moderator dashboard, appeals workflow, automated abuse/rate-limit system or staffing process. Do not invite real users or enter sensitive writing into a deployment you do not control. Do not treat UNTOLD as emergency or clinical support.

See [Product specification](docs/PRODUCT.md), [Architecture](docs/ARCHITECTURE.md), [Privacy and safety](docs/PRIVACY-AND-SAFETY.md), [API contract](docs/API-CONTRACT.md), [Community moderation operations](docs/COMMUNITY-MODERATION.md), and [Development plan](docs/ROADMAP.md).
