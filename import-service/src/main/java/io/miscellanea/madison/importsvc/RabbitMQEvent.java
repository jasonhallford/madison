package io.miscellanea.madison.importsvc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.miscellanea.madison.event.Event;

public class RabbitMQEvent extends Event {
    // Fields
    private long deliveryTag;

    // Constructors
    @JsonCreator
    public RabbitMQEvent(@JsonProperty("type") Type type, @JsonProperty("payload") String payload) {
        super(type, payload);
    }

    public RabbitMQEvent(Type type, String payload, long deliveryTag) {
        super(type, payload);
        this.deliveryTag = deliveryTag;
    }

    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public long getDeliveryTag() {
        return this.deliveryTag;
    }
}
