package io.miscellanea.madison.event;

import java.util.concurrent.atomic.AtomicLong;

public class Event {
    // Enum of known types
    public enum Type {
        IMPORT, IMPORT_ALL
    }

    // Fields
    private static final AtomicLong idCounter = new AtomicLong(0);

    private final Type type;
    private final String payload;
    private final long id;

    // Constructors
    public Event(Type type) {
        this(type, null);
    }

    public Event(Type type, String payload) {
        this.id = Event.idCounter.incrementAndGet();
        this.type = type;
        this.payload = payload == null ? "" : payload;
    }

    // Methods
    public Type getType() {
        return this.type;
    }

    public String getPayload() {
        return this.payload;
    }

    public long getId() {
        return this.id;
    }
}
