package io.miscellanea.madison.catalog;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class CatalogApiConfigModule extends ConfigModule<CatalogApiConfig> {
    public CatalogApiConfigModule() {
        super("/config/catalog-api.properties");
    }

    @Override
    protected CatalogApiConfig produceConfiguration(CompositeConfiguration configuration) {
        return new CatalogApiConfig(configuration.getInt("catalog.api.port"),
                configuration.getString("catalog.api.upload.dir"));
    }
}
