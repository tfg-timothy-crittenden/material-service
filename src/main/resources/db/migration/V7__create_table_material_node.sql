CREATE TABLE IF NOT EXISTS material_node (
    id                      BIGINT PRIMARY KEY,
    parent_node_id          BIGINT         REFERENCES material_node(id) ON DELETE CASCADE,
    blueprint_node_id       BIGINT         REFERENCES exam_blueprint_node(id) ON DELETE SET NULL,
    kind                    VARCHAR(30)  NOT NULL,
    code                    VARCHAR(100) NOT NULL,
    title                   VARCHAR(250),
    display_order           INTEGER      NOT NULL CHECK (display_order >= 0),
    skill_id                BIGINT         REFERENCES skill(id) ON DELETE SET NULL,
    task_type_id            BIGINT         REFERENCES task_type(id) ON DELETE SET NULL,
    instructions            TEXT,
    stimulus_text           TEXT,
    transcript_text         TEXT,
    explanation_text        TEXT,
    time_limit_seconds      INTEGER      CHECK (time_limit_seconds IS NULL OR time_limit_seconds >= 0),
    prep_time_seconds       INTEGER      CHECK (prep_time_seconds IS NULL OR prep_time_seconds >= 0),
    response_mode           VARCHAR(30)  NOT NULL DEFAULT 'NONE',
    response_required       BOOLEAN      NOT NULL DEFAULT true,
    min_duration_seconds    INTEGER      CHECK (min_duration_seconds IS NULL OR min_duration_seconds >= 0),
    max_duration_seconds    INTEGER      CHECK (max_duration_seconds IS NULL OR max_duration_seconds >= 0),
    min_word_count          INTEGER      CHECK (min_word_count IS NULL OR min_word_count >= 0),
    max_word_count          INTEGER      CHECK (max_word_count IS NULL OR max_word_count >= 0),
    scoring_mode            VARCHAR(30)  NOT NULL DEFAULT 'NONE',
    max_score               NUMERIC(8,2),
    passing_score           NUMERIC(8,2),
    config                  JSONB        NOT NULL DEFAULT '{}'::jsonb,
    version                 BIGINT       NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_material_node_kind
        CHECK (kind IN ('EXAM', 'SECTION', 'PART', 'TASK_GROUP', 'TASK', 'ITEM')),

    CONSTRAINT chk_material_node_response_mode
        CHECK (response_mode IN (
            'NONE',
            'SPOKEN',
            'FILE_UPLOAD'
        )),

    CONSTRAINT chk_material_node_scoring_mode
        CHECK (scoring_mode IN (
            'NONE',
            'MANUAL',
            'AUTO',
            'HYBRID',
            'RUBRIC'
        )),

    CONSTRAINT chk_material_node_duration_bounds
        CHECK (
            min_duration_seconds IS NULL
            OR max_duration_seconds IS NULL
            OR min_duration_seconds <= max_duration_seconds
        ),

    CONSTRAINT chk_material_node_word_bounds
        CHECK (
            min_word_count IS NULL
            OR max_word_count IS NULL
            OR min_word_count <= max_word_count
        ),

    CONSTRAINT uq_material_node_sibling_order
        UNIQUE (parent_node_id, display_order)
);

