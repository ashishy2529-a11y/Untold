CREATE TABLE private_writings (
  id BINARY(16) NOT NULL PRIMARY KEY,
  owner_id BINARY(16) NOT NULL,
  writing_type VARCHAR(16) NOT NULL,
  title VARCHAR(160) NOT NULL,
  encrypted_content MEDIUMBLOB NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_private_writings_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_private_writings_owner_type_created (owner_id, writing_type, created_at)
) ENGINE=InnoDB;