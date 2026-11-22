ALTER TABLE users ADD COLUMN role VARCHAR(24) NOT NULL DEFAULT 'USER';

CREATE TABLE moderation_audit (
  id BINARY(16) NOT NULL PRIMARY KEY,
  moderator_id BINARY(16) NOT NULL,
  post_id BINARY(16) NOT NULL,
  action VARCHAR(16) NOT NULL,
  note VARCHAR(500) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_moderation_audit_post (post_id, created_at),
  CONSTRAINT fk_audit_moderator FOREIGN KEY (moderator_id) REFERENCES users(id),
  CONSTRAINT fk_audit_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
