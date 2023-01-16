package io.miscellanea.madison.importsvc;

public record ImportServerConfig(String importDir, String contentDir, int taskPoolSize, String tikaUrl) {
}
