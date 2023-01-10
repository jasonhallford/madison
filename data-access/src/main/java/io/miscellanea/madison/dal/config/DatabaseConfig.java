package io.miscellanea.madison.dal.config;

public record DatabaseConfig(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName) {
}
