package io.miscellanea.madison.broker.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.miscellanea.madison.broker.ImportMessage;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public class RedisImportMessage extends ImportMessage {
  @JsonCreator
  public RedisImportMessage(
      @JsonProperty("sender") @NotNull String sender,
      @JsonProperty("createdAt") @NotNull LocalDateTime createdAt,
      @JsonProperty("documentUrl") @NotNull String documentUrl) {
    super(sender, createdAt, documentUrl);
  }
}
