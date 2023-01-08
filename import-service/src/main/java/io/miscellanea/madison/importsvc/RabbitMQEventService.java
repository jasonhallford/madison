package io.miscellanea.madison.importsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import io.miscellanea.madison.event.Event;
import io.miscellanea.madison.event.EventService;
import io.miscellanea.madison.event.EventServiceException;
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

    private Connection brokerConnection;
    private Channel brokerChannel;
    private Map<Event.Type, List<Consumer<Event>>> handlers = new ConcurrentHashMap<>();
    private Consumer<Event> defaultHandler;
    private ServiceConfig config;
    private ObjectMapper objectMapper = new ObjectMapper();

    // Constructors
    @Inject
    public RabbitMQEventService(ServiceConfig config, Connection brokerConnection) {
        this.brokerConnection = brokerConnection;
        this.config = config;
    }

    // EventService methods.
    @Override
    public void registerHandler(@NotNull Consumer<Event> eventConsumer, Event.Type... forEvents)
            throws EventServiceException {
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
    public boolean handled(Event event, Disposition disposition) {
        boolean handled = false;

        if (event instanceof RabbitMQEvent) {
            var rabbitEvent = (RabbitMQEvent) event;

            try {
                switch (disposition) {
                    case SUCCESS:
                        this.brokerChannel.basicAck(rabbitEvent.getDeliveryTag(), false);
                        logger.debug("Successfully acknowledge processing for event {}.", event.getId());
                        break;
                    case FAILURE_RETRY:
                        this.brokerChannel.basicReject(rabbitEvent.getDeliveryTag(), true);
                        logger.debug("Rejecting and requeueing event {}.", event.getId());
                        break;
                    case FAILURE_IGNORE:
                        this.brokerChannel.basicReject(rabbitEvent.getDeliveryTag(), false);
                        logger.debug("Rejecting and ignoring event {}.", event.getId());
                        break;
                }
                handled = true;
            } catch (Exception e) {
                logger.error("Unable to acknowledge or reject event.", e);
            }
        }

        return handled;
    }

    @Override
    public void accept() throws EventServiceException {
        // Only accept events if at least one handler is registered.
        if (this.defaultHandler == null && this.handlers.size() < 1) {
            throw new EventServiceException("At least one handler must be declared prior to accepting events.");
        }

        // Open a channel and begin listening for messages sent to the event exchange.
        try {
            this.brokerChannel = this.brokerConnection.createChannel();
            logger.debug("Successfully created RabbitMQ channel.");

            this.brokerChannel.exchangeDeclare("madison.event", "fanout", true);
            this.brokerChannel.basicQos(config.brokerConfig().qos());
            String queueName = this.brokerChannel.queueDeclare().getQueue();
            this.brokerChannel.queueBind(queueName, "madison.event", "");
            logger.debug("Successfully declared madison.event exchange and bound service queue.");

            this.brokerChannel.basicConsume(queueName, true, this::dispatchEvent, cancelCallback -> {
            });
            logger.debug("Registered dispatch handler and listening for events.");
        } catch (IOException e) {
            throw new EventServiceException("Unable to accept events from broker.", e);
        }
    }

    @Override
    public void close() throws Exception {
        // Close the connection to the RabbitMQ broker.
        if (this.brokerChannel != null && this.brokerChannel.isOpen()) {
            logger.debug("Closing open broken channel.");
            this.brokerChannel.close();
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

                for( var handler : handlers ){
                    handler.accept(event);
                }
            } else {
                if (this.defaultHandler == null) {
                    logger.debug("No handler registered for event {}; discarding message.", event.getType());
                    this.brokerChannel.basicReject(deliveryTag, false);
                } else {
                    logger.debug("Dispatching event to default handler.");
                    this.defaultHandler.accept(event);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
