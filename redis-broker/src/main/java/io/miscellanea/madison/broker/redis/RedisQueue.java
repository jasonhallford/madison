package io.miscellanea.madison.broker.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.BrokerException;
import io.miscellanea.madison.broker.Message;
import io.miscellanea.madison.broker.Queue;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisQueue<T extends Message> implements Queue<T> {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(RedisQueue.class);
    private static JedisPool jedisPool;
    private static boolean poolCreated;

    private final BrokerConfig brokerConfig;

    private final String queueName;
    private final ObjectMapper objectMapper;

    // Constructors
    public RedisQueue(@NotNull BrokerConfig brokerConfig, @NotNull String queueName) {
        this.brokerConfig = brokerConfig;
        this.queueName = queueName;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // Initialize a Jedis instance to test the pool configuration
        try (Jedis client = RedisQueue.client(this.brokerConfig)) {
            if (client.isConnected()) {
                logger.debug("Successfully connected to Redis broker.");
            } else {
                throw new BrokerException("Unable to connect to Redis broker.");
            }
        } catch (BrokerException be) {
            throw be;
        } catch (Exception e) {
            throw new BrokerException("Unable to initialize connection to Redis broker.", e);
        }
    }

    // Static methods
    private static Jedis client(BrokerConfig brokerConfig) {
        if (!RedisQueue.poolCreated) {
            synchronized (RedisQueue.class) {
                if (jedisPool == null) {
                    final JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(100);
                    config.setMaxIdle(10);
                    config.setMinIdle(5);
                    config.setTestOnBorrow(true);
                    config.setTestOnReturn(true);
                    config.setTestWhileIdle(true);
                    config.setBlockWhenExhausted(true);

                    if (brokerConfig.user() != null && brokerConfig.password() != null) {
                        jedisPool = new JedisPool(config, brokerConfig.host(), brokerConfig.port(), brokerConfig.user(),
                                brokerConfig.password());
                    } else {
                        jedisPool = new JedisPool(config, brokerConfig.host(), brokerConfig.port());
                    }
                }
            }
        }

        return jedisPool.getResource();
    }

    // WorkQueue
    @Override
    public void publish(T message) throws BrokerException {
        try {
            String json = this.objectMapper.writeValueAsString(message);
            logger.debug("Publishing message to Redis broker: {}", json);
            try (Jedis client = client(this.brokerConfig)) {
                client.lpush(this.queueName, json);
                logger.debug("Message successfully published.");
            }
        } catch (Exception e) {
            throw new BrokerException("Unable to publish message to Redis broker.", e);
        }
    }

    @Override
    public <C extends T> T poll(int timeoutSecs, @NotNull Class<C> asClass) throws BrokerException {
        T message = null;

        try (Jedis client = client(this.brokerConfig)) {
            if (client.isConnected()) {
                List<String> brokerMessage = client.brpop(timeoutSecs, this.queueName);
                if (brokerMessage != null) {
                    String json = brokerMessage.get(1);
                    logger.debug("Deserializing JSON message '{}' to class {}.", json, asClass.getName());
                    message = this.objectMapper.readValue(json, asClass);
                    logger.debug("Successfully deserialized message.");
                } else {
                    logger.debug("Timeout of {} second(s) expired without receiving a message.", timeoutSecs);
                }
            } else {
                logger.warn("Attempt was made to poll Redis broker using closed connection!");
            }
        } catch (Exception e) {
            throw new BrokerException("Unable to retrieve message from Redis broker.", e);
        }

        return message;
    }

    @Override
    public boolean isConnected() throws BrokerException {
        try (Jedis client = client(this.brokerConfig)) {
            return client.isConnected();
        } catch (Exception e) {
            throw new BrokerException("An error occurred while checking broker connection status.", e);
        }
    }

    @Override
    public void close() throws BrokerException {
    }
}
