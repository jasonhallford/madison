package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public interface DocumentStore {
    public URL find(@NotNull Document document) throws ContentException;

    public void store(@NotNull Document document, URL content) throws ContentException;

    public boolean delete(@NotNull Document document);

    public boolean exists(@NotNull Document document);
}
