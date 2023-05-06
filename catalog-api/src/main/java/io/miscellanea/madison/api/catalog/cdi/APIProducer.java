package io.miscellanea.madison.api.catalog.cdi;

import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.redis.RedisQueue;
import io.miscellanea.madison.config.ConfigException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class APIProducer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(APIProducer.class);
    public static final String IMPORT_QUEUE_NAME = "madison.import";

    private final BrokerConfig brokerConfig;
    private EventService eventService;
    private Queue<ImportMessage> importMessageQueue;

    // Constructors
    @Inject
    public APIProducer(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    @PostConstruct
    public void initialize() throws ConfigException {
        try {
            logger.debug("Initializing Redis EventService implementation.");
            this.eventService = new RedisEventService(this.brokerConfig);

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
}
