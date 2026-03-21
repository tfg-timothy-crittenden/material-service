package com.timcritt.tfg.domain.model;

import java.util.Objects;


public class TestItem {
    private Long id;
    private String name;

    public TestItem() {}

    public TestItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public TestItem setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TestItem setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestItem testItem = (TestItem) o;
        return Objects.equals(id, testItem.id) && Objects.equals(name, testItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

