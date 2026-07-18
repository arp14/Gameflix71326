-- Games/Catalog service schema. Owned exclusively by the Games service -
-- no other service connects to this database directly. The Reviews
-- service references game IDs but never joins against this table; it
-- confirms a game exists by calling the Games service's API instead.

CREATE DATABASE IF NOT EXISTS games_db;
USE games_db;

-- Table name is "game" (singular), not "games" - it has to match what
-- Hibernate generates by default for the `Game` entity (no @Table
-- override, same as the original monolith), since the app itself
-- creates/migrates this table via spring.jpa.hibernate.ddl-auto=update.
-- This script exists for reference/manual setup; if it ever disagrees
-- with the entity's actual table name, Hibernate silently creates a
-- second, empty table alongside this one instead of using it.
CREATE TABLE IF NOT EXISTS game (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    genre VARCHAR(255)
);
