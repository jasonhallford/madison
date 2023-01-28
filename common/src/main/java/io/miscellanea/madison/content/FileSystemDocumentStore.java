package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class FileSystemDocumentStore implements DocumentStore {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(FileSystemDocumentStore.class);

    private final Path documentRoot;
    private final Path thumbnailRoot;

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
            logger.debug("Content root '{}' exists.", contentPath);
        }

        try {
            this.documentRoot = contentPath.resolve("document");

            if (!Files.exists(this.documentRoot)) {
                logger.debug("Creating new document directory '{}'.", this.documentRoot);
                Files.createDirectory(this.documentRoot);
            }
        } catch (IOException e) {
            throw new ContentException("Unable to create document directory.", e);
        }

        try {
            this.thumbnailRoot = contentPath.resolve("thumbnail");

            if (!Files.exists(this.thumbnailRoot)) {
                logger.debug("Creating new thumbnail directory '{}'.", this.thumbnailRoot);
                Files.createDirectory(this.thumbnailRoot);
            }
        } catch (IOException e) {
            throw new ContentException("Unable to create thumbnail directory.", e);
        }
    }

    // DocumentStore
    @Override
    public URL content(Document document) throws ContentException {
        logger.debug("Looking for content for document with fingerprint '{}'.", document.getFingerPrint());
        Path docPath = this.documentPath(document);

        return getContentUrl(document, docPath);
    }

    @Override
    public URL thumbnail(@NotNull Document document) throws ContentException {
        logger.debug("Looking for thumbnail for document with fingerprint '{}'.", document.getFingerPrint());
        Path thumbnailPath = this.thumbnailPath(document);

        return getContentUrl(document, thumbnailPath);
    }

    @Override
    public void store(Document document, BufferedImage thumbnail, URL content) throws ContentException {
        logger.debug("Processing request to store document at {} with fingerprint {}.", content.toExternalForm(),
                document.getFingerPrint());

        // Do not overwrite an existing document, just log a warning.
        if (this.exists(document)) {
            logger.warn("A document with fingerprint {} already exists in the store and will not be overwritten.",
                    document.getFingerPrint());
        } else {
            this.storeDocument(document, content);
            this.storeThumbnail(document, thumbnail);
        }
    }

    @Override
    public boolean delete(@NotNull Document document) {
        boolean deleted = false;

        if (document != null) {
            Path docPath = this.documentPath(document);

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
        boolean exists = Files.exists(this.documentPath(document));
        logger.debug("Document fingerprint {} exist: {}", document.getFingerPrint(), exists);

        return exists;
    }

    // Private methods
    private URL getContentUrl(@NotNull Document document, Path contentPath) {
        URL contentUrl = null;
        if (this.exists(document)) {
            try {
                contentUrl = contentPath.toUri().toURL();
                logger.debug("URL for content with document fingerprint '{}' is '{}'.", document.getFingerPrint(),
                        contentUrl.toExternalForm());
            } catch (MalformedURLException e) {
                throw new ContentException("Unable to create URL for content with document fingerprint '" +
                        document.getFingerPrint() + "'.", e);
            }
        } else {
            logger.warn("Unable to find content for document with fingerprint '{}'.", document.getFingerPrint());
        }

        return contentUrl;
    }

    private void storeDocument(Document document, URL content) {
        // Write the document into the store.
        Path docPath = this.documentPath(document);
        logger.debug("Writing document to store at location '{}'.", docPath);

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

    private void storeThumbnail(Document document, BufferedImage thumbnail) {
        // Write the document into the store.
        Path thumbnailPath = this.thumbnailPath(document);
        logger.debug("Writing thumbnail to store at location '{}'.", thumbnailPath);

        try {
            ImageIO.write(thumbnail, "png", thumbnailPath.toFile());
        } catch (Exception e) {
            throw new ContentException("Unable to write thumbnail to document store.", e);
        }
    }

    private Path documentPath(Document document) {
        return this.documentRoot.resolve(document.getFingerPrint().toLowerCase() + "-doc.pdf");
    }

    private Path thumbnailPath(Document document) {
        return this.thumbnailRoot.resolve(document.getFingerPrint().toLowerCase() + "-tmb.png");
    }
}
