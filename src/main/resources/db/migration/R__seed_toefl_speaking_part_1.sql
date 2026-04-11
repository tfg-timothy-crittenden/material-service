-- TOEFL Speaking Part 1 (2026) — Listen and Repeat Practice
-- New topic: Campus library
-- Situation: You are hearing a short introduction to a university library. After each sentence, pause briefly, imagine the beep, and repeat the sentence exactly.

-- Sentence 1: Easy
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    3, 2, 'ITEM', 'LISTEN_REPEAT_1', 'Q1', 0,
    4, NULL, 'Welcome to the campus library entrance.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
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
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    4, 2, 'ITEM', 'LISTEN_REPEAT_2', 'Q2', 1,
    4, NULL, 'Students can borrow books at this desk.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 3: Medium
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    5, 2, 'ITEM', 'LISTEN_REPEAT_3', 'Q3', 2,
    4, NULL, 'Please scan your student card before using the self-checkout machine.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 4: Medium
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    6, 2, 'ITEM', 'LISTEN_REPEAT_4', 'Q4', 3,
    4, NULL, 'The computers near the window are for research, printing, and homework.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 5: Medium
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    7, 2, 'ITEM', 'LISTEN_REPEAT_5', 'Q5', 4,
    4, NULL, 'Return library books on time to avoid late fees on your account.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 6: Hard
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    8, 2, 'ITEM', 'LISTEN_REPEAT_6', 'Q6', 5,
    4, NULL, 'Group study rooms can be reserved online, but please arrive early so your booking is not canceled.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

-- Sentence 7: Hard
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    9, 2, 'ITEM', 'LISTEN_REPEAT_7', 'Q7', 6,
    4, NULL, 'Before you leave the library, put your chair back, collect your items, and make sure your table is clean.', 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    parent_node_id=EXCLUDED.parent_node_id,
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    updated_at=now(),
    config=EXCLUDED.config,
    version=EXCLUDED.version;

