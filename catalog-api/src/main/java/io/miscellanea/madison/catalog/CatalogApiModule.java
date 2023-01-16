package io.miscellanea.madison.catalog;

import com.google.inject.AbstractModule;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.EventService;

public class CatalogApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
    }
}
