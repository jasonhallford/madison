package io.miscellanea.madison.broker;

import io.miscellanea.madison.service.ServiceException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventService extends AutoCloseable {
    void publish(Event event) throws ServiceException;

    void subscribe(@NotNull Consumer<Event> subscriber, Event.Type... forEvents)
            throws ServiceException;


    void accept() throws ServiceException;

    void close() throws ServiceException;
}
