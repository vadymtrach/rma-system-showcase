-- Emails are now normalized to lowercase by the application; bring existing rows in line
-- so lookups and the unique constraint behave case-insensitively.
UPDATE users SET email = LOWER(TRIM(email)) WHERE email <> LOWER(TRIM(email));
