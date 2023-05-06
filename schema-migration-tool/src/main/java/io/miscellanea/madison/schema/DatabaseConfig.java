package io.miscellanea.madison.schema;

public record DatabaseConfig(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName) {
}
