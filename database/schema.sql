-- UNTOLD initial relational schema (MySQL 8.0+)
-- Apply only after reviewing the security and retention requirements in docs/PRIVACY-AND-SAFETY.md.
CREATE DATABASE IF NOT EXISTS untold CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE untold;

CREATE TABLE users (
  id BINARY(16) PRIMARY KEY,
  email_normalized VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  status ENUM('PENDING','ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE user_preferences (
  user_id BINARY(16) PRIMARY KEY,
  community_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
  mirror_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
  experiment_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE journal_entries (
  id BINARY(16) PRIMARY KEY,
  owner_id BINARY(16) NOT NULL,
  encrypted_body MEDIUMBLOB NOT NULL,
  encryption_key_version VARCHAR(40) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX idx_journal_owner_updated (owner_id, updated_at),
  CONSTRAINT fk_journal_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE unsent_letters (
  id BINARY(16) PRIMARY KEY,
  owner_id BINARY(16) NOT NULL,
  encrypted_title VARBINARY(2048) NULL,
  encrypted_body MEDIUMBLOB NOT NULL,
  encryption_key_version VARCHAR(40) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX idx_letters_owner_updated (owner_id, updated_at),
  CONSTRAINT fk_letters_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE community_posts (
  id BINARY(16) PRIMARY KEY,
  owner_id BINARY(16) NOT NULL,
  body TEXT NOT NULL,
  category VARCHAR(40) NOT NULL,
  moderation_status ENUM('PENDING','APPROVED','REJECTED','REMOVED') NOT NULL DEFAULT 'PENDING',
  published_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_feed_status_published (moderation_status, published_at),
  CONSTRAINT fk_posts_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reports (
  id BINARY(16) PRIMARY KEY,
  reporter_id BINARY(16) NOT NULL,
  post_id BINARY(16) NULL,
  reason_code VARCHAR(40) NOT NULL,
  details TEXT NULL,
  status ENUM('OPEN','IN_REVIEW','ACTIONED','DISMISSED') NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_reports_status_created (status, created_at),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE SET NULL
);

CREATE TABLE blocks (
  blocker_id BINARY(16) NOT NULL,
  blocked_id BINARY(16) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (blocker_id, blocked_id),
  CONSTRAINT fk_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_blocks_blocked FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_no_self_block CHECK (blocker_id <> blocked_id)
);

CREATE TABLE time_capsules (
  id BINARY(16) PRIMARY KEY,
  owner_id BINARY(16) NOT NULL,
  encrypted_body MEDIUMBLOB NOT NULL,
  encryption_key_version VARCHAR(40) NOT NULL,
  reveal_at TIMESTAMP(6) NOT NULL,
  revealed_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_capsules_due (reveal_at, revealed_at),
  CONSTRAINT fk_capsules_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Before production: add session/token storage, consent history, moderation audit,
-- comments/reactions, experiment definitions/responses, match consent, notifications,
-- retention/deletion jobs and carefully designed key-management integration.
