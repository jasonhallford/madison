package io.miscellanea.madison.api.catalog.cdi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.dal.config.DataSourceConfig;
import io.miscellanea.madison.dal.config.DatabaseConfig;
import io.miscellanea.madison.repository.RepositoryException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

@ApplicationScoped
public class DataSourceProducer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(DataSourceProducer.class);
    public static final String IMPORT_QUEUE_NAME = "madison.import";

    private final DatabaseConfig databaseConfig;
    private final DataSourceConfig dataSourceConfig;
    private DataSource dataSource;

    // Constructors
    @Inject
    public DataSourceProducer(DatabaseConfig dbConfig, DataSourceConfig dsConfig) {
        this.databaseConfig = dbConfig;
        this.dataSourceConfig = dsConfig;
    }

    @PostConstruct
    public void initialize() throws ConfigException {
        try {
            logger.debug("Producing new pooled data source.");

            var hikariConfig = new HikariConfig();
            hikariConfig.setDataSourceClassName(dataSourceConfig.className());
            hikariConfig.setUsername(databaseConfig.dbUser());
            hikariConfig.setPassword(databaseConfig.dbPassword());
            hikariConfig.setMaximumPoolSize(dataSourceConfig.maxPoolSize());
            hikariConfig.setPoolName(dataSourceConfig.poolName());
            hikariConfig.setAutoCommit(dataSourceConfig.autoCommit());
            hikariConfig.addDataSourceProperty("serverName", databaseConfig.dbHost());
            hikariConfig.addDataSourceProperty("portNumber", databaseConfig.dbPort());
            hikariConfig.addDataSourceProperty("databaseName", databaseConfig.dbName());

            this.dataSource = new HikariDataSource(hikariConfig);
            logger.debug("Successfully created new Hikari datasource.");
        } catch (Exception e) {
            throw new ConfigException("Unable to initialize Import Agent configuration module.", e);
        }
    }

    // Producers
    @Produces
    public DataSource produceDataSource() {
        return this.dataSource;
    }

    @Produces
    public Connection produceConnection() {
        long ts = System.currentTimeMillis();

        try {
            logger.debug("Producing new pool connection.");
            Connection connection = dataSource.getConnection();
            logger.debug("Produced new connection in {} ms.", System.currentTimeMillis() - ts);

            return connection;
        } catch (Exception e) {
            throw new RepositoryException("Unable to connect to repository database.", e);
        }
    }
}
