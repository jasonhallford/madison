package io.miscellanea.madison.event;

public class Event {
    // Enum of known types
    public enum Type {
        IMPORT, IMPORT_ALL
    }

    // Fields
    private final Type type;
    private final String payload;

    // Constructors
    public Event(Type type) {
        this(type, null);
    }

    public Event(Type type, String payload) {
        this.type = type;
        this.payload = payload == null ? "" : payload;
    }

    // Methods
    public Type getType() {
        return this.type;
    }

    public String payload() {
        return this.payload;
    }
}
