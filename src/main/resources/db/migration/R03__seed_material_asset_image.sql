INSERT INTO material_asset (
    id, material_node_id, kind, storage_key, created_at, updated_at, display_order, metadata, version
)
VALUES (
    1, 2, 'IMAGE', 'speaking/listen-repeat/image/library.png', now(), now(), 0, '{}'::jsonb, 0
)
ON CONFLICT (id)
DO UPDATE SET
    storage_key = EXCLUDED.storage_key,
    updated_at = now(),
    display_order = EXCLUDED.display_order,
    metadata = EXCLUDED.metadata,
    version = EXCLUDED.version;


