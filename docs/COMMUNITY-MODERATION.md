# Community moderation operations

## Current implementation

Community submissions are stored as `PENDING`. Only `APPROVED` posts are returned by the community feed. Authenticated users can submit stories, report another author's story, block an author, and remove their own block. Moderator endpoints are protected by the `MODERATOR` role.

- `GET /api/moderation/queue` — list pending stories, with open-report counts.
- `POST /api/moderation/posts/{postId}/decision` — approve or reject a pending story.
- `GET /api/community/feed` — approved stories only, filtered for blocks in either direction.
- `POST /api/community/posts` — submit a story for review.
- `POST /api/community/posts/{postId}/report` — report a story.
- `POST /api/community/posts/{postId}/block-author` — block the story author.
- `DELETE /api/community/blocks/{blockedUserId}` — unblock an author.

Every moderation decision is written to `moderation_audit`. Moderators can review community submissions only; these endpoints do not provide access to private journal entries or unsent letters.

## Assign the first moderator (manual, trusted database access only)

There is deliberately no public moderator-registration endpoint. First create a normal account through the registration screen, then use a trusted database administrator connection to promote that account:

```sql
UPDATE users
SET role = 'MODERATOR'
WHERE email_normalized = LOWER(TRIM('moderator@example.com'));

SELECT email_normalized, role
FROM users
WHERE email_normalized = LOWER(TRIM('moderator@example.com'));
```

Replace the example address with the exact email used to register. Confirm that exactly one account was updated. The account must sign out and sign in again so the new role is loaded into its session. Never expose database credentials or this SQL interface to ordinary users.

## Important operational limitations

- The current frontend does not include a moderator dashboard; moderators must call the protected API using an authenticated session and CSRF token.
- A moderator account must be provisioned manually by a trusted operator.
- There is no automated content classifier. Human review is required before publication.
- Appeals, moderation staffing/response targets, rate limiting, abuse monitoring, account recovery and production incident procedures are not implemented.
- Do not open public registration or invite real users until these operational controls, privacy/legal documents, backups, deletion behaviour and independent security review are complete.

For local testing, use fictional stories and separate test accounts. Never use private user writing as moderation test data.
