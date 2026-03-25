CREATE TABLE event_photos
(
    id             BIGSERIAL    NOT NULL,
    event_id       BIGINT       NOT NULL,
    uploaded_by_id BIGINT       NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    caption        VARCHAR(255),
    uploaded_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_event_photos PRIMARY KEY (id),
    CONSTRAINT fk_event_photos_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_photos_user FOREIGN KEY (uploaded_by_id)
        REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_event_photos_event ON event_photos (event_id);