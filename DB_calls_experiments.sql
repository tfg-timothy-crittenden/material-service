WITH RECURSIVE node_tree AS (
    SELECT
        *,
        LPAD(display_order::text, 3, '0') AS path
    FROM material_node
    WHERE id = (SELECT material_node_id FROM material WHERE id = :material_id)
    UNION ALL
    SELECT
        mn.*,
        nt.path || '.' || LPAD(mn.display_order::text, 3, '0')
    FROM material_node mn
             INNER JOIN node_tree nt ON mn.parent_node_id = nt.id
)
SELECT * FROM node_tree
ORDER BY path, id;