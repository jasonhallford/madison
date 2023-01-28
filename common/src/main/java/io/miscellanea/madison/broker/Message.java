package io.miscellanea.madison.broker;

import java.time.LocalDateTime;

public interface Message {
    String getSender();

    LocalDateTime getCreatedAt();
}
