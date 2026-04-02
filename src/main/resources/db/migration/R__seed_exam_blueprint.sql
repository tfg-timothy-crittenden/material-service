-- Seed exam_blueprint
INSERT INTO exam_blueprint (id, exam_family_id, code, name, version_no, is_active, version, created_at, updated_at)
VALUES
    (1, 1, 'TOEFL_V1', 'TOEFL Blueprint v1', 1, true, 0, now(), now()),
    (2, 1, 'TOEFL_V2', 'TOEFL Blueprint v2', 2, true, 0, now(), now()),
    (3, 2, 'CAE_V1', 'CAE Blueprint v1', 1, true, 0, now(), now()),
    (4, 3, 'FCE_V1', 'FCE Blueprint v1', 1, true, 0, now(), now())
ON CONFLICT (code) DO NOTHING;

