package io.miscellanea.madison.dal.config;

public record DataSourceConfig(String className, String poolName, int maxPoolSize, boolean autoCommit) {
}
