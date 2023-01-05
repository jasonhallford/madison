package io.miscellanea.madison.dbmanager;

public record Configuration(String dbHost,int dbPort,String dbUser, String dbPassword, String dbName) {
}
