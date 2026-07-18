-- Games/Catalog service schema. Owned exclusively by the Games service -
-- no other service connects to this database directly. The Reviews
-- service references game IDs but never joins against this table; it
-- confirms a game exists by calling the Games service's API instead.

CREATE DATABASE IF NOT EXISTS games_db;
USE games_db;

CREATE TABLE IF NOT EXISTS games (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    genre VARCHAR(255)
);
