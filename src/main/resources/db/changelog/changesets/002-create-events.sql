CREATE TABLE events
(
    id               BIGSERIAL      NOT NULL,
    title            VARCHAR(200)   NOT NULL,
    description      TEXT           NOT NULL,
    cover_image_path VARCHAR(500),
    event_date       TIMESTAMP      NOT NULL,
    price            NUMERIC(10, 2) NOT NULL DEFAULT 0,
    max_participants INT            NOT NULL DEFAULT 0,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    organizer_id     BIGINT         NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_events_price CHECK (price >= 0),
    CONSTRAINT chk_events_max_participants CHECK (max_participants >= 0),
    CONSTRAINT chk_events_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'))
);

CREATE INDEX idx_events_status ON events (status);
CREATE INDEX idx_events_organizer ON events (organizer_id);
CREATE INDEX idx_events_event_date ON events (event_date);