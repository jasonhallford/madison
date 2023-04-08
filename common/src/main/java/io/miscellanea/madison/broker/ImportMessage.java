package io.miscellanea.madison.broker;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

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
