package io.miscellanea.madison.broker;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class DefaultMessage implements Message {
    // Fields
    private final String sender;
    private final LocalDateTime createdAt;

    // Constructors
    public DefaultMessage(@NotNull String sender) {
        this(sender, null);
    }

    public DefaultMessage(@NotNull String sender, LocalDateTime createdAt) {
        this.sender = sender;
        this.createdAt = Objects.requireNonNullElseGet(createdAt, LocalDateTime::now);
    }

    // Message
    @Override
    public String getSender() {
        return this.sender;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
