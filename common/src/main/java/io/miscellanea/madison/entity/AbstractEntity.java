package io.miscellanea.madison.entity;

import java.util.Objects;

public abstract class AbstractEntity {
    // Fields
    private Long id;

    // Constructors
    public AbstractEntity() {

    }

    public AbstractEntity(Long id) {
        this.id = id;
    }

    // Properties
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEntity that = (AbstractEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
