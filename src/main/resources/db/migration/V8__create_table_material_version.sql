CREATE TABLE material_version (
    id                  BIGINT PRIMARY KEY,
    material_id         BIGINT         NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    version_no          INTEGER      NOT NULL CHECK (version_no > 0),
    status              VARCHAR(30)  NOT NULL,
    change_summary      TEXT,
    created_by          BIGINT,
    published_at        TIMESTAMPTZ,
    blueprint_snapshot  JSONB,
    is_locked           BOOLEAN      NOT NULL DEFAULT FALSE,

    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_material_version_status
        CHECK (status IN ('DRAFT', 'REVIEW', 'PUBLISHED', 'ARCHIVED')),

    CONSTRAINT chk_material_version_published_at
        CHECK (
            status <> 'PUBLISHED'
            OR published_at IS NOT NULL
        ),

    CONSTRAINT uq_material_version
        UNIQUE (material_id, version_no)
);