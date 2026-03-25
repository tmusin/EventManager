CREATE TABLE acl_class
(
    id    BIGSERIAL    NOT NULL,
    class VARCHAR(255) NOT NULL,
    CONSTRAINT pk_acl_class PRIMARY KEY (id),
    CONSTRAINT uq_acl_class UNIQUE (class)
);

CREATE TABLE acl_sid
(
    id        BIGSERIAL    NOT NULL,
    principal BOOLEAN      NOT NULL,
    sid       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_acl_sid PRIMARY KEY (id),
    CONSTRAINT uq_acl_sid UNIQUE (sid, principal)
);

CREATE TABLE acl_object_identity
(
    id                 BIGSERIAL NOT NULL,
    object_id_class    BIGINT    NOT NULL,
    object_id_identity BIGINT    NOT NULL,
    parent_object      BIGINT,
    owner_sid          BIGINT,
    entries_inheriting BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_acl_object_identity PRIMARY KEY (id),
    CONSTRAINT uq_acl_object_identity UNIQUE (object_id_class, object_id_identity),
    CONSTRAINT fk_acl_oi_class FOREIGN KEY (object_id_class)
        REFERENCES acl_class (id),
    CONSTRAINT fk_acl_oi_parent FOREIGN KEY (parent_object)
        REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_oi_owner FOREIGN KEY (owner_sid)
        REFERENCES acl_sid (id)
);

CREATE TABLE acl_entry
(
    id                  BIGSERIAL NOT NULL,
    acl_object_identity BIGINT    NOT NULL,
    ace_order           INT       NOT NULL,
    sid                 BIGINT    NOT NULL,
    mask                INT       NOT NULL,
    granting            BOOLEAN   NOT NULL,
    audit_success       BOOLEAN   NOT NULL DEFAULT FALSE,
    audit_failure       BOOLEAN   NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_acl_entry PRIMARY KEY (id),
    CONSTRAINT uq_acl_entry UNIQUE (acl_object_identity, ace_order),
    CONSTRAINT fk_acl_entry_oi FOREIGN KEY (acl_object_identity)
        REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_entry_sid FOREIGN KEY (sid)
        REFERENCES acl_sid (id)
);