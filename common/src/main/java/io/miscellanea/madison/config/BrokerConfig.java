package io.miscellanea.madison.config;

public record BrokerConfig(String host, int port, String user, String password, int qos) {
}
