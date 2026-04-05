CREATE UNIQUE INDEX IF NOT EXISTS uq_exam_blueprint_root_order
    ON exam_blueprint_node (blueprint_id, display_order)
    WHERE parent_node_id IS NULL;
