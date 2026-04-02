CREATE UNIQUE INDEX uq_material_node_root_order
    ON material_node (material_version_id, display_order)
    WHERE parent_node_id IS NULL;