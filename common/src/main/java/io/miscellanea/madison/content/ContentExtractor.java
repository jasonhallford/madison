package io.miscellanea.madison.content;

import java.net.URL;

public interface ContentExtractor {
    String extract(String fingerprint, URL documentURL) throws ContentException;
}
