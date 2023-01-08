package io.miscellanea.madison.importsvc.config;

import io.miscellanea.madison.config.BrokerConfig;
import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class ServiceConfigModule extends ConfigModule<ServiceConfig> {
    public ServiceConfigModule() {
        super("/config/import-service.properties");
    }

    @Override
    protected ServiceConfig produceConfiguration(CompositeConfiguration configuration) {
        var brokerConfig = new BrokerConfig(configuration.getString("broker.host"),
                configuration.getInt("broker.port"), configuration.getString("broker.user"),
                configuration.getString("broker.password"), configuration.getInt("broker.qos"));

        return new ServiceConfig(configuration.getString("import.service.dir"),
                configuration.getInt("import.service.task-pool-size"),brokerConfig);
    }
}
