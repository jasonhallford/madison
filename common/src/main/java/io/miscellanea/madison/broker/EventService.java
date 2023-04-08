package io.miscellanea.madison.broker;

import io.miscellanea.madison.service.ServiceException;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public interface EventService extends AutoCloseable {
    void publish(Event event) throws ServiceException;

    void subscribe(@NotNull Consumer<Event> subscriber, Event.Type... forEvents)
            throws ServiceException;


    void accept() throws ServiceException;

    void close() throws ServiceException;
}
