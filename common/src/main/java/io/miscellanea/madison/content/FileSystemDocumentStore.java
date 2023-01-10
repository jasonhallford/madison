package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class FileSystemDocumentStore implements DocumentStore {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(FileSystemDocumentStore.class);

    private final Path docRoot;

    // Constructors
    @Inject
    public FileSystemDocumentStore(@ContentRoot @NotNull String contentRoot) throws ContentException {
        Path contentPath;
        try {
            contentPath = Path.of(contentRoot);
        } catch (InvalidPathException e) {
            throw new ContentException("'" + contentRoot + "' is not a valid file path.", e);
        }

        if (!contentPath.isAbsolute() || !Files.exists(contentPath)) {
            throw new ContentException("contentRoot must be an absolute path to an existing directory.");
        } else {
            logger.debug("Content root '{}' exists.", contentPath.toString());
        }

        try {
            this.docRoot = contentPath.resolve("documents");

            if (!Files.exists(this.docRoot)) {
                logger.debug("Creating new document directory '{}'.", this.docRoot.toString());
                Files.createDirectory(this.docRoot);
            }
        } catch (IOException e) {
            throw new ContentException("Unable to create document directory.", e);
        }
    }

    // DocumentStore
    @Override
    public URL find(Document document) throws ContentException {
        URL docUrl = null;

        logger.debug("Looking for content for document with fingerprint '{}'.", document.getFingerPrint());
        Path docPath = this.createDocPath(document);

        if (this.exists(document)) {
            try {
                docUrl = docPath.toUri().toURL();
                logger.debug("URL for document with fingerprint '{}' is '{}'.", document.getFingerPrint(),
                        docUrl.toExternalForm());
            } catch (MalformedURLException e) {
                throw new ContentException("Unable to create URL for document with fingerprint '" +
                        document.getFingerPrint() + "'.", e);
            }
        } else {
            logger.warn("Unable to find content for document with fingerprint '{}'.", document.getFingerPrint());
        }

        return docUrl;
    }

    @Override
    public void store(Document document, URL content) throws ContentException {
        logger.debug("Processing request to store document at {} with fingerprint {}.", content.toExternalForm(),
                document.getFingerPrint());

        // Do not overwrite an existing document, just log a warning.
        if (this.exists(document)) {
            logger.warn("A document with fingerprint {} already exists in the store and will not be overwritten.",
                    document.getFingerPrint());
        } else {
            Path docPath = this.createDocPath(document);
            logger.debug("Writing content to store at location '{}'.", docPath);

            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(docPath.toFile()))) {
                try (InputStream in = content.openStream()) {
                    in.transferTo(out);
                    out.flush();
                }
            } catch (Exception e) {
                throw new ContentException("Unable to write document with fingerprint '" + document.getFingerPrint() +
                        "' to content store.", e);
            }

            logger.info("Document with fingerprint '{}' written to content store at location '{}'.",
                    document.getFingerPrint(), docPath);
        }
    }

    @Override
    public boolean delete(Document document) {
        boolean deleted = false;

        if (document != null) {
            Path docPath = this.createDocPath(document);

            if (Files.exists(docPath)) {
                try {
                    Files.delete(docPath);
                    deleted = true;
                } catch (IOException e) {
                    logger.warn("Unable to delete document with fingerprint '{}': {}", document.getFingerPrint(), e.getMessage());
                }
            }
        }

        return deleted;
    }

    @Override
    public boolean exists(Document document) {
        boolean exists = Files.exists(this.createDocPath(document));
        logger.debug("Document fingerprint {} exist: {}", document.getFingerPrint(), exists);

        return exists;
    }

    // Private methods
    private Path createDocPath(Document document) {
        return this.docRoot.resolve(document.getFingerPrint().toLowerCase() + ".mad");
    }
}
