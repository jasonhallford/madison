package io.miscellanea.madison.importsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import io.miscellanea.madison.entity.Event;
import io.miscellanea.madison.service.EventService;
import io.miscellanea.madison.service.ServiceException;
import io.miscellanea.madison.importsvc.config.ServiceConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RabbitMQEventService implements EventService {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQEventService.class);
    public static final String MADISON_EVENT_EXCHANGE = "madison.event";

    private final Connection brokerConnection;
    private Channel acceptChannel;
    private Channel pulishChannel;
    private final Map<Event.Type, List<Consumer<Event>>> handlers = new ConcurrentHashMap<>();
    private Consumer<Event> defaultHandler;
    private final ServiceConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructors
    @Inject
    public RabbitMQEventService(ServiceConfig config, Connection brokerConnection) {
        this.brokerConnection = brokerConnection;
        this.config = config;
    }

    // EventService methods.
    @Override
    public void registerHandler(@NotNull Consumer<Event> eventConsumer, Event.Type... forEvents)
            throws ServiceException {
        if (forEvents == null || forEvents.length < 1) {
            this.defaultHandler = eventConsumer;
        } else {
            for (var evt : forEvents) {
                var handlerList = this.handlers.get(evt);
                if (handlerList == null) {
                    handlerList = new ArrayList<Consumer<Event>>();
                    this.handlers.put(evt, handlerList);
                }
                handlerList.add(eventConsumer);
            }
        }
    }

    @Override
    public boolean accepted(Event event, Disposition disposition) {
        boolean handled = false;

        if (event instanceof RabbitMQEvent rabbitEvent) {

            try {
                switch (disposition) {
                    case SUCCESS -> {
                        this.acceptChannel.basicAck(rabbitEvent.getDeliveryTag(), false);
                        logger.debug("Successfully acknowledge processing for event {}.", event.getId());
                    }
                    case FAILURE_RETRY -> {
                        this.acceptChannel.basicReject(rabbitEvent.getDeliveryTag(), true);
                        logger.debug("Rejecting and requeueing event {}.", event.getId());
                    }
                    case FAILURE_IGNORE -> {
                        this.acceptChannel.basicReject(rabbitEvent.getDeliveryTag(), false);
                        logger.debug("Rejecting and ignoring event {}.", event.getId());
                    }
                }
                handled = true;
            } catch (Exception e) {
                logger.error("Unable to acknowledge or reject event.", e);
            }
        }

        return handled;
    }

    public void publish(Event event) throws ServiceException {
        // Open the publication channel if it's not already open.
        if (this.pulishChannel == null || !this.pulishChannel.isOpen()) {
            try {
                this.pulishChannel = this.brokerConnection.createChannel();
                this.pulishChannel.exchangeDeclare(MADISON_EVENT_EXCHANGE, "fanout", true);
                logger.debug("Successfully opened broker publish channel and declared event exchange.");
            } catch (Exception e) {
                throw new ServiceException("Unable to open broker publish channel.", e);
            }
        }

        // Write the event to the exchange.
        try {
            String json = this.objectMapper.writer().writeValueAsString(event);
            this.pulishChannel.basicPublish(MADISON_EVENT_EXCHANGE, "", null,
                    json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ServiceException("Unable to publish event to exchange.", e);
        }
    }

    @Override
    public void accept() throws ServiceException {
        // Only accept events if at least one handler is registered.
        if (this.defaultHandler == null && this.handlers.size() < 1) {
            throw new ServiceException("At least one handler must be declared prior to accepting events.");
        }

        // Only open the accept channel if it doesn't already exist or it does exist but isn't
        // open.
        if (this.acceptChannel == null || !this.acceptChannel.isOpen()) {
            // Open a channel and begin listening for messages sent to the event exchange.
            try {
                this.acceptChannel = this.brokerConnection.createChannel();
                this.acceptChannel.exchangeDeclare(MADISON_EVENT_EXCHANGE, "fanout", true);
                logger.debug("Successfully opened broker accept channel and declared event exchange.");

                this.acceptChannel.basicQos(config.brokerConfig().qos());
                String queueName = this.acceptChannel.queueDeclare().getQueue();
                this.acceptChannel.queueBind(queueName, MADISON_EVENT_EXCHANGE, "");
                logger.debug("Successfully bound service queue to the event exchange.");

                this.acceptChannel.basicConsume(queueName, true, this::dispatchEvent, cancelCallback -> {
                });
                logger.debug("Registered dispatch handler and listening for events.");
            } catch (IOException e) {
                throw new ServiceException("Unable to accept events from broker.", e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        // Close the connection to the RabbitMQ broker.
        if (this.acceptChannel != null && this.acceptChannel.isOpen()) {
            this.acceptChannel.close();
            logger.debug("Closed broker accept channel.");
        } else {
            logger.debug("Broker accept channel is already closed.");
        }

        if (this.pulishChannel != null && this.pulishChannel.isOpen()) {
            this.pulishChannel.close();
            logger.debug("Closed broker publish channel.");
        } else {
            logger.debug("Broker publish channel is already closed.");
        }

        if (this.brokerConnection != null && this.brokerConnection.isOpen()) {
            logger.debug("Closing open broker connection.");
            this.brokerConnection.close();
        }
    }

    private void dispatchEvent(String consumerTag, Delivery delivery) {
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
        logger.debug("Received event msg '{}' with delivery tag '{}'", msg, deliveryTag);

        try {
            RabbitMQEvent event = this.objectMapper.readValue(msg, RabbitMQEvent.class);
            event.setDeliveryTag(deliveryTag);

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
                    this.acceptChannel.basicReject(deliveryTag, false);
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
