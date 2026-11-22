CREATE TABLE community_posts (
  id BINARY(16) NOT NULL PRIMARY KEY,
  author_id BINARY(16) NOT NULL,
  category VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  moderated_at TIMESTAMP(6) NULL,
  INDEX idx_community_feed (status, created_at),
  INDEX idx_community_author (author_id, created_at),
  CONSTRAINT fk_community_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE community_reports (
  id BINARY(16) NOT NULL PRIMARY KEY,
  post_id BINARY(16) NOT NULL,
  reporter_id BINARY(16) NOT NULL,
  reason VARCHAR(32) NOT NULL,
  details VARCHAR(500) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_reporter_post (post_id, reporter_id),
  INDEX idx_reports_queue (status, created_at),
  CONSTRAINT fk_report_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_report_user FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_blocks (
  blocker_id BINARY(16) NOT NULL,
  blocked_id BINARY(16) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (blocker_id, blocked_id),
  CONSTRAINT fk_blocker FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_blocked FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_no_self_block CHECK (blocker_id <> blocked_id)
) ENGINE=InnoDB;
