package io.miscellanea.madison.broker.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.miscellanea.madison.broker.Event;

public class RedisEvent extends Event {
    // Constructors
    @JsonCreator
    public RedisEvent(@JsonProperty("type") Type type, @JsonProperty("payload") String payload) {
        super(type, payload);
    }
}
