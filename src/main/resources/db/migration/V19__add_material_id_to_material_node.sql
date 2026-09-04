ALTER TABLE material_node
    ADD COLUMN material_id BIGINT NOT NULL;

ALTER TABLE material_node
    ADD CONSTRAINT fk_material_node_material
        FOREIGN KEY (material_id)
            REFERENCES material(id)
            ON DELETE CASCADE;

CREATE INDEX idx_material_node_material_id
    ON material_node(material_id);