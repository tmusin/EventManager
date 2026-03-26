ALTER TABLE acl_object_identity
ALTER COLUMN object_id_identity TYPE VARCHAR(36)
    USING object_id_identity::VARCHAR;