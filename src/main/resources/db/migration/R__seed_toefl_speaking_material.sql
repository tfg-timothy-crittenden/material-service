-- Seed TOEFL Speaking exam material (Part 1: Listen and Repeat)
-- Ensures exam_family, material, and material_node are all linked correctly

-- 2. Represents a whole practice exam
INSERT INTO material (id, exam_family_id, material_node_id, code, title, description, version, created_at, updated_at)
VALUES (1, 1, null, 'TOEFL_1', 'TOEFL Practice Test 1', 'TOEFL Practice Test 1', 0, now(), now())
ON CONFLICT (id) DO UPDATE SET material_node_id=EXCLUDED.material_node_id, code=EXCLUDED.code, title=EXCLUDED.title, description=EXCLUDED.description, exam_family_id=EXCLUDED.exam_family_id;

-- 4. Insert root node for the speaking section (SECTION)
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    1, NULL, NULL, 'SECTION', 'TOEFL_SPEAKING_ROOT', 'TOEFL Speaking Root', 0,
    4, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- 4b. Update material to reference the root node
UPDATE material SET material_node_id = 1 WHERE id = 1;

-- 5. Insert part 1 node (Listen and Repeat) as PART under root
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES (
    2, 1, NULL, 'PART', 'LISTEN_REPEAT', 'Listen and Repeat', 0,
    4, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, 'SPOKEN', FALSE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{}', 0, now(), now()
)
ON CONFLICT ON CONSTRAINT uq_material_node_sibling_order DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- 6. Insert each sentence as an ITEM node under part 1, each with skill_id=4
INSERT INTO material_node (
    id, parent_node_id, blueprint_node_id, kind, code, title, display_order,
    skill_id, task_type_id, instructions, stimulus_text, transcript_text, explanation_text,
    time_limit_seconds, prep_time_seconds, response_mode, response_required, min_duration_seconds, max_duration_seconds, min_word_count, max_word_count, scoring_mode, max_score, passing_score, config, version, created_at, updated_at
) VALUES
    (3, 2, NULL, 'ITEM', 'Q1', 'Question 1', 0, 4, NULL, NULL, NULL, 'Welcome to the art gallery.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q1.mp3"}', 0, now(), now()),
    (4, 2, NULL, 'ITEM', 'Q2', 'Question 2.', 1, 4, NULL, NULL, NULL, 'A free audio guide is available for all visitors.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q2.mp3"}', 0, now(), now()),
    (5, 2, NULL, 'ITEM', 'Q3', 'Question 3', 2, 4, NULL, NULL, NULL, 'Digital maps can be used for planning your visit.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q3.mp3"}', 0, now(), now()),
    (6, 2, NULL, 'ITEM', 'Q4', 'Question 4', 3, 4, NULL, NULL, NULL, 'If you have questions, just ask a staff member', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q4.mp3"}', 0, now(), now()),
    (7, 2, NULL, 'ITEM', 'Q5', 'Question 5', 4, 4, NULL, NULL, NULL, 'When taking photos, please turn off your flash.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q5.mp3"}', 0, now(), now()),
    (8, 2, NULL, 'ITEM', 'Q6', 'Question 6', 5, 4, NULL, NULL, NULL, 'There’s also a quiet area over here for personal reflection.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q6.mp3"}', 0, now(), now()),
    (9, 2, NULL, 'ITEM', 'Q7', 'Question 7', 6, 4, NULL, NULL, NULL, 'Before leaving the gallery, please make sure to return your audio guide at the entrance.', NULL, NULL, NULL, 'SPOKEN', TRUE, NULL, NULL, NULL, NULL, 'NONE', NULL, NULL, '{"audio_url": "audio/toefl_speaking/q7.mp3"}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    transcript_text=EXCLUDED.transcript_text,
    config=EXCLUDED.config,
    updated_at=now();

-- Question 1
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    11, 10, 'ITEM', 'INTERVIEW_QUESTION_1', 'Q1', 1,
    4, NULL, 'Thank you for speaking with me today. Please think about the last time you read something that was important to you, such as a book, an article, a message, or some information for school or work. What were you reading, and why was it important to you?',
    'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Question 2
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    12, 10, 'ITEM', 'INTERVIEW_QUESTION_2', 'Q2', 2,
    4, NULL, 'People have very different reading habits in daily life. Some people read a little every day, while others only read when they need information. What are your reading habits usually like, and why do you think they developed that way?',
    'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Question 3
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    13, 10, 'ITEM', 'INTERVIEW_QUESTION_3', 'Q3', 3,
    4, NULL, 'Now I’d like your opinion. Some people believe that reading on a screen is just as valuable as reading printed books or papers. Do you agree with that idea? Why or why not?',
    'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Question 4
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    14, 10, 'ITEM', 'INTERVIEW_QUESTION_4', 'Q4', 4,
    4, NULL, 'One last question. Do you think schools and workplaces should encourage people to spend more time reading carefully and less time quickly scrolling through short online content? Why or why not?',
    'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT (id) DO UPDATE SET
    kind=EXCLUDED.kind,
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    instructions=EXCLUDED.instructions,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Example for PART node (repeat for other relevant inserts)
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order,
    skill_id, instructions, transcript_text, response_mode, created_at, updated_at, config, version
)
VALUES (
    20, 2, 'PART', 'LISTEN_REPEAT', 'Listen and Repeat', 0,
    4, NULL, NULL, 'SPOKEN', now(), now(), '{}'::jsonb, 0
)
ON CONFLICT ON CONSTRAINT uq_material_node_sibling_order DO NOTHING;


-- For the root node of the second exam, use id=26 (example):
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order, skill_id, response_mode, config, version, created_at, updated_at
) VALUES (
    26, NULL, 'SECTION', 'TOEFL_SPEAKING_ROOT_2', 'TOEFL Speaking Root 2', 0, 4, 'NONE', '{}', 0, now(), now()
)
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Insert material for TOEFL Practice Test 2
INSERT INTO material (
    id, exam_family_id, code, title, description, material_node_id, version, created_at, updated_at
) VALUES (
    2, 1, 'TOEFL_2', 'TOEFL Practice Test 2', 'TOEFL Practice Test 2', 26, 0, now(), now()
) ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    description=EXCLUDED.description,
    material_node_id=EXCLUDED.material_node_id,
    updated_at=now();

-- Insert PART nodes for TOEFL Practice Test 2
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order, skill_id, response_mode, config, version, created_at, updated_at
) VALUES
    (27, 26, 'PART', 'LISTEN_REPEAT_2', 'Listen and Repeat', 0, 4, 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (28, 26, 'PART', 'TAKE_INTERVIEW_2', 'Take an Interview', 1, 4, 'SPOKEN', '{}'::jsonb, 0, now(), now())
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Insert Listen and Repeat items for TOEFL Practice Test 2 (ids 29-35)
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order, skill_id, transcript_text, response_mode, config, version, created_at, updated_at
) VALUES
    (29, 27, 'ITEM', 'LISTEN_REPEAT_2_Q1', 'Q1', 0, 4, 'Welcome to the campus library entrance.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (30, 27, 'ITEM', 'LISTEN_REPEAT_2_Q2', 'Q2', 1, 4, 'Students can borrow books at this desk.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (31, 27, 'ITEM', 'LISTEN_REPEAT_2_Q3', 'Q3', 2, 4, 'Please scan your student card before using the self-checkout machine.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (32, 27, 'ITEM', 'LISTEN_REPEAT_2_Q4', 'Q4', 3, 4, 'The computers near the window are for research, printing, and homework.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (33, 27, 'ITEM', 'LISTEN_REPEAT_2_Q5', 'Q5', 4, 4, 'Return library books on time to avoid late fees on your account.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (34, 27, 'ITEM', 'LISTEN_REPEAT_2_Q6', 'Q6', 5, 4, 'Group study rooms can be reserved online, but please arrive early so your booking is not canceled.', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (35, 27, 'ITEM', 'LISTEN_REPEAT_2_Q7', 'Q7', 6, 4, 'Before you leave the library, put your chair back, collect your items, and make sure your table is clean.', 'SPOKEN', '{}'::jsonb, 0, now(), now())
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    skill_id=EXCLUDED.skill_id,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();

-- Insert Take an Interview items for TOEFL Practice Test 2 (ids 36-39)
INSERT INTO material_node (
    id, parent_node_id, kind, code, title, display_order, skill_id, transcript_text, response_mode, config, version, created_at, updated_at
) VALUES
    (36, 28, 'ITEM', 'INTERVIEW_QUESTION_1_2', 'Q1', 0, 4, 'Thank you for speaking with me today. Please think about the last time you read something that was important to you, such as a book, an article, a message, or some information for school or work. What were you reading, and why was it important to you?', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (37, 28, 'ITEM', 'INTERVIEW_QUESTION_2_2', 'Q2', 1, 4, 'People have very different reading habits in daily life. Some people read a little every day, while others only read when they need information. What are your reading habits usually like, and why do you think they developed that way?', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (38, 28, 'ITEM', 'INTERVIEW_QUESTION_3_2', 'Q3', 2, 4, 'Now I’d like your opinion. Some people believe that reading on a screen is just as valuable as reading printed books or papers. Do you agree with that idea? Why or why not?', 'SPOKEN', '{}'::jsonb, 0, now(), now()),
    (39, 28, 'ITEM', 'INTERVIEW_QUESTION_4_2', 'Q4', 3, 4, 'One last question. Do you think schools and workplaces should encourage people to spend more time reading carefully and less time quickly scrolling through short online content? Why or why not?', 'SPOKEN', '{}'::jsonb, 0, now(), now())
ON CONFLICT (id) DO UPDATE SET
    code=EXCLUDED.code,
    title=EXCLUDED.title,
    display_order=EXCLUDED.display_order,
    skill_id=EXCLUDED.skill_id,
    transcript_text=EXCLUDED.transcript_text,
    response_mode=EXCLUDED.response_mode,
    config=EXCLUDED.config,
    version=EXCLUDED.version,
    updated_at=now();
