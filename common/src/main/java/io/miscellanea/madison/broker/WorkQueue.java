package io.miscellanea.madison.broker;

import java.util.function.Consumer;

public interface WorkQueue {
    void publish(Message message) throws BrokerException;

    void consume(Consumer<Message> consumer) throws BrokerException;
}
