CREATE TABLE IF NOT EXISTS material (
    id              BIGINT PRIMARY KEY,
    exam_family_id  BIGINT         NOT NULL REFERENCES exam_family(id) ON DELETE RESTRICT,
    blueprint_id    BIGINT         REFERENCES exam_blueprint(id) ON DELETE SET NULL,
    material_node_id BIGINT        REFERENCES material_node(id) ON DELETE SET NULL, -- Added link to root node

    code            VARCHAR(100) NOT NULL,
    title           VARCHAR(250) NOT NULL,
    description     TEXT,

    author_id       BIGINT,
    owner_org_id    BIGINT,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);