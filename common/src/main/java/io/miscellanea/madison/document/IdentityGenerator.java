package io.miscellanea.madison.document;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public interface IdentityGenerator {
    String fromUrl(@NotNull URL url) throws DocumentException;
    String fromBytes(byte[] bytes) throws DocumentException;
}
