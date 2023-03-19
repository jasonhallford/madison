package io.miscellanea.madison.content;

import java.awt.image.BufferedImage;
import java.net.URL;

public interface ThumbnailGenerator {
    BufferedImage generate(String fingerprint, URL documentUrl);
}
