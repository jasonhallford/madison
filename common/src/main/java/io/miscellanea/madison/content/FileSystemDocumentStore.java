package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class FileSystemDocumentStore implements DocumentStore {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(FileSystemDocumentStore.class);
    public static final String DOCUMENT_COMPONENT = "document";
    public static final String THUMBNAIL_COMPONENT = "thumbnail";
    public static final String TEXT_COMPONENT = "text";
    public static final String DOCUMENT_COMPONENT_EXTENSION = ".document";
    public static final String THUMBNAIL_COMPONENT_EXTENSION = ".thumbnail";
    public static final String TEXT_COMPONENT_EXTENSION = ".text";

    private final Path documentRoot;
    private final Path thumbnailRoot;
    private final Path textRoot;

    // Constructors
    public FileSystemDocumentStore(@NotNull String storageRoot) throws ContentException {
        Path storagePath;
        try {
            storagePath = Path.of(storageRoot);
        } catch (InvalidPathException e) {
            throw new ContentException("'" + storageRoot + "' is not a valid file path.", e);
        }

        if (!storagePath.isAbsolute() || !Files.exists(storagePath)) {
            throw new ContentException("contentRoot must be an absolute path to an existing directory.");
        } else {
            logger.debug("Content root '{}' exists.", storagePath);
        }

        this.documentRoot = this.createStorageComponentDirectory(storagePath, DOCUMENT_COMPONENT);
        this.thumbnailRoot = this.createStorageComponentDirectory(storagePath, THUMBNAIL_COMPONENT);
        this.textRoot = this.createStorageComponentDirectory(storagePath, TEXT_COMPONENT);
    }

    // DocumentStore
    @Override
    public URL sourceURL(@NotNull Document document) throws ContentException {
        logger.debug("Looking for source for document with fingerprint '{}'.", document.getFingerprint());
        Path docPath = this.componentPath(document, DOCUMENT_COMPONENT);

        return getContentUrl(document, docPath);
    }

    @Override
    public URL thumbnailURL(@NotNull Document document) throws ContentException {
        logger.debug("Looking for thumbnail for document with fingerprint '{}'.", document.getFingerprint());
        Path thumbnailPath = this.componentPath(document, THUMBNAIL_COMPONENT);

        return getContentUrl(document, thumbnailPath);
    }

    @Override
    public URL textURL(@NotNull Document document) throws ContentException {
        logger.debug("Looking for text for document with fingerprint '{}'.", document.getFingerprint());
        Path textPath = this.componentPath(document, TEXT_COMPONENT);

        return getContentUrl(document, textPath);
    }

    @Override
    public synchronized void store(@NotNull Document document, BufferedImage thumbnail, String content, URL source) throws ContentException {
        logger.debug("Processing request to store document at {} with fingerprint {}.", source.toExternalForm(),
                document.getFingerprint());

        // Do not overwrite an existing document, just log a warning.
        if (this.exists(document)) {
            logger.warn("A document with fingerprint {} already exists in the store and will not be overwritten.",
                    document.getFingerprint());
        } else {
            this.storeDocument(document, source);
            this.storeThumbnail(document, thumbnail);
            this.storeText(document, content);
        }
    }

    @Override
    public synchronized boolean delete(@NotNull Document document) {
        boolean deleted = false;

        try {
            deleted = this.deleteContent(this.componentPath(document, DOCUMENT_COMPONENT));
            if (deleted) {
                deleted = this.deleteContent(this.componentPath(document, THUMBNAIL_COMPONENT));
            }
            if (deleted) {
                deleted = this.deleteContent(this.componentPath(document, TEXT_COMPONENT));
            }
        } catch (Exception e) {
            logger.error("Unable to delete document with fingerprint " + document.getFingerprint() +
                    "; returning false.", e);
        }

        return deleted;
    }

    @Override
    public synchronized boolean exists(@NotNull Document document) {
        boolean exists = Files.exists(this.componentPath(document, DOCUMENT_COMPONENT));
        logger.debug("Document fingerprint {} exist: {}", document.getFingerprint(), exists);

        return exists;
    }

    // Private methods
    private Path createStorageComponentDirectory(Path root, String component) throws ContentException {
        Path path;

        try {
            path = root.resolve(component);

            if (!Files.exists(path)) {
                logger.debug("Creating new storage component directory '{}'.", path);
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            throw new ContentException("Unable to create storage component directory.", e);
        }

        return path;
    }

    private boolean deleteContent(@NotNull Path atPath) throws ContentException {
        if (Files.exists(atPath)) {
            try {
                Files.delete(atPath);
                return true;
            } catch (IOException e) {
                throw new ContentException("Unable to delete content at path '" + atPath + "'.", e);
            }
        } else {
            return false;
        }
    }

    private URL getContentUrl(@NotNull Document document, Path contentPath) {
        URL contentUrl = null;
        if (this.exists(document)) {
            try {
                contentUrl = contentPath.toUri().toURL();
                logger.debug("URL for content with document fingerprint '{}' is '{}'.", document.getFingerprint(),
                        contentUrl.toExternalForm());
            } catch (MalformedURLException e) {
                throw new ContentException("Unable to create URL for content with document fingerprint '" +
                        document.getFingerprint() + "'.", e);
            }
        } else {
            logger.warn("Unable to find content for document with fingerprint '{}'.", document.getFingerprint());
        }

        return contentUrl;
    }

    private void storeDocument(Document document, URL content) {
        // Write the document into the store.
        Path docPath = this.componentPath(document, DOCUMENT_COMPONENT);
        logger.debug("Writing document to store at location '{}'.", docPath);

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(docPath.toFile()))) {
            try (InputStream in = content.openStream()) {
                in.transferTo(out);
                out.flush();
            }
        } catch (Exception e) {
            throw new ContentException("Unable to write document with fingerprint '" + document.getFingerprint() +
                    "' to content store.", e);
        }

        logger.info("Document with fingerprint '{}' written to content store at location '{}'.",
                document.getFingerprint(), docPath);
    }

    private void storeThumbnail(Document document, BufferedImage thumbnail) {
        Path thumbnailPath = this.componentPath(document, THUMBNAIL_COMPONENT);
        logger.debug("Writing thumbnail to store at location '{}'.", thumbnailPath);

        try {
            ImageIO.write(thumbnail, "png", thumbnailPath.toFile());
        } catch (Exception e) {
            throw new ContentException("Unable to write thumbnail to document store.", e);
        }
    }

    private void storeText(Document document, String content) {
        Path contentPath = this.componentPath(document, TEXT_COMPONENT);
        logger.debug("Writing content to store at location '{}'.", contentPath);

        try (GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(contentPath.toFile())))) {
            try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                in.transferTo(out);
                out.flush();
            }
        } catch (Exception e) {
            throw new ContentException("Unable to write document with fingerprint '" + document.getFingerprint() +
                    "' to content store.", e);
        }
    }

    private Path componentPath(Document document, String component) {
        return switch (component) {
            case DOCUMENT_COMPONENT -> this.documentRoot.resolve(document.getFingerprint().toLowerCase() +
                    DOCUMENT_COMPONENT_EXTENSION);
            case THUMBNAIL_COMPONENT -> this.thumbnailRoot.resolve(document.getFingerprint().toLowerCase() +
                    THUMBNAIL_COMPONENT_EXTENSION);
            case TEXT_COMPONENT -> this.textRoot.resolve(document.getFingerprint().toLowerCase() +
                    TEXT_COMPONENT_EXTENSION);
            default -> throw new ContentException("Unknown repository component '" + component + ".");
        };
    }
}
