package io.miscellanea.madison.dal.config;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class DatabaseConfigModule extends ConfigModule<DatabaseConfig> {
    public DatabaseConfigModule() {
        super("/config/database.properties");
    }

    @Override
    protected DatabaseConfig produceConfiguration(CompositeConfiguration compositeConfiguration) {
        return new DatabaseConfig(compositeConfiguration.getString("db.host"), compositeConfiguration.getInt("db.port"),
                compositeConfiguration.getString("db.user"), compositeConfiguration.getString("db.password"),
                compositeConfiguration.getString("db.name"));
    }
}
