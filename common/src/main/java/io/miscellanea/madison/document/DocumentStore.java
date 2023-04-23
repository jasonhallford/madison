package io.miscellanea.madison.document;

import io.miscellanea.madison.content.ContentException;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public interface DocumentStore {
    void storeSource(@NotNull Fingerprint fingerprint, InputStream source) throws ContentException;

    void storeThumbnail(@NotNull Fingerprint fingerprint, BufferedImage thumbnail) throws ContentException;

    void storeText(@NotNull Fingerprint fingerprint, String text) throws ContentException;

    boolean delete(@NotNull Fingerprint fingerprint);

    StoreStatus status(@NotNull Fingerprint fingerprint);

    URL sourceURL(@NotNull Fingerprint fingerprint) throws ContentException;

    URL thumbnailURL(@NotNull Fingerprint fingerprint) throws ContentException;

    URL textURL(@NotNull Fingerprint fingerprint) throws ContentException;
}
