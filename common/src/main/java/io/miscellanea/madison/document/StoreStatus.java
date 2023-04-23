package io.miscellanea.madison.document;

import java.util.StringJoiner;

public record StoreStatus(ComponentStatus source, ComponentStatus thumbnail, ComponentStatus text) {
    @Override
    public String toString() {
        return new StringJoiner(", ", StoreStatus.class.getSimpleName() + "[", "]")
                .add("source=" + source)
                .add("thumbnail=" + thumbnail)
                .add("text=" + text)
                .toString();
    }
}
