package io.miscellanea.madison.catalog.config;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class ApiConfigModule extends ConfigModule<ApiConfig> {
    public ApiConfigModule() {
        super("/config/catalog-api.properties");
    }

    @Override
    protected ApiConfig produceConfiguration(CompositeConfiguration configuration) {
        var restConfig = new RestConfig(configuration.getInt("api.port"), configuration.getString("api.upload.dir"));
        var brokerConfig = new BrokerConfig(configuration.getString("broker.host"), configuration.getInt("broker.port"),
                configuration.getString("broker.user"), configuration.getString("broker.password"));

        return new ApiConfig(restConfig, brokerConfig);
    }
}
