-- Seed TOEFL Speaking exam material (Part 2: Take an Interview)
-- Assumes material and root node already exist (material id=1, root node id=1)

-- Insert part 2 node (Take an Interview) as PART under root
INSERT INTO material_node (
    id, parent_node_id, kind, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    10, 1, 'PART', 'Take an Interview', 1,
    4, NULL, NULL, NULL, NULL, NULL, NULL,
    NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}'::json, 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, updated_at=now(), config=COALESCE(EXCLUDED.config, '{}'::json);

-- Insert each interview question as an ITEM node under part 2, each with skill_id=4
INSERT INTO material_node (
    id, parent_node_id, kind, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES
    (11, 10, 'ITEM', 'Q1', 0, 4, NULL, NULL, NULL, 'Tell me about a time you solved a problem.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}'::json, 0, now(), now()),
    (12, 10, 'ITEM', 'Q2', 1, 4, NULL, NULL, NULL, 'What is your favorite subject and why?', NULL, NULL,  NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}'::json, 0, now(), now()),
    (13, 10, 'ITEM', 'Q3', 2, 4, NULL, NULL, NULL, 'Describe a challenge you faced at school.', NULL, NULL,  NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}'::json, 0, now(), now()),
    (14, 10, 'ITEM', 'Q4', 3, 4, NULL, NULL, NULL, 'How do you prepare for exams?', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}'::json, 0, now(), now())
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, transcript_text=EXCLUDED.transcript_text, updated_at=now();

-- Ensure sequence is set to max(id)
SELECT setval('material_node_id_seq', (SELECT COALESCE(MAX(id), 1) FROM material_node));
