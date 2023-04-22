package io.miscellanea.madison.api.storage.cdi;

import io.miscellanea.madison.api.storage.VerticleConfig;
import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.FileSystemDocumentStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StorageApiProducer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(StorageApiProducer.class);

    private EventService eventService;
    private DocumentStore documentStore;
    private final VerticleConfig verticleConfig;
    private final BrokerConfig brokerConfig;

    // Constructors
    @Inject
    public StorageApiProducer(VerticleConfig verticleConfig, BrokerConfig brokerConfig) {
        this.verticleConfig = verticleConfig;
        this.brokerConfig = brokerConfig;
    }

    @PostConstruct
    public void initialize() throws ConfigException {
        try {
            logger.debug("Initializing Redis EventService implementation.");
            this.eventService = new RedisEventService(this.brokerConfig);

            logger.debug("Initializing file system document store implementation.");
            this.documentStore = new FileSystemDocumentStore(this.verticleConfig.contentDirectory());
        } catch (Exception e) {
            throw new ConfigException("Unable to initialize Import Agent configuration module.", e);
        }
    }

    // Producers
    @Produces
    @ApplicationScoped
    public EventService produceEventService() {
        return this.eventService;
    }

    @Produces
    @ApplicationScoped
    public DocumentStore produceDocumentStore() {
        return this.documentStore;
    }
}
