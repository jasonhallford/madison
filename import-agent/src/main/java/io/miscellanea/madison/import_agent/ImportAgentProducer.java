package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.redis.RedisQueue;
import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.document.FingerprintGenerator;
import io.miscellanea.madison.document.SHA256FingerprintGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ImportAgentProducer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportAgentProducer.class);
    public static final String IMPORT_QUEUE_NAME = "madison.import";

    private final BrokerConfig brokerConfig;
    private EventService eventService;
    private FingerprintGenerator fingerprintGenerator;
    private ResteasyClient resteasyClient;
    private Queue<ImportMessage> importMessageQueue;

    // Constructors
    @Inject
    public ImportAgentProducer(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    @PostConstruct
    public void initialize() throws ConfigException {
        try {
            logger.debug("Initializing the SHA256 fingerprint generator.");
            this.fingerprintGenerator = new SHA256FingerprintGenerator();

            logger.debug("Initializing Redis EventService implementation.");
            this.eventService = new RedisEventService(this.brokerConfig);

            logger.debug("Initializing Redis import message queue.");
            this.importMessageQueue = new RedisQueue<>(this.brokerConfig, IMPORT_QUEUE_NAME);

            logger.debug("Initializing Resteasy client to support unlimited upload sizes.");
            System.setProperty("dev.resteasy.entity.file.threshold", "-1");
            this.resteasyClient = (ResteasyClient) ClientBuilder.newClient();
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
    public FingerprintGenerator produceFingerprintGenerator() {
        return this.fingerprintGenerator;
    }

    @Produces
    public ResteasyClient produceResteasyClient() {
        return this.resteasyClient;
    }

    @Produces
    public Queue<ImportMessage> produceImportMessageQueue() {
        return this.importMessageQueue;
    }
}
