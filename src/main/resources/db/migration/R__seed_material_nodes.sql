INSERT INTO material_node (
    id, material_version_id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, response_mode, response_required, scoring_mode, config,
    version, created_at, updated_at
) VALUES
    -- Root exam node
    (1001, 1, NULL, 100, 'EXAM', 'EXAM1', 'TOEFL Exam', 0, NULL, NULL, 'NONE', TRUE, 'NONE', '{}', 0, now(), now()),
    -- Section under exam
    (1002, 1, 1001, 101, 'SECTION', 'SEC1', 'Reading Section', 0, 1, NULL, 'NONE', TRUE, 'NONE', '{}', 0, now(), now()),
    -- Task under section
    (1003, 1, 1002, 102, 'TASK', 'TASK1', 'Reading Task 1', 0, 1, 1, 'FREE_TEXT', TRUE, 'MANUAL', '{}', 0, now(), now())
ON CONFLICT (material_version_id, code) DO UPDATE SET
    id = EXCLUDED.id,
    parent_node_id = EXCLUDED.parent_node_id,
    blueprint_node_id = EXCLUDED.blueprint_node_id,
    kind = EXCLUDED.kind,
    title = EXCLUDED.title,
    display_order = EXCLUDED.display_order,
    skill_id = EXCLUDED.skill_id,
    task_type_id = EXCLUDED.task_type_id,
    response_mode = EXCLUDED.response_mode,
    response_required = EXCLUDED.response_required,
    scoring_mode = EXCLUDED.scoring_mode,
    config = EXCLUDED.config,
    version = EXCLUDED.version,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at;
