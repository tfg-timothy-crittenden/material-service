-- TOEFL Speaking Part 1 (2026) — Listen and Repeat Practice
-- New topic: Campus library
-- Situation: You are hearing a short introduction to a university library. After each sentence, pause briefly, imagine the beep, and repeat the sentence exactly.

-- Sentence 1: Easy
INSERT INTO material_node (
    id, parent_node_id, kind, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    3, 2, 'ITEM', 'Q1', 0,
    4, NULL, 'Welcome to the campus library entrance.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 2: Easy
INSERT INTO material_node (
    id, parent_node_id, kind, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    4, 2, 'ITEM', 'Q2', 1,
    4, NULL, 'Students can borrow books at this desk.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;
