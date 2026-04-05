CREATE TABLE IF NOT EXISTS material_asset (
    id                  BIGINT PRIMARY KEY,
    material_node_id    BIGINT         NOT NULL REFERENCES material_node(id) ON DELETE CASCADE,

    kind                VARCHAR(30)  NOT NULL,
    storage_key         VARCHAR(500) NOT NULL,
    original_filename   VARCHAR(255),
    mime_type           VARCHAR(150),
    file_size_bytes     BIGINT       CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0),

    title               VARCHAR(250),
    transcript_text     TEXT,
    display_order       INTEGER      NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    metadata            JSONB        NOT NULL DEFAULT '{}'::jsonb,

    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_material_asset_kind
        CHECK (kind IN ('TEXT', 'AUDIO', 'IMAGE', 'VIDEO', 'PDF', 'OTHER'))
);