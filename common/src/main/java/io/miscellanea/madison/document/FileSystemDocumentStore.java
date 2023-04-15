package io.miscellanea.madison.document;

import io.miscellanea.madison.content.ContentException;
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
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

public class FileSystemDocumentStore implements DocumentStore {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(FileSystemDocumentStore.class);
    public static final String SOURCE_COMPONENT = "document";
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

        this.documentRoot = this.createComponentDirectory(storagePath, SOURCE_COMPONENT);
        this.thumbnailRoot = this.createComponentDirectory(storagePath, THUMBNAIL_COMPONENT);
        this.textRoot = this.createComponentDirectory(storagePath, TEXT_COMPONENT);
    }

    // DocumentStore
    @Override
    public URL sourceURL(@NotNull Fingerprint fingerprint) throws ContentException {
        logger.debug("Looking for source for document with fingerprint '{}'.", fingerprint);
        return getContentUrl(fingerprint, SOURCE_COMPONENT);
    }

    @Override
    public URL thumbnailURL(@NotNull Fingerprint fingerprint) throws ContentException {
        logger.debug("Looking for thumbnail for document with fingerprint '{}'.", fingerprint);
        return getContentUrl(fingerprint, THUMBNAIL_COMPONENT);
    }

    @Override
    public URL textURL(@NotNull Fingerprint fingerprint) throws ContentException {
        logger.debug("Looking for text for document with fingerprint '{}'.", fingerprint);
        return getContentUrl(fingerprint, TEXT_COMPONENT);
    }

    @Override
    public void storeSource(@NotNull Fingerprint fingerprint, InputStream source)
            throws ContentException {
        // Write the document into the store.
        Path sourcePath = this.componentPath(fingerprint, SOURCE_COMPONENT);
        logger.debug("Writing source for {} to store at '{}'.", fingerprint, sourcePath);

        try (BufferedOutputStream out =
                     new BufferedOutputStream(new FileOutputStream(sourcePath.toFile()))) {
            source.transferTo(out);
            out.flush();
        } catch (Exception e) {
            throw new ContentException("Unable to write source for " + fingerprint + " to store.", e);
        }

        logger.info(
                "Document source with fingerprint '{}' written to content store at location '{}'.",
                fingerprint,
                sourcePath);
    }

    @Override
    public void storeThumbnail(@NotNull Fingerprint fingerprint, BufferedImage thumbnail)
            throws ContentException {
        Path thumbnailPath = this.componentPath(fingerprint, THUMBNAIL_COMPONENT);
        logger.debug("Writing thumbnail for {} to store at '{}'.", fingerprint, thumbnailPath);

        try {
            ImageIO.write(thumbnail, "png", thumbnailPath.toFile());
            logger.debug(
                    "Successfully wrote thumbnail for {} to store at '{}'.", fingerprint, thumbnailPath);
        } catch (Exception e) {
            throw new ContentException("Unable to write thumbnail for " + fingerprint + " to store.", e);
        }
    }

    @Override
    public void storeText(@NotNull Fingerprint fingerprint, String text) throws ContentException {
        Path contentPath = this.componentPath(fingerprint, TEXT_COMPONENT);
        logger.debug(
                "Writing compressed text for {} to store at location '{}'.", fingerprint, contentPath);

        try (GZIPOutputStream out =
                     new GZIPOutputStream(
                             new BufferedOutputStream(new FileOutputStream(contentPath.toFile())))) {
            try (InputStream in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
                in.transferTo(out);
                out.flush();
            }
        } catch (Exception e) {
            throw new ContentException(
                    "Unable to write compressed text for " + fingerprint + " to store.", e);
        }
    }

    @Override
    public synchronized boolean delete(@NotNull Fingerprint fingerprint) {
        boolean deleted = false;

        try {
            deleted = this.deleteContent(this.componentPath(fingerprint, SOURCE_COMPONENT));
            if (deleted) {
                deleted = this.deleteContent(this.componentPath(fingerprint, THUMBNAIL_COMPONENT));
            }
            if (deleted) {
                deleted = this.deleteContent(this.componentPath(fingerprint, TEXT_COMPONENT));
            }
        } catch (Exception e) {
            logger.error(
                    "Unable to delete document with fingerprint " + fingerprint + "; returning false.", e);
        }

        return deleted;
    }

    @Override
    public Status status(@NotNull Fingerprint fingerprint) {
        Function<String, ComponentStatus> componentExists =
                (String component) -> {
                    try {
                        var sourcePath = this.componentPath(fingerprint, component);
                        if (sourcePath.toFile().exists()) {
                            return ComponentStatus.Available;
                        } else {
                            return ComponentStatus.NotAvailable;
                        }
                    } catch (Exception e) {
                        return ComponentStatus.NotAvailable;
                    }
                };

        var sourceStatus = componentExists.apply(SOURCE_COMPONENT);
        var thumbnailStatus = componentExists.apply(THUMBNAIL_COMPONENT);
        var textStatus = componentExists.apply(TEXT_COMPONENT);

        return new Status(sourceStatus, thumbnailStatus, textStatus);
    }

    // Private methods
    private Path createComponentDirectory(Path root, String component) throws ContentException {
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

    private URL getContentUrl(@NotNull Fingerprint fingerprint, String component) {
        URL contentUrl = null;

        Path contentPath = this.componentPath(fingerprint, component);
        if (contentPath.toFile().exists()) {
            try {
                contentUrl = contentPath.toUri().toURL();
                logger.debug(
                        "URL for content with document fingerprint '{}' is '{}'.",
                        fingerprint,
                        contentUrl.toExternalForm());
            } catch (MalformedURLException e) {
                throw new ContentException(
                        "Unable to create URL for content with document fingerprint '" + fingerprint + "'.", e);
            }
        } else {
            logger.warn("Content of type '{}' not available for {}.", component, fingerprint);
        }

        return contentUrl;
    }

    private Path componentPath(Fingerprint fingerprint, String component) {
        return switch (component) {
            case SOURCE_COMPONENT -> this.documentRoot.resolve(
                    fingerprint + DOCUMENT_COMPONENT_EXTENSION);
            case THUMBNAIL_COMPONENT -> this.thumbnailRoot.resolve(
                    fingerprint + THUMBNAIL_COMPONENT_EXTENSION);
            case TEXT_COMPONENT -> this.textRoot.resolve(fingerprint + TEXT_COMPONENT_EXTENSION);
            default -> throw new ContentException("Unknown repository component '" + component + ".");
        };
    }
}
