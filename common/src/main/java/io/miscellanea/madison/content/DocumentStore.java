package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.net.URL;

public interface DocumentStore {
    URL content(@NotNull Document document) throws ContentException;

    URL thumbnail(@NotNull Document document) throws ContentException;

    void store(@NotNull Document document, BufferedImage thumbnail, URL content) throws ContentException;

    boolean delete(@NotNull Document document);

    boolean exists(@NotNull Document document);
}
