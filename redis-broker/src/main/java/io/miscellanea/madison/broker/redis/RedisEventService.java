package io.miscellanea.madison.broker.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.service.ServiceException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedisEventService implements EventService {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(RedisEventService.class);

    private final Map<Event.Type, List<Consumer<Event>>> handlers = new ConcurrentHashMap<>();
    private Consumer<Event> defaultHandler;
    private final BrokerConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Jedis subClient;
    private final Jedis pubClient;
    private Thread subscriber;

    // Constructors
    public RedisEventService(BrokerConfig config) {
        this.config = config;

        this.subClient = new Jedis(config.host(), config.port());
        this.pubClient = new Jedis(config.host(), config.port());
        logger.debug("Successfully connected to Redis on server '{}' at port {}.", config.host(), config.port());
    }

    // EventService
    @Override
    public void publish(Event event) throws ServiceException {
        if (event != null) {
            try {
                String json = this.objectMapper.writer().writeValueAsString(event);
                this.pubClient.publish(config.topic(), json);
                logger.debug("Published event '{}' to Redis topic '{}'.", json, config.topic());
            } catch (Exception e) {
                throw new ServiceException("Unable to publish event to Redis queue.", e);
            }
        } else {
            logger.warn("Ignoring null event.");
        }
    }

    @Override
    public void subscribe(@NotNull Consumer<Event> subscriber, Event.Type... forEvents) throws ServiceException {
        if (forEvents == null || forEvents.length < 1) {
            this.defaultHandler = subscriber;
        } else {
            for (var evt : forEvents) {
                var handlerList = this.handlers.computeIfAbsent(evt, k -> new ArrayList<>());
                handlerList.add(subscriber);
            }
        }
    }

    @Override
    public void accept() throws ServiceException {
        if (this.subscriber == null || this.subscriber.isInterrupted()) {
            logger.debug("Starting new subscriber thread.");

            JedisPubSub pubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    logger.debug("Recevied message from channel '{}': {}", channel, message);
                    dispatchEvent(channel, message);
                }
            };

            this.subscriber = new Thread(() -> {
                try {
                    logger.debug("Subscribing to event topic '{}'.", config.topic());
                    subClient.subscribe(pubSub, config.topic());
                } catch (Exception e) {
                    logger.debug("Terminating subscriber thread.");
                }
            });
            this.subscriber.start();
            logger.debug("Subscriber thread is listening for events published to '{}'.", config.topic());
        } else {
            logger.debug("Subscriber is already active; ignoring request.");
        }
    }

    @Override
    public void close() throws ServiceException {
        try {
            if (this.subClient.isConnected()) {
                this.subClient.close();
                logger.debug("Closed Redis subscriber connection.");
            }

            if (this.pubClient.isConnected()) {
                this.pubClient.close();
                logger.debug("Closed Redis publisher connection.");
            }

            if (this.subscriber != null) {
                logger.debug("Interrupting subscriber thread and waiting for termination.");
                this.subscriber.interrupt();
                this.subscriber.join();
                logger.debug("Subscriber thread successfully terminated.");
            }
        } catch (Exception e) {
            throw new ServiceException("Unable to close Redis Event Service.", e);
        }

    }

    // Private methods
    private void dispatchEvent(String channel, String message) {
        logger.debug("Received event '{}' on channel '{}'.", message, channel);

        try {
            RedisEvent event = this.objectMapper.readValue(message, RedisEvent.class);

            // Do we have a handler registered for events of this type?
            List<Consumer<Event>> handlers = this.handlers.get(event.getType());
            if (handlers != null) {
                logger.debug("We found {} handler(s) for events of type {}.", handlers.size(), event.getType());

                for (var handler : handlers) {
                    handler.accept(event);
                }
            } else {
                if (this.defaultHandler == null) {
                    logger.debug("No handler registered for event {}; discarding message.", event.getType());
                } else {
                    logger.debug("Dispatching event to default handler.");
                    this.defaultHandler.accept(event);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("An error occurred while dispatching an event to a handler.", e);
        }
    }
}
