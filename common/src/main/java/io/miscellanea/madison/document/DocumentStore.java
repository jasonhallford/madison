package io.miscellanea.madison.document;

import io.miscellanea.madison.content.ContentException;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public interface DocumentStore {
    void storeSource(@NotNull Fingerprint fingerprint, InputStream source) throws ContentException;

    void storeThumbnail(@NotNull Fingerprint fingerprint, BufferedImage thumbnail) throws ContentException;

    void storeText(@NotNull Fingerprint fingerprint, String text) throws ContentException;

    boolean delete(@NotNull Fingerprint fingerprint);

    Status status(@NotNull Fingerprint fingerprint);

    URL sourceURL(@NotNull Fingerprint fingerprint) throws ContentException;

    URL thumbnailURL(@NotNull Fingerprint fingerprint) throws ContentException;

    URL textURL(@NotNull Fingerprint fingerprint) throws ContentException;
}
