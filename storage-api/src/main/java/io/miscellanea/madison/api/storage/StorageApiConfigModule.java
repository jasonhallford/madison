package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class StorageApiConfigModule extends ConfigModule<StorageApiConfig> {
    public StorageApiConfigModule() {
        super("/config/storage-api.properties");
    }

    @Override
    protected StorageApiConfig produceConfiguration(CompositeConfiguration configuration) {
        return new StorageApiConfig(configuration.getInt("storage.api.port"),
                configuration.getString("storage.api.content.dir"),
                configuration.getString("storage.api.upload.dir"));
    }
}
