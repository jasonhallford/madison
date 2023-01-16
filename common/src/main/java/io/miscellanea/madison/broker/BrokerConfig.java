package io.miscellanea.madison.broker;

public record BrokerConfig(String host, int port, String user, String password, String topic) {
}
