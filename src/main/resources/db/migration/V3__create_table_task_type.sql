CREATE TABLE IF NOT EXISTS task_type (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    exam_family_id  BIGINT       REFERENCES exam_family(id) ON DELETE RESTRICT,
    skill_id        BIGINT       REFERENCES skill(id) ON DELETE SET NULL,
    description     TEXT,
    config_schema   JSONB        NOT NULL DEFAULT '{}'::jsonb,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);