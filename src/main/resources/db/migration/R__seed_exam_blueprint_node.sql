-- Seed exam_blueprint_node
-- Example: root exam node, section, and task
INSERT INTO exam_blueprint_node (
    id, blueprint_id, parent_node_id, kind, code, title, display_order, skill_id, task_type_id, is_required, min_children, max_children, default_time_limit_seconds, default_prep_time_seconds, config, version, created_at, updated_at
) VALUES
    (100, 1, NULL, 'EXAM', 'TOEFL_EXAM', 'TOEFL Exam', 0, NULL, NULL, TRUE, 1, 4, NULL, NULL, '{}', 0, now(), now()),
    (101, 1, 100, 'SECTION', 'READING', 'Reading Section', 0, 1, NULL, TRUE, 1, 1, 3600, NULL, '{}', 0, now(), now()),
    (102, 1, 101, 'TASK', 'READING_TASK_1', 'Reading Task 1', 0, 1, 1, TRUE, NULL, NULL, 600, 60, '{}', 0, now(), now())
ON CONFLICT (blueprint_id, code) DO NOTHING;

