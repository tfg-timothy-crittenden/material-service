CREATE TABLE material (
    id              BIGINT PRIMARY KEY,
    exam_family_id  BIGINT         NOT NULL REFERENCES exam_family(id) ON DELETE RESTRICT,
    blueprint_id    BIGINT         REFERENCES exam_blueprint(id) ON DELETE SET NULL,

    code            VARCHAR(100) NOT NULL UNIQUE,
    title           VARCHAR(250) NOT NULL,
    description     TEXT,

    author_id       BIGINT,
    owner_org_id    BIGINT,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);