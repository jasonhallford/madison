package io.miscellanea.madison.broker;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class BrokerConfigModule extends ConfigModule<BrokerConfig> {
    public BrokerConfigModule() {
        super("/config/broker.properties");
    }

    @Override
    protected BrokerConfig produceConfiguration(CompositeConfiguration configuration) {
        return new BrokerConfig(configuration.getString("broker.host"),
                configuration.getInt("broker.port"), configuration.getString("broker.user"),
                configuration.getString("broker.password"), configuration.getString("broker.topic"));
    }
}
