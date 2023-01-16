package io.miscellanea.madison.content;

import io.miscellanea.madison.entity.Document;

import java.io.InputStream;

public interface MetadataExtractor {
    Document fromStream(String fingerprint, InputStream documentStream) throws ContentException;
}
