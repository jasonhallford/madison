package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.net.URL;

public interface DocumentStore {
    void store(@NotNull Document document, BufferedImage thumbnail, String content, URL source) throws ContentException;

    boolean delete(@NotNull Document document);

    boolean exists(@NotNull Document document);

    URL sourceURL(@NotNull Document document) throws ContentException;

    URL thumbnailURL(@NotNull Document document) throws ContentException;

    URL textURL(@NotNull Document document) throws ContentException;
}
