package io.miscellanea.madison.importsvc.task;

import io.miscellanea.madison.entity.Document;
import io.miscellanea.madison.content.DocumentStore;
import io.miscellanea.madison.service.EventService;
import io.miscellanea.madison.content.FingerprintGenerator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.function.Supplier;

public class ImportDocument implements Supplier<Document> {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportDocument.class);

    private FingerprintGenerator fingerprintGenerator;
    private DocumentStore documentStore;
    private EventService eventService;
    private URL documentUrl;

    // Constructors
    @Inject
    public ImportDocument(@NotNull FingerprintGenerator fingerprintGenerator, @NotNull DocumentStore documentStore,
                          @NotNull EventService eventService) {
        this.fingerprintGenerator = fingerprintGenerator;
        this.documentStore = documentStore;
        this.eventService = eventService;
    }

    // Properties
    public void setDocumentUrl(URL documentUrl) {
        this.documentUrl = documentUrl;
    }

    // Callable
    @Override
    public Document get() {
        if (this.documentUrl == null) {
            throw new IllegalStateException("Unable to execute task: document URL is null.");
        }
        logger.info("Importing document at {}.", this.documentUrl.toExternalForm());

        String fingerprint = this.fingerprintGenerator.fromUrl(this.documentUrl);
        logger.debug("Fingerprint for document at {} is {}.", this.documentUrl.toExternalForm(), fingerprint);

        Document doc = new Document(fingerprint, "application/pdf");
        documentStore.store(doc, documentUrl);

        return doc;
    }
}
