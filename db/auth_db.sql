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
    password_hash       CHAR(60)    NOT NULL,
    password_updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_credentials_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);
