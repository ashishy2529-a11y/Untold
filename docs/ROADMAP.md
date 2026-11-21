# Development roadmap

## Phase 0 — foundation
- [x] Product definition and feature map
- [x] Initial repository scaffold
- [ ] Wireframes and design review
- [ ] Threat model and data-flow review

## Phase 1 — app shell
- [x] Responsive dashboard prototype
- [x] Nine feature entry points
- [ ] Accessibility audit and browser/device testing
- [x] CI workflow configured (first run with MySQL service is being checked)

## Phase 2 — private expression and identity
- [x] Initial session-based account registration and login API
- [x] BCrypt password hashing and server-side sessions
- [x] CSRF protection and configurable secure session cookies
- [x] MySQL/Flyway account migration and authentication integration tests added
- [ ] Verify CI passes on GitHub Actions
- [ ] Email verification, password reset and login throttling
- [ ] Encrypted journal and letters persistence
- [ ] Server-side ownership and cross-user access tests
- [ ] Export and deletion workflow

## Phase 3 — community
- [ ] Moderated story publishing and feed
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
