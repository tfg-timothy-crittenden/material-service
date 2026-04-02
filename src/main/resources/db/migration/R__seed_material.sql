-- Seed data for material table
-- Adjust exam_family_id and blueprint_id as needed to match your actual data
INSERT INTO material (id, exam_family_id, blueprint_id, code, title, description, author_id, owner_org_id, version, created_at, updated_at)
VALUES
    (1, 1, NULL, 'MAT-TOEFL-001', 'TOEFL Practice Test 1', 'First TOEFL practice test', NULL, NULL, 0, now(), now()),
    (2, 2, NULL, 'MAT-CAE-001', 'CAE Practice Test 1', 'First CAE practice test', NULL, NULL, 0, now(), now()),
    (3, 3, NULL, 'MAT-FCE-001', 'FCE Practice Test 1', 'First FCE practice test', NULL, NULL, 0, now(), now());

