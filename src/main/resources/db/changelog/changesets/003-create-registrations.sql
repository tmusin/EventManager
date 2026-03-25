CREATE TABLE event_registrations
(
    id            BIGSERIAL   NOT NULL,
    event_id      BIGINT      NOT NULL,
    user_id       BIGINT      NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    registered_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_event_registrations PRIMARY KEY (id),
    CONSTRAINT uq_event_registrations UNIQUE (event_id, user_id),
    CONSTRAINT fk_event_registrations_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_registrations_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_registration_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PAID', 'REJECTED'))
);

CREATE INDEX idx_registrations_event ON event_registrations (event_id);
CREATE INDEX idx_registrations_user ON event_registrations (user_id);