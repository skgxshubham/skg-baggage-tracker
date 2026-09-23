CREATE TABLE passengers (
                            id        BIGSERIAL PRIMARY KEY,
                            user_id   BIGINT      NOT NULL REFERENCES users(id),
                            flight_id BIGINT      NOT NULL REFERENCES flights(id),
                            pnr       VARCHAR(10) NOT NULL,
                            seat_no   VARCHAR(5),
                            CONSTRAINT uq_passengers_pnr_flight UNIQUE (pnr, flight_id)
);