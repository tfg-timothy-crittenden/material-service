-- Seed data for material_version table
-- Example row, adjust as needed
INSERT INTO material_version (
    id, material_id, version_no, status, change_summary, created_by, published_at, blueprint_snapshot, is_locked, version, created_at, updated_at
) VALUES
    (1, 1, 1, 'DRAFT', 'Initial version', 1, NULL, '{"example":true}', false, 0, now(), now());

