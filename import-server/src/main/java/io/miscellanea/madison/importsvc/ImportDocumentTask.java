package io.miscellanea.madison.importsvc;

import io.miscellanea.madison.content.*;
import io.miscellanea.madison.entity.Document;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.repository.DocumentRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

public class ImportDocumentTask implements Supplier<Document> {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportDocumentTask.class);

    private final FingerprintGenerator fingerprintGenerator;
    private final DocumentStore documentStore;
    private final MetadataExtractor metadataExtractor;
    private final EventService eventService;
    private final DocumentRepository documentRepository;
    private final ThumbnailGenerator thumbnailGenerator;

    private URL documentUrl;

    // Constructors
    @Inject
    public ImportDocumentTask(@NotNull FingerprintGenerator fingerprintGenerator, @NotNull DocumentStore documentStore,
                              @NotNull MetadataExtractor metadataExtractor, @NotNull ThumbnailGenerator thumbnailGenerator,
                              @NotNull DocumentRepository documentRepository, @NotNull EventService eventService) {
        this.fingerprintGenerator = fingerprintGenerator;
        this.documentStore = documentStore;
        this.metadataExtractor = metadataExtractor;
        this.thumbnailGenerator = thumbnailGenerator;
        this.documentRepository = documentRepository;
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

        try (InputStream docStream = this.documentUrl.openStream()) {
            Document doc = this.metadataExtractor.fromStream(fingerprint, docStream);
            BufferedImage thumbnail = this.thumbnailGenerator.generate(documentUrl);

            this.documentStore.store(doc, thumbnail, documentUrl);
            this.documentRepository.add(doc);

            return doc;
        } catch (ContentException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ContentException("Unable to extract metadata from document at URL " +
                    this.documentUrl.toExternalForm() + ".", e);
        } finally {
            this.documentRepository.close();
        }
    }
}
