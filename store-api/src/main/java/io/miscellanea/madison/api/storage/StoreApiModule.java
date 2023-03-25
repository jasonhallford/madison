package io.miscellanea.madison.api.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.content.*;

import javax.inject.Inject;

public class StoreApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
        bind(DocumentStore.class).to(FileSystemDocumentStore.class).asEagerSingleton();
    }

    // Producer methods
    @Inject
    @StorageRoot
    @Provides
    public String provideContentRoot(StoreApiConfig config) {
        return config.contentDirectory();
    }
}
