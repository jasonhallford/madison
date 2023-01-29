package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.net.URL;

public interface DocumentStore {
    void store(@NotNull Document document, BufferedImage thumbnail, URL source) throws ContentException;

    boolean delete(@NotNull Document document);

    boolean exists(@NotNull Document document);

    URL source(@NotNull Document document) throws ContentException;

    URL thumbnail(@NotNull Document document) throws ContentException;

}
