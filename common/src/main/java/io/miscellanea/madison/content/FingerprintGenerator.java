package io.miscellanea.madison.content;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public interface FingerprintGenerator {
    String fromBytes(byte[] bytes) throws ContentException;
    String fromFile(File file) throws ContentException;
    String fromUrl(@NotNull URL url) throws ContentException;
}
