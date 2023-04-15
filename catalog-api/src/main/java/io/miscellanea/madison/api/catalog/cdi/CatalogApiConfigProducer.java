package io.miscellanea.madison.api.catalog.cdi;

import io.miscellanea.madison.api.catalog.CatalogApiConfig;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

@ApplicationScoped
public class CatalogApiConfigProducer extends ConfigProducer<CatalogApiConfig> {
    private CatalogApiConfig config;

    public CatalogApiConfigProducer() {
        super("/config/catalog-api.properties");
    }

    // ConfigProducer
    @Override
    protected CatalogApiConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new CatalogApiConfig(configuration.getInt("catalog.api.port"),
                configuration.getString("catalog.api.upload.dir"));
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
    public CatalogApiConfig produce() {
        return this.config;
    }
}
