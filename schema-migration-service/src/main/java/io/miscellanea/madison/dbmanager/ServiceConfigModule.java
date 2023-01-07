package io.miscellanea.madison.dbmanager;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class ServiceConfigModule extends ConfigModule<ServiceConfig> {
    public ServiceConfigModule() {
        super("/config/schema-migration-service.properties");
    }

    @Override
    protected ServiceConfig produceConfiguration(CompositeConfiguration compositeConfiguration) {
        return new ServiceConfig(compositeConfiguration.getString("db.host"), compositeConfiguration.getInt("db.port"),
                compositeConfiguration.getString("db.user"), compositeConfiguration.getString("db.password"),
                compositeConfiguration.getString("db.name"));
    }
}
