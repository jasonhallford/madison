package io.miscellanea.madison.broker;

import org.jetbrains.annotations.NotNull;

public interface Queue<T extends Message> extends AutoCloseable {
    void publish(T message) throws BrokerException;

    <C extends T> T poll(int timeoutSecs, @NotNull Class<C> asClass) throws BrokerException;

    boolean isConnected();

    void close() throws BrokerException;
}
