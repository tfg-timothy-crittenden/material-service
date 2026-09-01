-- Repeatable seed for 100 dummy TOEFL Speaking tests.
-- Each test contains:
--   * 1 material row
--   * 1 SECTION root node
--   * 2 PART nodes
--   * 11 ITEM question nodes (7 for Part 1, 4 for Part 2)
--   * 12 assets (1 image + 11 audio files)

INSERT INTO material (
    id,
    exam_family_id,
    material_node_id,
    title,
    description,
    author_id,
    owner_org_id,
    status,
    version,
    created_at,
    updated_at
)
SELECT
    10000 + n AS id,
    1 AS exam_family_id,
    20000 + (n * 100) AS material_node_id,
    concat('TOEFL Speaking Test ', n) AS title,
    concat('Dummy TOEFL speaking test #', n, ' generated for local database seeding.') AS description,
    NULL AS author_id,
    NULL AS owner_org_id,
    'PUBLISHED' AS status,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM generate_series(1, 100) AS gs(n)
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_node (
    id,
    parent_node_id,
    kind,
    title,
    display_order,
    skill_id,
    task_type_id,
    instructions,
    stimulus_text,
    transcript_text,
    explanation_text,
    time_limit_seconds,
    prep_time_seconds,
    response_mode,
    response_required,
    min_duration_seconds,
    max_duration_seconds,
    min_word_count,
    max_word_count,
    scoring_mode,
    max_score,
    passing_score,
    config,
    version,
    created_at,
    updated_at
)
SELECT
    20000 + (n * 100) AS id,
    NULL AS parent_node_id,
    'SECTION' AS kind,
    concat('TOEFL Speaking Test ', n) AS title,
    0 AS display_order,
    3 AS skill_id,
    NULL AS task_type_id,
    NULL AS instructions,
    NULL AS stimulus_text,
    NULL AS transcript_text,
    NULL AS explanation_text,
    NULL AS time_limit_seconds,
    NULL AS prep_time_seconds,
    'NONE' AS response_mode,
    FALSE AS response_required,
    NULL AS min_duration_seconds,
    NULL AS max_duration_seconds,
    NULL AS min_word_count,
    NULL AS max_word_count,
    'NONE' AS scoring_mode,
    NULL AS max_score,
    NULL AS passing_score,
    '{}'::jsonb AS config,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM generate_series(1, 100) AS gs(n)
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_node (
    id,
    parent_node_id,
    kind,
    title,
    display_order,
    skill_id,
    task_type_id,
    instructions,
    stimulus_text,
    transcript_text,
    explanation_text,
    time_limit_seconds,
    prep_time_seconds,
    response_mode,
    response_required,
    min_duration_seconds,
    max_duration_seconds,
    min_word_count,
    max_word_count,
    scoring_mode,
    max_score,
    passing_score,
    config,
    version,
    created_at,
    updated_at
)
SELECT
    root_id + 1 AS id,
    root_id AS parent_node_id,
    'PART' AS kind,
    'Part 1' AS title,
    0 AS display_order,
    3 AS skill_id,
    NULL AS task_type_id,
    'Dummy prompt for Part 1.' AS instructions,
    NULL AS stimulus_text,
    NULL AS transcript_text,
    NULL AS explanation_text,
    NULL AS time_limit_seconds,
    NULL AS prep_time_seconds,
    'NONE' AS response_mode,
    FALSE AS response_required,
    NULL AS min_duration_seconds,
    NULL AS max_duration_seconds,
    NULL AS min_word_count,
    NULL AS max_word_count,
    'NONE' AS scoring_mode,
    NULL AS max_score,
    NULL AS passing_score,
    '{}'::jsonb AS config,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM (
    SELECT 20000 + (n * 100) AS root_id
    FROM generate_series(1, 100) AS gs(n)
) AS roots
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_node (
    id,
    parent_node_id,
    kind,
    title,
    display_order,
    skill_id,
    task_type_id,
    instructions,
    stimulus_text,
    transcript_text,
    explanation_text,
    time_limit_seconds,
    prep_time_seconds,
    response_mode,
    response_required,
    min_duration_seconds,
    max_duration_seconds,
    min_word_count,
    max_word_count,
    scoring_mode,
    max_score,
    passing_score,
    config,
    version,
    created_at,
    updated_at
)
SELECT
    root_id + 2 AS id,
    root_id AS parent_node_id,
    'PART' AS kind,
    'Part 2' AS title,
    1 AS display_order,
    3 AS skill_id,
    NULL AS task_type_id,
    'Dummy prompt for Part 2.' AS instructions,
    NULL AS stimulus_text,
    NULL AS transcript_text,
    NULL AS explanation_text,
    NULL AS time_limit_seconds,
    NULL AS prep_time_seconds,
    'NONE' AS response_mode,
    FALSE AS response_required,
    NULL AS min_duration_seconds,
    NULL AS max_duration_seconds,
    NULL AS min_word_count,
    NULL AS max_word_count,
    'NONE' AS scoring_mode,
    NULL AS max_score,
    NULL AS passing_score,
    '{}'::jsonb AS config,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM (
    SELECT 20000 + (n * 100) AS root_id
    FROM generate_series(1, 100) AS gs(n)
) AS roots
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_node (
    id,
    parent_node_id,
    kind,
    title,
    display_order,
    skill_id,
    task_type_id,
    instructions,
    stimulus_text,
    transcript_text,
    explanation_text,
    time_limit_seconds,
    prep_time_seconds,
    response_mode,
    response_required,
    min_duration_seconds,
    max_duration_seconds,
    min_word_count,
    max_word_count,
    scoring_mode,
    max_score,
    passing_score,
    config,
    version,
    created_at,
    updated_at
)
SELECT
    root_id + 10 + question_offset AS id,
    root_id + 1 AS parent_node_id,
    'ITEM' AS kind,
    concat('Question ', question_offset + 1) AS title,
    question_offset AS display_order,
    3 AS skill_id,
    NULL AS task_type_id,
    NULL AS instructions,
    NULL AS stimulus_text,
    concat('Dummy Part 1 transcript for test ', test_number, ', question ', question_offset + 1, '.') AS transcript_text,
    NULL AS explanation_text,
    60 AS time_limit_seconds,
    15 AS prep_time_seconds,
    'SPOKEN' AS response_mode,
    TRUE AS response_required,
    NULL AS min_duration_seconds,
    NULL AS max_duration_seconds,
    NULL AS min_word_count,
    NULL AS max_word_count,
    'NONE' AS scoring_mode,
    NULL AS max_score,
    NULL AS passing_score,
    jsonb_build_object('dummy', true, 'testNumber', test_number, 'part', 1, 'question', question_offset + 1) AS config,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM (
    SELECT
        n AS test_number,
        20000 + (n * 100) AS root_id,
        q AS question_offset
    FROM generate_series(1, 100) AS tests(n)
    CROSS JOIN generate_series(0, 6) AS questions(q)
) AS part1_questions
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_node (
    id,
    parent_node_id,
    kind,
    title,
    display_order,
    skill_id,
    task_type_id,
    instructions,
    stimulus_text,
    transcript_text,
    explanation_text,
    time_limit_seconds,
    prep_time_seconds,
    response_mode,
    response_required,
    min_duration_seconds,
    max_duration_seconds,
    min_word_count,
    max_word_count,
    scoring_mode,
    max_score,
    passing_score,
    config,
    version,
    created_at,
    updated_at
)
SELECT
    root_id + 20 + question_offset AS id,
    root_id + 2 AS parent_node_id,
    'ITEM' AS kind,
    concat('Question ', question_offset + 1) AS title,
    question_offset AS display_order,
    3 AS skill_id,
    NULL AS task_type_id,
    NULL AS instructions,
    NULL AS stimulus_text,
    concat('Dummy Part 2 transcript for test ', test_number, ', question ', question_offset + 1, '.') AS transcript_text,
    NULL AS explanation_text,
    60 AS time_limit_seconds,
    15 AS prep_time_seconds,
    'SPOKEN' AS response_mode,
    TRUE AS response_required,
    NULL AS min_duration_seconds,
    NULL AS max_duration_seconds,
    NULL AS min_word_count,
    NULL AS max_word_count,
    'NONE' AS scoring_mode,
    NULL AS max_score,
    NULL AS passing_score,
    jsonb_build_object('dummy', true, 'testNumber', test_number, 'part', 2, 'question', question_offset + 1) AS config,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM (
    SELECT
        n AS test_number,
        20000 + (n * 100) AS root_id,
        q AS question_offset
    FROM generate_series(1, 100) AS tests(n)
    CROSS JOIN generate_series(0, 3) AS questions(q)
) AS part2_questions
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_asset (
    id,
    material_node_id,
    kind,
    storage_key,
    original_filename,
    mime_type,
    file_size_bytes,
    title,
    transcript_text,
    display_order,
    metadata,
    version,
    created_at,
    updated_at
)
SELECT
    40000 + n AS id,
    20000 + (n * 100) + 1 AS material_node_id,
    'IMAGE' AS kind,
    concat('speaking/', 10000 + n, '/part1/image/image.png') AS storage_key,
    concat('part1-', n, '.png') AS original_filename,
    'image/png' AS mime_type,
    1 AS file_size_bytes,
    concat('Part 1 image for test ', n) AS title,
    NULL AS transcript_text,
    0 AS display_order,
    '{}'::jsonb AS metadata,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM generate_series(1, 100) AS gs(n)
ON CONFLICT (id) DO NOTHING;

INSERT INTO material_asset (
    id,
    material_node_id,
    kind,
    storage_key,
    original_filename,
    mime_type,
    file_size_bytes,
    title,
    transcript_text,
    display_order,
    metadata,
    version,
    created_at,
    updated_at
)
SELECT
    41000 + (test_number * 100) + (part_number * 10) + question_number AS id,
    CASE WHEN part_number = 1 THEN root_id + 10 + (question_number - 1) ELSE root_id + 20 + (question_number - 1) END AS material_node_id,
    'AUDIO' AS kind,
    concat('speaking/', 10000 + test_number, '/part', part_number, '/audio/question_', question_number, '.mp3') AS storage_key,
    concat('question-', test_number, '-', part_number, '-', question_number, '.mp3') AS original_filename,
    'audio/mpeg' AS mime_type,
    1 AS file_size_bytes,
    concat('Question ', question_number, ' audio') AS title,
    NULL AS transcript_text,
    0 AS display_order,
    jsonb_build_object('dummy', true, 'testNumber', test_number, 'part', part_number, 'question', question_number) AS metadata,
    0 AS version,
    now() AS created_at,
    now() AS updated_at
FROM (
    SELECT
        n AS test_number,
        20000 + (n * 100) AS root_id,
        1 AS part_number,
        q AS question_number
    FROM generate_series(1, 100) AS tests(n)
    CROSS JOIN generate_series(1, 7) AS questions(q)

    UNION ALL

    SELECT
        n AS test_number,
        20000 + (n * 100) AS root_id,
        2 AS part_number,
        q AS question_number
    FROM generate_series(1, 100) AS tests(n)
    CROSS JOIN generate_series(1, 4) AS questions(q)
) AS audio_rows
ON CONFLICT (id) DO NOTHING;
