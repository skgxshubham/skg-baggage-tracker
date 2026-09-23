CREATE TABLE flights (
                         id          BIGSERIAL PRIMARY KEY,
                         flight_no   VARCHAR(10)  NOT NULL,
                         origin_id   BIGINT       NOT NULL REFERENCES airports(id),
                         dest_id     BIGINT       NOT NULL REFERENCES airports(id),
                         dep_time    TIMESTAMPTZ  NOT NULL,
                         arr_time    TIMESTAMPTZ  NOT NULL,
                         status      VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
                         CONSTRAINT chk_flights_status CHECK (status IN ('SCHEDULED','DEPARTED','ARRIVED','CANCELLED'))
);

CREATE INDEX idx_flights_flight_no ON flights (flight_no);