-- Reviews service schema. Owned exclusively by the Reviews service -
-- no other service connects to this database directly.
--
-- game_id and user_id are plain references, not foreign keys: the rows
-- they'd point at live in games_db and auth_db, separate databases
-- (possibly separate servers) that this service has no direct access to.
-- Referential integrity across that boundary is enforced in application
-- code instead - the Reviews service calls the Games service's API to
-- confirm a game exists before saving a review; user_id is trusted
-- as-is because it comes from an already-validated JWT, not a lookup.

CREATE DATABASE IF NOT EXISTS reviews_db;
USE reviews_db;

CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    rating     TINYINT     NOT NULL,
    comment    TEXT,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    -- One review per user per game (drop this if you'd rather allow
    -- multiple reviews from the same user over time). game_id is the
    -- leading column, so this composite index also serves the main read
    -- pattern - "all reviews for a given game" - without a second index.
    CONSTRAINT uq_reviews_game_user UNIQUE (game_id, user_id)
);
