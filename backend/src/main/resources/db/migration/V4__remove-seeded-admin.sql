-- V2 seeded an admin whose password ("admin123") is published in the README. The initial admin
-- is now created at startup from BOOTSTRAP_ADMIN_EMAIL / BOOTSTRAP_ADMIN_PASSWORD instead.
-- Remove the seeded account if its password was never changed; if complaints reference it,
-- keep the row for history but make it unusable.
DELETE FROM users u
WHERE u.password = '$2a$10$sjXfgEmc1d3aYwh2uwSh3.w5/ujm/OcLFNQmvHalpxrNYZJ2fsBIO'
  AND NOT EXISTS (SELECT 1 FROM complaints c WHERE c.assigned_to_id = u.id);

UPDATE users
SET password = '{disabled}', active = FALSE
WHERE password = '$2a$10$sjXfgEmc1d3aYwh2uwSh3.w5/ujm/OcLFNQmvHalpxrNYZJ2fsBIO';
