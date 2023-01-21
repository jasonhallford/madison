package io.miscellanea.madison.broker.redis;

import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.BrokerException;
import io.miscellanea.madison.broker.Message;
import io.miscellanea.madison.broker.WorkQueue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.function.Consumer;

public class RedisWorkQueue implements WorkQueue {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(RedisWorkQueue.class);

    private final BrokerConfig brokerConfig;
    private final Jedis queueClient;

    // Constructors
    public RedisWorkQueue(@NotNull BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.queueClient = new Jedis(brokerConfig.host(), brokerConfig.port());
    }

    // WorkQueue
    @Override
    public void publish(Message message) throws BrokerException {

    }

    @Override
    public void consume(Consumer<Message> consumer) throws BrokerException {

    }
}
