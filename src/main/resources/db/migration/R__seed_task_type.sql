-- Seed task types
INSERT INTO task_type (id, code, name, exam_family_id, skill_id, description, config_schema, version, created_at, updated_at)
VALUES
    (1, 'READING_COMPREHENSION', 'Reading Comprehension', 1, 1, 'Reading comprehension tasks', '{}', 0, now(), now()),
    (2, 'LISTENING_COMPREHENSION', 'Listening Comprehension', 1, 2, 'Listening comprehension tasks', '{}', 0, now(), now()),
    (3, 'SPEAKING_RESPONSE', 'Speaking Response', 1, 3, 'Speaking response tasks', '{}', 0, now(), now()),
    (4, 'WRITING_ESSAY', 'Writing Essay', 1, 4, 'Essay writing tasks', '{}', 0, now(), now())
ON CONFLICT (code) DO NOTHING;

