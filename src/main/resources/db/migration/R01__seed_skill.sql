-- Seed skills
INSERT INTO skill (id, code, name, description, version, created_at, updated_at)
VALUES
    (1, 'READING', 'Reading', 'Reading comprehension and analysis', 0, now(), now()),
    (2, 'LISTENING', 'Listening', 'Listening comprehension and analysis', 0, now(), now()),
    (3, 'SPEAKING', 'Speaking', 'Oral production and interaction', 0, now(), now()),
    (4, 'WRITING', 'Writing', 'Written production and interaction', 0, now(), now())
ON CONFLICT (code) DO NOTHING;


