-- Required so PostgreSQL can use (id, material_id)
-- as the target of the composite foreign key below.
ALTER TABLE material_node
    ADD CONSTRAINT uq_material_node_id_material
        UNIQUE (id, material_id);


-- Remove the existing parent_node_id -> material_node(id) FK.
-- PostgreSQL's automatically generated name for your current definition
-- should be material_node_parent_node_id_fkey.
ALTER TABLE material_node
    DROP CONSTRAINT material_node_parent_node_id_fkey;


-- Parent and child must belong to the same Material.
ALTER TABLE material_node
    ADD CONSTRAINT fk_material_node_parent_same_material
        FOREIGN KEY (parent_node_id, material_id)
            REFERENCES material_node(id, material_id)
            ON DELETE CASCADE;


-- A Material may have only one root node.
CREATE UNIQUE INDEX uq_material_node_one_root_per_material
    ON material_node(material_id)
    WHERE parent_node_id IS NULL;