package io.miscellanea.madison.document;

import java.util.StringJoiner;

public record Status(ComponentStatus source, ComponentStatus thumbnail, ComponentStatus text) {
    @Override
    public String toString() {
        return new StringJoiner(", ", Status.class.getSimpleName() + "[", "]")
                .add("source=" + source)
                .add("thumbnail=" + thumbnail)
                .add("text=" + text)
                .toString();
    }
}
