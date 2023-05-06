package io.miscellanea.madison.entity;

public interface EntityMapper<F, T> {
    T map(F from);
}
