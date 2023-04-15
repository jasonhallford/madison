package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

@ApplicationScoped
public class StorageApiConfigProducer extends ConfigProducer<StorageApiConfig> {
    private StorageApiConfig config;

    public StorageApiConfigProducer() {
        super("/config/import-agent.properties");
    }

    // ConfigProducer
    @Override
    protected StorageApiConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new StorageApiConfig(configuration.getInt("storage.api.port"),
                configuration.getString("storage.api.content.dir"),
                configuration.getString("storage.api.upload.dir"));
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
    public StorageApiConfig produce() {
        return this.config;
    }
}
