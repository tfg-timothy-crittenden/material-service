INSERT INTO material_asset (
    id, material_node_id, kind, storage_key, created_at, updated_at, display_order, metadata, version, title, transcript_text
)
SELECT
    nextval('material_asset_id_seq'), 3, 'AUDIO', 'speaking/listen-repeat/question-audio/question-1.mp3', now(), now(), 1, '{}'::jsonb, 0, 'audio', '"Welcome to the train station."'
WHERE NOT EXISTS (
    SELECT 1 FROM material_asset WHERE material_node_id = 3 AND kind = 'AUDIO'
);

INSERT INTO material_asset (
    id, material_node_id, kind, storage_key, created_at, updated_at, display_order, metadata, version, title, transcript_text
)
SELECT
    nextval('material_asset_id_seq'), 4, 'AUDIO', 'speaking/listen-repeat/question-audio/question-2.mp3', now(), now(), 2, '{}'::jsonb, 0, 'audio', '"Tickets can be purchased from the machines by the entrance"'
WHERE NOT EXISTS (
    SELECT 1 FROM material_asset WHERE material_node_id = 4 AND kind = 'AUDIO'
);

-- Insert Part 2 Interview Questions for TOEFL Speaking Exam
-- Each question is an AUDIO asset for a different material_node (assume node ids 11-14 for part 2 items)

INSERT INTO material_asset (
    id, material_node_id, kind, storage_key, created_at, updated_at, display_order, metadata, version
)
SELECT nextval('material_asset_id_seq'), 11, 'AUDIO', 'speaking/take-interview/question-audio/question-0.mp3', now(), now(), 0, '{}'::jsonb, 0
    WHERE NOT EXISTS (
    SELECT 1 FROM material_asset WHERE material_node_id = 11 AND kind = 'AUDIO'
);

INSERT INTO material_asset (
    id, material_node_id, kind, storage_key, created_at, updated_at, display_order, metadata, version
)
SELECT nextval('material_asset_id_seq'), 12, 'AUDIO', 'speaking/take-interview/question-audio/question-1.mp3', now(), now(), 1, '{}'::jsonb, 0
    WHERE NOT EXISTS (
    SELECT 1 FROM material_asset WHERE material_node_id = 12 AND kind = 'AUDIO'
);


