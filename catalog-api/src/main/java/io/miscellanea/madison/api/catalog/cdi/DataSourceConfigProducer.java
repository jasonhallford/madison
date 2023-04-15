package io.miscellanea.madison.api.catalog.cdi;

import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import io.miscellanea.madison.dal.config.DataSourceConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

@ApplicationScoped
public class DataSourceConfigProducer extends ConfigProducer<DataSourceConfig> {
    // Fields
    private DataSourceConfig config;

    // Constructors
    public DataSourceConfigProducer() {
        super("/config/data-access.properties");
    }

    // ConfigModule
    @Override
    protected DataSourceConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new DataSourceConfig(configuration.getString("ds.className"),
                configuration.getString("ds.poolName"), configuration.getInt("ds.maxPoolSize"),
                configuration.getBoolean("ds.autoCommit"));
    }

    // Producer methods
    @PostConstruct
    public void initializeConfig() throws ConfigException {
        try {
            this.config = this.buildConfig();
        } catch (Exception e) {
            throw new ConfigException("Unable to initialize configuration.", e);
        }
    }

    @Produces
    public DataSourceConfig produce() {
        return this.config;
    }
}
