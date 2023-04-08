package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;
import java.net.URL;

public interface MetadataExtractor {
    Document extract(String fingerprint, URL documentURL) throws ContentException;
}
