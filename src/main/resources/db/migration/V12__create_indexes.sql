CREATE INDEX IF NOT EXISTS idx_task_type_exam_family
    ON task_type (exam_family_id);

CREATE INDEX IF NOT EXISTS idx_task_type_skill
    ON task_type (skill_id);


CREATE INDEX IF NOT EXISTS idx_material_exam_family
    ON material (exam_family_id);


CREATE INDEX IF NOT EXISTS idx_material_node_parent
    ON material_node (parent_node_id);



CREATE INDEX IF NOT EXISTS idx_material_node_skill
    ON material_node (skill_id);

CREATE INDEX IF NOT EXISTS idx_material_node_task_type
    ON material_node (task_type_id);

CREATE INDEX IF NOT EXISTS idx_material_node_config_gin
    ON material_node USING GIN ((config::jsonb));

CREATE INDEX IF NOT EXISTS idx_material_asset_material_node
    ON material_asset (material_node_id);

CREATE INDEX IF NOT EXISTS idx_material_asset_metadata_gin
    ON material_asset USING GIN ((metadata::jsonb));

