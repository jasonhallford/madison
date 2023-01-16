package io.miscellanea.madison.broker;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;

public class Event {
    // Enum of known types
    public enum Type {
        IMPORT_DOCUMENT, IMPORT_SCAN
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("payload", payload)
                .append("id", id)
                .toString();
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
