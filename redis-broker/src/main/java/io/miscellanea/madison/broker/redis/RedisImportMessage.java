package io.miscellanea.madison.broker.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.miscellanea.madison.broker.ImportMessage;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class RedisImportMessage extends ImportMessage {
    @JsonCreator
    public RedisImportMessage(@JsonProperty("sender") @NotNull String sender,
                              @JsonProperty("createdAt") @NotNull LocalDateTime createdAt,
                              @JsonProperty("documentUrl") @NotNull String documentUrl) {
        super(sender, createdAt, documentUrl);
    }

}
