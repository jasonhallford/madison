package io.miscellanea.madison.dbmanager;

public record ServiceConfig(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName) {
}
