CREATE TABLE notifications_log (
                                   id         BIGSERIAL PRIMARY KEY,
                                   user_id    BIGINT      NOT NULL REFERENCES users(id),
                                   baggage_id BIGINT      NOT NULL REFERENCES baggage(id),
                                   channel    VARCHAR(10) NOT NULL,
                                   message    VARCHAR(500) NOT NULL,
                                   status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   sent_at    TIMESTAMPTZ,
                                   CONSTRAINT chk_notif_channel CHECK (channel IN ('SMS','EMAIL')),
                                   CONSTRAINT chk_notif_status CHECK (status IN ('PENDING','SENT','FAILED'))
);