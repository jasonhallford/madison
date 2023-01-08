package io.miscellanea.madison.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventService extends AutoCloseable {
    enum Disposition {
        SUCCESS, FAILURE_RETRY, FAILURE_IGNORE
    }

    void registerHandler(@NotNull Consumer<Event> eventConsumer, Event.Type... forEvents)
            throws EventServiceException;

    boolean handled(Event event, Disposition disposition);

    void accept() throws EventServiceException;
}
