CREATE TABLE airports (
                          id   BIGSERIAL PRIMARY KEY,
                          code VARCHAR(3)   NOT NULL UNIQUE,
                          name VARCHAR(150) NOT NULL,
                          city VARCHAR(100) NOT NULL
);