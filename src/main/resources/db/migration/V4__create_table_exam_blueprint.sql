CREATE TABLE exam_blueprint (
    id              BIGINT       PRIMARY KEY,
    exam_family_id  BIGINT       NOT NULL REFERENCES exam_family(id) ON DELETE RESTRICT,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    version_no      INTEGER      NOT NULL CHECK (version_no > 0),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_exam_blueprint_family_name_version
        UNIQUE (exam_family_id, name, version_no)
);