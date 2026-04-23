-- Seed skills
INSERT INTO skill (id, name, description, version, created_at, updated_at)
VALUES
    (1, 'Reading', 'Reading comprehension and analysis', 0, now(), now()),
    (2, 'Listening', 'Listening comprehension and analysis', 0, now(), now()),
    (3, 'Speaking', 'Oral production and interaction', 0, now(), now()),
    (4, 'Writing', 'Written production and interaction', 0, now(), now())
ON CONFLICT (id) DO NOTHING;
