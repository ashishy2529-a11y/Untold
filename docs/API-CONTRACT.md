# API contract — current implementation and planned endpoints

Base path: `/api`. JSON requests and responses use UTF-8.

## Health
- `GET /api/health` — service status and timestamp.

## Authentication
- `GET /api/auth/csrf` — returns a CSRF token and sets the XSRF cookie.
- `POST /api/auth/register` — accepts email and a password of 12–72 characters; stores a BCrypt hash. New accounts receive the USER role.
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

The API encrypts titles and content with AES-256-GCM before persistence. The encryption key is supplied via `UNTOLD_DATA_ENCRYPTION_KEY`; keep a protected backup. The application cannot decrypt old records if the key is lost. Every operation derives owner identity from the authenticated principal and scopes the database lookup by owner ID.

## SOMEONE, SOMEWHERE — implemented backend foundation
- `POST /api/community/posts` — submit a story. Supported categories: LIFE, GROWING UP, SMALL WINS, RELATIONSHIPS, WORK, STUDY, OTHER. Stories are created as PENDING and are never public immediately.
- `GET /api/community/feed` — returns APPROVED stories only, hides author identity and applies both-direction block filtering.
- `GET /api/community/mine` — returns the signed-in user's own submissions and moderation statuses (not story content).
- `POST /api/community/posts/{postId}/report` — report a story using a controlled reason; duplicate reports by the same account are rejected.
- `POST /api/community/posts/{postId}/block-author` — block the author of a story without exposing their account ID.
- `DELETE /api/community/blocks/{blockedUserId}` — remove a block when the account ID is known through a future account-safety UI.

## Moderation — protected role
- `GET /api/moderation/queue` — moderator-only pending queue, with open report counts.
- `POST /api/moderation/posts/{postId}/decision` — moderator-only APPROVE or REJECT decision, with an audit record and report status update.

New registrations are always USER. A trusted operator must explicitly assign the MODERATOR role to an account through a controlled database operation before that account can access moderation endpoints. Do not expose role assignment in public registration or user settings.

## Safety and current limitations
- Community story text is not encrypted at rest in this first implementation; it is public-intended content held in a restricted pending/approved workflow. Do not submit private diary content as a community post.
- Moderation is manual. No automated content classifier is used, and no public post appears before a moderator approves it.
- Blocking hides each other's approved stories from their respective feeds. It does not erase old reports or content already seen.
- A report does not itself remove a published story; it creates an open safety-review item. Moderator review is required.
- Commenting, reactions, moderation appeals, account-level block management, moderator provisioning workflow, and automated abuse/rate limiting remain future work.
- Do not launch publicly until moderation staffing, escalation procedures, abuse controls and privacy disclosures are in place.

## Client security requirements
State-changing requests must send cookies and `X-XSRF-TOKEN`. Browser clients must use credentialed requests. Production must use HTTPS and `COOKIE_SECURE=true`. CORS must be restricted to the exact frontend origin.

## Remaining launch requirements
Email verification, password recovery, rate limiting, audit/monitoring, encryption key rotation and recovery planning, account export/deletion and independent security review are not yet complete. Do not treat this development API as production-ready.
