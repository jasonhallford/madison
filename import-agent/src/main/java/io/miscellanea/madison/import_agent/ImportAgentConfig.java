package io.miscellanea.madison.import_agent;

public record ImportAgentConfig(
        String importDir,
        String contentDir,
        int taskPoolSize,
        String tikaUrl,
        String storageUrl,
        String catalogUrl,
        boolean deleteAfterImport) {
}
