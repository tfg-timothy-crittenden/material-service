CREATE TABLE IF NOT EXISTS exam_blueprint_node (
    id                          BIGINT PRIMARY KEY,
    blueprint_id                BIGINT         NOT NULL REFERENCES exam_blueprint(id) ON DELETE CASCADE,
    parent_node_id              BIGINT         REFERENCES exam_blueprint_node(id) ON DELETE CASCADE,

    kind                        VARCHAR(30)  NOT NULL,
    code                        VARCHAR(100) NOT NULL,
    title                       VARCHAR(250) NOT NULL,
    display_order               INTEGER      NOT NULL CHECK (display_order >= 0),

    skill_id                    BIGINT        REFERENCES skill(id) ON DELETE SET NULL,
    task_type_id                BIGINT         REFERENCES task_type(id) ON DELETE SET NULL,

    is_required                 BOOLEAN      NOT NULL DEFAULT TRUE,
    min_children                INTEGER      CHECK (min_children IS NULL OR min_children >= 0),
    max_children                INTEGER      CHECK (max_children IS NULL OR max_children >= 0),

    default_time_limit_seconds  INTEGER      CHECK (default_time_limit_seconds IS NULL OR default_time_limit_seconds >= 0),
    default_prep_time_seconds   INTEGER      CHECK (default_prep_time_seconds IS NULL OR default_prep_time_seconds >= 0),

    config                      JSONB        NOT NULL DEFAULT '{}'::jsonb,

    version                     BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_exam_blueprint_node_kind
        CHECK (kind IN ('EXAM', 'SECTION', 'PART', 'TASK_GROUP', 'TASK', 'ITEM')),

    CONSTRAINT chk_exam_blueprint_node_child_bounds
        CHECK (
            min_children IS NULL
            OR max_children IS NULL
            OR min_children <= max_children
        ),

    CONSTRAINT uq_exam_blueprint_node_code
        UNIQUE (blueprint_id, code),

    CONSTRAINT uq_exam_blueprint_node_sibling_order
        UNIQUE (blueprint_id, parent_node_id, display_order)
);