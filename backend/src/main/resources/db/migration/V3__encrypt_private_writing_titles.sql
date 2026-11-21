ALTER TABLE private_writings
  ADD COLUMN encrypted_title MEDIUMBLOB NULL AFTER writing_type;

UPDATE private_writings
  SET encrypted_title = CAST(title AS BINARY)
  WHERE encrypted_title IS NULL;

ALTER TABLE private_writings
  MODIFY encrypted_title MEDIUMBLOB NOT NULL,
  DROP COLUMN title;
