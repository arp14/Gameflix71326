-- Auth/User service schema. Owned exclusively by the Auth service -
-- no other service connects to this database directly.

CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE IF NOT EXISTS users (
    user_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    created_at   DATETIME(6)  NOT NULL
);

CREATE TABLE IF NOT EXISTS credentials (
    user_id             BIGINT PRIMARY KEY,
    -- VARCHAR, not CHAR: matches the Credential entity's @Column(length = 60)
    -- with no columnDefinition override, which defaults to VARCHAR. A
    -- BCrypt hash is always exactly 60 chars, so CHAR(60) would also
    -- have worked functionally, but this has to match what Hibernate
    -- actually generates, not just what's functionally equivalent -
    -- caught the same way as the games_db table-name mismatch: by
    -- actually running the app against this script and comparing.
    password_hash       VARCHAR(60) NOT NULL,
    password_updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_credentials_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);
