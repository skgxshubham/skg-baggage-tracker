CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       name          VARCHAR(100) NOT NULL,
                       email         VARCHAR(150) NOT NULL UNIQUE,
                       phone         VARCHAR(20),
                       password_hash VARCHAR(255),
                       role          VARCHAR(20)  NOT NULL,
                       auth_provider VARCHAR(20)  NOT NULL,
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       CONSTRAINT chk_users_role CHECK (role IN ('PASSENGER','STAFF','ADMIN')),
                       CONSTRAINT chk_users_auth_provider CHECK (auth_provider IN ('LOCAL','GOOGLE'))
);