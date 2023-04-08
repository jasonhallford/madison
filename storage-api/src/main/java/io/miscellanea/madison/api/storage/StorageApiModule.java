package io.miscellanea.madison.api.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.FileSystemDocumentStore;
import io.miscellanea.madison.document.StorageRoot;
import javax.inject.Inject;

public class StorageApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
        bind(DocumentStore.class).to(FileSystemDocumentStore.class).asEagerSingleton();
    }

    // Producer methods
    @Inject
    @StorageRoot
    @Provides
    public String provideContentRoot(StorageApiConfig config) {
        return config.contentDirectory();
    }
}
