package io.miscellanea.madison.import_agent;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.redis.RedisQueue;
import io.miscellanea.madison.content.*;
import io.miscellanea.madison.dal.repository.JooqDocumentRepository;
import io.miscellanea.madison.document.FingerprintGenerator;
import io.miscellanea.madison.document.SHA256FingerprintGenerator;
import io.miscellanea.madison.repository.DocumentRepository;
import jakarta.ws.rs.client.ClientBuilder;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportAgentModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ImportAgentModule.class);

    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
        bind(FingerprintGenerator.class).to(SHA256FingerprintGenerator.class);
        bind(MetadataExtractor.class).to(TikaMetadataExtractor.class);
        bind(DocumentRepository.class).to(JooqDocumentRepository.class);
        bind(ThumbnailGenerator.class).to(PdfBoxThumbnailGenerator.class);
        bind(ContentExtractor.class).to(TikaContentExtractor.class);
    }

    // Producer methods
    @Provides
    @Singleton
    public ResteasyClient provideResteasyClient() {
        System.setProperty("dev.resteasy.entity.file.threshold", "-1");
        logger.debug("Set Resteasy property to uncap file upload size.");

        return (ResteasyClient) ClientBuilder.newClient();
    }

    @Inject
    @Provides
    @Singleton
    public Queue<ImportMessage> provideImportQueue(BrokerConfig brokerConfig) {
        return new RedisQueue<>(brokerConfig, "madison.import");
    }
}
