
-- Seed exam families
INSERT INTO exam_family (id, code, name, description)
VALUES (1, 'TOEFL', 'TOEFL', 'Test of English as a Foreign Language')
    ON CONFLICT (id) DO UPDATE SET code=EXCLUDED.code, name=EXCLUDED.name, description=EXCLUDED.description;

-- Seed task types
INSERT INTO task_type (id, code, name, exam_family_id, skill_id, description, config_schema, version, created_at, updated_at)
VALUES
    (1, 'TOEFL_SPEAKING_PART_1', 'TOEFL Speaking Part 1', 1, 1, 'Listen and Repeat', '{}', 0, now(), now())
ON CONFLICT (id) DO NOTHING;


