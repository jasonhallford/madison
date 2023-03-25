package io.miscellanea.madison.api.catalog;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.miscellanea.madison.broker.BrokerConfig;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisEventService;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.redis.RedisQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

public class CatalogApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventService.class).to(RedisEventService.class);
    }

    @Inject
    @Provides
    @Singleton
    public Queue<ImportMessage> provideImportQueue(BrokerConfig brokerConfig) {
        return new RedisQueue<>(brokerConfig, "madison.import");
    }
}
