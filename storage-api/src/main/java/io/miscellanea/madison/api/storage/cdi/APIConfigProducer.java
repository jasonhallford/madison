package io.miscellanea.madison.api.storage.cdi;

import io.miscellanea.madison.api.storage.APIConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class APIConfigProducer {
    private APIConfig config;


    @PostConstruct
    public void init() {
        this.config = new APIConfig(
                ConfigProvider.getConfig().getValue("storage.api.content.dir", String.class),
                ConfigProvider.getConfig().getValue("storage.api.upload.dir", String.class));
    }

    @Produces
    public APIConfig produce() {
        return this.config;
    }
}
