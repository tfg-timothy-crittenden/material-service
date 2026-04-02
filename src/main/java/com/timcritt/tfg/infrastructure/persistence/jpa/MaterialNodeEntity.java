package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "material_node")
public class MaterialNodeEntity {
    @Id
    private Long id;
    // ...other fields as needed...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}

