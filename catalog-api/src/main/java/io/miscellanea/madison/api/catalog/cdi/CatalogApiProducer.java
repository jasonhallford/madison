package io.miscellanea.madison.api.catalog.cdi;

import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.redis.RedisQueue;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.dal.repository.JooqDocumentRepository;
import io.miscellanea.madison.repository.DocumentRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

@ApplicationScoped
public class CatalogApiProducer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(CatalogApiProducer.class);
    public static final String IMPORT_QUEUE_NAME = "madison.import";

    private final BrokerConfig brokerConfig;
    private EventService eventService;
    private DocumentRepository documentRepository;
    private Queue<ImportMessage> importMessageQueue;
    private Connection connection;

    // Constructors
    @Inject
    public CatalogApiProducer(Connection connection, BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.connection = connection;
    }

    @PostConstruct
    public void initialize() throws ConfigException {
        try {
            logger.debug("Initializing Redis EventService implementation.");
            this.eventService = new RedisEventService(this.brokerConfig);

            logger.debug("Initializing JOOQ DocumentRepository implementation.");
            this.documentRepository = new JooqDocumentRepository(connection);
            this.connection = null; // We don't need to hold this reference any longer.

            logger.debug("Initializing Redis import message queue.");
            this.importMessageQueue = new RedisQueue<>(this.brokerConfig, IMPORT_QUEUE_NAME);
        } catch (Exception e) {
            throw new ConfigException("Unable to initialize Import Agent configuration module.", e);
        }
    }

    // Producers
    @Produces
    public EventService produceEventService() {
        return this.eventService;
    }

    @Produces
    public Queue<ImportMessage> produceImportMessageQueue() {
        return this.importMessageQueue;
    }

    @Produces
    public DocumentRepository produceDocumentRepository() {
        return this.documentRepository;
    }
}
