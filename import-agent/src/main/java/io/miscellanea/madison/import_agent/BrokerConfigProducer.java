package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

@ApplicationScoped
public class BrokerConfigProducer extends ConfigProducer<BrokerConfig> {
    private BrokerConfig config;

    public BrokerConfigProducer() {
        super("/config/broker.properties");
    }

    // ConfigProducer
    @Override
    protected BrokerConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new BrokerConfig(
                configuration.getString("broker.host"),
                configuration.getInt("broker.port"),
                configuration.getString("broker.user"),
                configuration.getString("broker.password"),
                configuration.getString("broker.topic"));
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
    public BrokerConfig produce() {
        return this.config;
    }
}
