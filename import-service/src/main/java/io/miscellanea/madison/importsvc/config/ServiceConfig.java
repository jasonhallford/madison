package io.miscellanea.madison.importsvc.config;

import io.miscellanea.madison.config.BrokerConfig;

public record ServiceConfig(String importDir, int taskPoolSize, BrokerConfig brokerConfig) {
}
