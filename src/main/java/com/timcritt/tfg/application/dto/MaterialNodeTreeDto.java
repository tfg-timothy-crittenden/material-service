package com.timcritt.tfg.application.dto;

import java.util.List;

public class MaterialNodeTreeDto {
    private Long id;
    private String code;
    private String title;
    private String kind;
    private Integer displayOrder;
    private List<MaterialNodeTreeDto> children;

    public MaterialNodeTreeDto() {}

    public MaterialNodeTreeDto(Long id, String code, String title, String kind, Integer displayOrder, List<MaterialNodeTreeDto> children) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.kind = kind;
        this.displayOrder = displayOrder;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<MaterialNodeTreeDto> getChildren() {
        return children;
    }

    public void setChildren(List<MaterialNodeTreeDto> children) {
        this.children = children;
    }

    public static MaterialNodeTreeDto fromDomain(com.timcritt.tfg.domain.model.MaterialNode node) {
        if (node == null) return null;
        return new MaterialNodeTreeDto(
            node.getId(),
            node.getCode(),
            node.getTitle(),
            node.getKind(),
            node.getDisplayOrder(),
            new java.util.ArrayList<>() // children will be set in tree-building logic
        );
    }
}
