-- Seed TOEFL Speaking exam material (Part 1: Listen and Repeat)
-- Ensures exam_family, material, and material_node are all linked correctly

-- 2. Represents a whole practice exam
INSERT INTO material (id, exam_family_id, material_node_id, code, title, description, version, created_at, updated_at)
VALUES (1, 1, null, 'TOEFL_1', 'TOEFL Practice Test 1', 'TOEFL Practice Test 1', 0, now(), now())
ON CONFLICT (id) DO UPDATE SET material_node_id=EXCLUDED.material_node_id, code=EXCLUDED.code, title=EXCLUDED.title, description=EXCLUDED.description, exam_family_id=EXCLUDED.exam_family_id;

-- 4. Insert root node for the speaking section (SECTION)
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    1, NULL, NULL, 'SECTION', 'TOEFL_SPEAKING_ROOT', 'TOEFL Speaking Root', 0,
    4, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- 4b. Update material to reference the root node
UPDATE material SET material_node_id = 1 WHERE id = 1;

-- 5. Insert part 1 node (Listen and Repeat) as PART under root
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    2, 1, NULL, 'PART', 'LISTEN_REPEAT', 'Listen and Repeat', 0,
    4, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}', 0, now(), now()
)

