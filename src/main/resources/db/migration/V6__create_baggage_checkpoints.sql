CREATE TABLE baggage_checkpoints (
                                     id              BIGSERIAL PRIMARY KEY,
                                     baggage_id      BIGINT      NOT NULL REFERENCES baggage(id),
                                     checkpoint_type VARCHAR(20) NOT NULL,
                                     location        VARCHAR(3),
                                     timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     notes           VARCHAR(255),
                                     CONSTRAINT chk_checkpoint_type CHECK (checkpoint_type IN
                                                                           ('CHECKED_IN','SECURITY_CLEARED','LOADED','IN_TRANSIT','ARRIVED','ON_CAROUSEL','MISHANDLED'))
);

CREATE INDEX idx_checkpoints_baggage_id ON baggage_checkpoints (baggage_id);