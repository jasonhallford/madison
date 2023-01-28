package io.miscellanea.madison.broker;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class ImportMessage extends DefaultMessage {
    // Fields
    private final String documentUrl;

    // Constructors
    public ImportMessage(@NotNull String sender, @NotNull String documentUrl) {
        super(sender);
        this.documentUrl = documentUrl;
    }

    public ImportMessage(@NotNull String sender, @NotNull LocalDateTime createdAt, @NotNull String documentUrl) {
        super(sender, createdAt);
        this.documentUrl = documentUrl;
    }

    // Fields
    public String getDocumentUrl() {
        return this.documentUrl;
    }
}
