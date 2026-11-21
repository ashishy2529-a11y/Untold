# Privacy, security and safety requirements

This document is a release gate, not a claim that the current prototype already satisfies these controls.

## Private writing
- Use authenticated, server-side ownership checks on every object operation.
- Encrypt sensitive content at rest with keys managed outside the database; version keys and plan rotation.
- Never include private bodies, passwords, tokens or encryption keys in application logs.
- Keep private journal storage and public community publishing separate.
- Deletion must cover primary records, derived data, caches and documented backup-retention windows.
- No private writing sale, ad targeting, silent AI analysis or training.

## Authentication and application security
- Hash passwords with Argon2id or appropriately configured bcrypt.
- Use secure HttpOnly, SameSite cookies with CSRF protection, or a carefully reviewed token design.
- Expire and revoke sessions; rate-limit authentication and sensitive endpoints.
- Validate input, encode output, protect against CSRF/XSS/SQL injection and use least privilege.
- Keep secrets in environment/secret management; never commit credentials.
- Audit moderator/admin access and sensitive actions.

## Community safety
- Reporting, blocking, moderation queue, appeals and repeat-abuse controls are required.
- Prohibit doxxing, threats, targeted harassment, naming/shaming and unverified accusations.
- Do not expose account identifiers, email or private metadata in anonymous public APIs.
- Define moderator access boundaries; moderators should not have unrestricted access to private journals.

## Mental health boundaries
UNTOLD is not therapy and does not diagnose or treat conditions. Provide clear support pathways for urgent risk without implying emergency monitoring or guaranteed response.

## Human experiments
Participation must be voluntary, procedures and any simulated stimuli must be disclosed, and results must state limitations. Do not make causal claims unsupported by the design.

## Before public launch
Complete a threat model, privacy review, security testing, access-control tests, deletion tests, moderation operations, backup/restore drill, incident response plan, accessibility review and legal review appropriate to launch jurisdictions.
