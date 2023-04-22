package io.miscellanea.madison.api.catalog.cdi;

import io.miscellanea.madison.api.catalog.VerticleConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class VerticleConfigProducer {
    private VerticleConfig config;

    @PostConstruct
    public void init() {
        this.config = new VerticleConfig(
                ConfigProvider.getConfig().getValue("catalog.api.upload.dir", String.class));
    }

    // Producer methods
    @Produces
    public VerticleConfig produce() {
        return this.config;
    }
}
