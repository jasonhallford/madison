package io.miscellanea.madison.document;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public interface Fingerprint {
    String fromBytes(byte[] bytes) throws DocumentException;
    String fromFile(File file) throws DocumentException;
    String fromUrl(@NotNull URL url) throws DocumentException;
}
