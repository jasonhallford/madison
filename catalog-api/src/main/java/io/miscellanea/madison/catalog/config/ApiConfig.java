package io.miscellanea.madison.catalog.config;

import io.miscellanea.madison.config.BrokerConfig;

public record ApiConfig(RestConfig restConfig, BrokerConfig brokerConfig) {
}
