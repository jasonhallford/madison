package io.miscellanea.madison.document;

import io.miscellanea.madison.content.ContentException;
import java.io.File;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public interface FingerprintGenerator {
    String fromBytes(byte[] bytes) throws ContentException;
    String fromFile(File file) throws ContentException;
    String fromUrl(@NotNull URL url) throws ContentException;
}
