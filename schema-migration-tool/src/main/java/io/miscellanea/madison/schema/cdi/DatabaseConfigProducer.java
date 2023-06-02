package io.miscellanea.madison.schema.cdi;

import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import io.miscellanea.madison.schema.DatabaseConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

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
        return new DatabaseConfig(configuration.getString("db.dir"), configuration.getString("db.name"),
                configuration.getString("db.user"), configuration.getString("db.password"));
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
