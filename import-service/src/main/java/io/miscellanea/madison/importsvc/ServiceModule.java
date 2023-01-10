package io.miscellanea.madison.importsvc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.miscellanea.madison.content.*;
import io.miscellanea.madison.service.EventService;
import io.miscellanea.madison.importsvc.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ServiceModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ServiceModule.class);

    @Override
    protected void configure() {
        bind(EventService.class).to(RabbitMQEventService.class);
        bind(FingerprintGenerator.class).to(SHA256FingerprintGenerator.class);
        bind(DocumentStore.class).to(FileSystemDocumentStore.class);
    }

    // Producer methods
    @Provides
    @Singleton
    @Inject
    public Connection provideConnection(ServiceConfig config) throws IOException, TimeoutException {
        logger.debug("Configuring RabbtMQ connection factory.");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.brokerConfig().host());
        factory.setPort(config.brokerConfig().port());
        factory.setUsername(config.brokerConfig().user());
        factory.setPassword(config.brokerConfig().password());
        factory.setAutomaticRecoveryEnabled(true);

        logger.debug("RabbitMQ connection factory configured; creating connection.");
        Connection conn = factory.newConnection();
        logger.debug("Successfully created RabbitMQ connection.");

        return conn;
    }

    @Inject
    @ContentRoot
    @Provides
    public String provideContentRoot(ServiceConfig config) {
        return config.contentDir();
    }
}
