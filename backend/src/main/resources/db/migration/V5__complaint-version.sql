-- Optimistic locking: incremented on every update so concurrent edits are detected.
ALTER TABLE complaints ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
