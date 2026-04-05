-- Seed TOEFL Speaking exam material (Part 2: Take an Interview)
-- Assumes material and root node already exist (material id=1, root node id=1)

-- Insert part 2 node (Take an Interview) as PART under root
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    10, 1, NULL, 'PART', 'TAKE_INTERVIEW', 'Take an Interview', 1,
    4, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, updated_at=now();

-- Insert each interview question as an ITEM node under part 2, each with skill_id=4
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text,
    response_mode, config, created_at, updated_at
) VALUES
    (11, 10, 'ITEM', 'INTERVIEW_QUESTION', 'Q1', 1, 4, NULL, NULL, NULL, 'Tell me about a time you solved a problem.', 'SPOKEN', '{}', now(), now()),
    (12, 10, 'ITEM', 'INTERVIEW_QUESTION', 'Q2', 2, 4, NULL, NULL, NULL, 'What is your favorite subject and why?','SPOKEN', '{}', now(), now()),
    (13, 10, 'ITEM', 'INTERVIEW_QUESTION', 'Q3', 3, 4, NULL, NULL, NULL, 'Describe a challenge you faced at school.','SPOKEN', '{}', now(), now()),
    (14, 10, 'ITEM', 'INTERVIEW_QUESTION', 'Q4', 4, 4, NULL, NULL, NULL,  'How do you prepare for exams?', 'SPOKEN', '{}', now(), now())
ON CONFLICT (id) DO UPDATE SET
    title=EXCLUDED.title, parent_node_id=EXCLUDED.parent_node_id, kind=EXCLUDED.kind, display_order=EXCLUDED.display_order, skill_id=EXCLUDED.skill_id, transcript_text=EXCLUDED.transcript_text, updated_at=now();
