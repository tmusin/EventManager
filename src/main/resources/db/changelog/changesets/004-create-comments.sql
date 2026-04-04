CREATE TABLE comments
(
    id             BIGSERIAL NOT NULL,
    event_id       BIGINT    NOT NULL,
    author_id      BIGINT    NOT NULL,
    content        TEXT      NOT NULL,
    is_organizer   BOOLEAN   NOT NULL DEFAULT FALSE,
    is_participant BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_comments_event ON comments (event_id);
CREATE INDEX idx_comments_author ON comments (author_id);