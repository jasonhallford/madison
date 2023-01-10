package io.miscellanea.madison.importsvc.config;

import io.miscellanea.madison.config.BrokerConfig;

public record ServiceConfig(String importDir, String contentDir, int taskPoolSize, BrokerConfig brokerConfig) {
}
