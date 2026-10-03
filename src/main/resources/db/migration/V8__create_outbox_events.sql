CREATE TABLE outbox_events (
                               id         BIGSERIAL PRIMARY KEY,
                               topic      VARCHAR(100) NOT NULL,
                               event_key  VARCHAR(100) NOT NULL,
                               payload    TEXT         NOT NULL,
                               created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               sent       BOOLEAN      NOT NULL DEFAULT FALSE,
                               sent_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unsent ON outbox_events (sent, created_at);