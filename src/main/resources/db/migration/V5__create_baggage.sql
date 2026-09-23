CREATE TABLE baggage (
                         id               BIGSERIAL PRIMARY KEY,
                         passenger_id     BIGINT       NOT NULL REFERENCES passengers(id),
                         flight_id        BIGINT       NOT NULL REFERENCES flights(id),
                         tag_number       VARCHAR(20)  NOT NULL UNIQUE,
                         weight_kg        NUMERIC(5,2) NOT NULL,
                         current_status   VARCHAR(20)  NOT NULL DEFAULT 'CHECKED_IN',
                         current_location VARCHAR(3),
                         updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         CONSTRAINT chk_baggage_status CHECK (current_status IN
                                                              ('CHECKED_IN','SECURITY_CLEARED','LOADED','IN_TRANSIT','ARRIVED','ON_CAROUSEL','MISHANDLED'))
);

CREATE INDEX idx_baggage_tag_number ON baggage (tag_number);