package io.miscellanea.madison.importsvc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.miscellanea.madison.content.*;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.dal.repository.JooqDocumentRepository;
import io.miscellanea.madison.repository.DocumentRepository;
import jakarta.ws.rs.client.ClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

public class ImportServerModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ImportServerModule.class);

    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
        bind(FingerprintGenerator.class).to(SHA256FingerprintGenerator.class);
        bind(DocumentStore.class).to(FileSystemDocumentStore.class);
        bind(MetadataExtractor.class).to(TikaMetadataExtractor.class);
        bind(DocumentRepository.class).to(JooqDocumentRepository.class);
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
    @ContentRoot
    @Provides
    public String provideContentRoot(ImportServerConfig config) {
        return config.contentDir();
    }
}
