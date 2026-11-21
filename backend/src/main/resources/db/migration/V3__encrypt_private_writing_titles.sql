ALTER TABLE private_writings
  ADD COLUMN encrypted_title MEDIUMBLOB NULL AFTER title;

-- Existing development records keep their encrypted body; replace any legacy title
-- with a generic label. New titles are stored encrypted by the application.
UPDATE private_writings
  SET title = CASE WHEN writing_type = 'UNSENT' THEN 'UNSENT LETTER' ELSE 'PRIVATE NOTE' END;
