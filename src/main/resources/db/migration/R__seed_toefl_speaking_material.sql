-- Seed TOEFL Speaking exam material (Part 1: Listen and Repeat)
-- Ensures exam_family, material, and material_node are all linked correctly

-- 2. Represents a whole practice exam
INSERT INTO material (id, exam_family_id, material_node_id, code, title, description)
VALUES (1, 1, null, 'TOEFL_1', 'TOEFL Practice Test 1', 'TOEFL Practice Test 1')
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
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, updated_at=now();

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
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, updated_at=now();

-- 6. Insert each sentence as an ITEM node under part 1, each with skill_id=4
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES
    (3, 2, NULL, 'ITEM', 'Q1', 'Question 1', 0, 4, NULL, NULL, NULL, 'Welcome to the art gallery.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q1.mp3"}', 0, now(), now()),
    (4, 2, NULL, 'ITEM', 'Q2', 'Question 2.', 1, 4, NULL, NULL, NULL, 'A free audio guide is available for all visitors.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q2.mp3"}', 0, now(), now()),
    (5, 2, NULL, 'ITEM', 'Q3', 'Question 3', 2, 4, NULL, NULL, NULL, 'Digital maps can be used for planning your visit.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q3.mp3"}', 0, now(), now()),
    (6, 2, NULL, 'ITEM', 'Q4', 'Question 4', 3, 4, NULL, NULL, NULL, 'If you have questions, just ask a staff member', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q4.mp3"}', 0, now(), now()),
    (7, 2, NULL, 'ITEM', 'Q5', 'Question 5', 4, 4, NULL, NULL, NULL, 'When taking photos, please turn off your flash.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q5.mp3"}', 0, now(), now()),
    (8, 2, NULL, 'ITEM', 'Q6', 'Question 6', 5, 4, NULL, NULL, NULL, 'There’s also a quiet area over here for personal reflection.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q6.mp3"}', 0, now(), now()),
    (9, 2, NULL, 'ITEM', 'Q7', 'Question 7', 6, 4, NULL, NULL, NULL, 'Before leaving the gallery, please make sure to return your audio guide at the entrance.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q7.mp3"}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, transcript_text=EXCLUDED.transcript_text, config=EXCLUDED.config, updated_at=now();
