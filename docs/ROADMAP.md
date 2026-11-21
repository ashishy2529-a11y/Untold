# Development roadmap

## Phase 0 — foundation
- [x] Product definition and feature map
- [x] Initial repository scaffold
- [ ] Wireframes and design review
- [ ] Threat model and data-flow review

## Phase 1 — app shell
- [x] Responsive dashboard prototype
- [x] Nine feature entry points
- [ ] Accessibility audit and real-device testing
- [x] GitHub Actions frontend build

## Phase 2 — private expression and identity
- [x] Account registration and login UI connected to backend
- [x] BCrypt password hashing and server-side sessions
- [x] CSRF protection and configurable secure session cookies
- [x] MySQL/Flyway account and private-writing migrations
- [x] THE VOID and THE UNSENT create/list/read/update/delete API
- [x] AES-256-GCM encryption of private writing at rest
- [x] Server-side owner scoping on every private-writing operation
- [x] My Space view and delete control connected to API
- [x] Integration tests for authentication, encrypted storage and cross-account access
- [ ] Confirm latest full CI run after final test additions
- [ ] Email verification, password reset and login throttling
- [ ] Key rotation/recovery procedure and independent security review
- [ ] Export and account-wide deletion workflow

## Phase 3 — community
- [ ] Moderated story publishing and live feed
- [ ] Reporting, blocking and moderator queue
- [ ] Appeals and audit events
- [ ] Rate limits and abuse controls

## Phase 4 — remaining spaces
- [ ] THE OTHER SIDE and THE TEA ROOM
- [ ] THE TIME CAPSULE scheduler
- [ ] THE MIRROR with explicit opt-in
- [ ] THE HUMAN EXPERIMENT
- [ ] THE SILENT MATCH with mutual consent

## Definition of done
A feature is not complete because its screen exists. It needs working happy path, validation and errors, authorization and cross-user tests, loading/empty states, mobile and keyboard accessibility, deletion behavior, moderation implications and passing automated tests.
