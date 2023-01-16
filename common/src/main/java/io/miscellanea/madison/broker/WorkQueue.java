package io.miscellanea.madison.broker;

import java.util.function.Consumer;

public interface WorkQueue {
    void publish(UnitOfWork unitOfWork) throws BrokerException;

    void consume(Consumer<UnitOfWork> consumer) throws BrokerException;
}
