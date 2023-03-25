package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class StoreApiConfigModule extends ConfigModule<StoreApiConfig> {
    public StoreApiConfigModule() {
        super("/config/catalog-api.properties");
    }

    @Override
    protected StoreApiConfig produceConfiguration(CompositeConfiguration configuration) {
        return new StoreApiConfig(configuration.getInt("store.api.port"),
                configuration.getString("store.api.content.dir"),
                configuration.getString("store.api.upload.dir"));
    }
}
