package io.miscellanea.madison.api.storage;

public record StorageApiConfig(int port, String contentDirectory, String uploadDirectory) {
}
