package io.miscellanea.madison.service;

import io.miscellanea.madison.entity.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventService extends AutoCloseable {
    enum Disposition {
        SUCCESS, FAILURE_RETRY, FAILURE_IGNORE
    }

    void publish(Event event) throws ServiceException;

    void registerHandler(@NotNull Consumer<Event> eventConsumer, Event.Type... forEvents)
            throws ServiceException;

    boolean accepted(Event event, Disposition disposition);

    void accept() throws ServiceException;
}
