package io.miscellanea.madison.dal;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.throwingproviders.CheckedProvides;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.miscellanea.madison.dal.config.DataSourceConfig;
import io.miscellanea.madison.dal.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceModule extends AbstractModule {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(DataSourceModule.class);

    // Producers
    @Inject
    @Singleton
    @Provides
    public DataSource produceDataSource(DatabaseConfig dbConfig, DataSourceConfig dsConfig) {
        logger.debug("Producing new pooled data source.");

        var hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName(dsConfig.className());
        hikariConfig.setUsername(dbConfig.dbUser());
        hikariConfig.setPassword(dbConfig.dbPassword());
        hikariConfig.setMaximumPoolSize(dsConfig.maxPoolSize());
        hikariConfig.setPoolName(dsConfig.poolName());
        hikariConfig.setAutoCommit(dsConfig.autoCommit());
        hikariConfig.addDataSourceProperty("serverName", dbConfig.dbHost());
        hikariConfig.addDataSourceProperty("portNumber", dbConfig.dbPort());
        hikariConfig.addDataSourceProperty("databaseName", dbConfig.dbName());

        var ds = new HikariDataSource(hikariConfig);
        logger.debug("Successfully created new Hikari datasource.");

        return ds;
    }

    @Inject
    @CheckedProvides(ConnectionProvider.class)
    @Provides
    public Connection produceConnection(DataSource dataSource) throws SQLException {
        long ts = System.currentTimeMillis();

        logger.debug("Producing new pool connection.");
        Connection connection = dataSource.getConnection();
        logger.debug("Produced new connection in {} ms.", System.currentTimeMillis() - ts);

        return connection;
    }
}
