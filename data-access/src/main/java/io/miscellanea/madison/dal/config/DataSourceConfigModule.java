package io.miscellanea.madison.dal.config;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class DataSourceConfigModule extends ConfigModule<DataSourceConfig> {
    public DataSourceConfigModule() {
        super("/config/data-source.properties");
    }

    @Override
    protected DataSourceConfig produceConfiguration(CompositeConfiguration config) {
        return new DataSourceConfig(config.getString("ds.className"), config.getString("ds.poolName"),
                config.getInt("ds.maxPoolSize"), config.getBoolean("ds.autoCommit"));
    }
}
