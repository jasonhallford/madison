package io.miscellanea.madison.schema.cdi;

import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;
import io.miscellanea.madison.schema.DatabaseConfig;

@ApplicationScoped
public class DatabaseConfigProducer extends ConfigProducer<DatabaseConfig> {
    // Fields
    private DatabaseConfig config;

    public DatabaseConfigProducer() {
        super("/config/database.properties");
    }

    // ConfigProducer
    @Override
    protected DatabaseConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new DatabaseConfig(configuration.getString("db.host"), configuration.getInt("db.port"),
                configuration.getString("db.user"), configuration.getString("db.password"),
                configuration.getString("db.name"));
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
    public DatabaseConfig produce() {
        return this.config;
    }

}
