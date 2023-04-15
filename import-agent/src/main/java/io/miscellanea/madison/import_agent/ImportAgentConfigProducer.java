package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.config.ConfigException;
import io.miscellanea.madison.config.ConfigProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.configuration2.CompositeConfiguration;

@ApplicationScoped
public class ImportAgentConfigProducer extends ConfigProducer<ImportAgentConfig> {
    private ImportAgentConfig config;

    public ImportAgentConfigProducer() {
        super("/config/import-agent.properties");
    }

    // ConfigProducer
    @Override
    protected ImportAgentConfig buildCustomConfig(CompositeConfiguration configuration) {
        return new ImportAgentConfig(
                configuration.getString("import.dir"),
                configuration.getString("import.content.dir"),
                configuration.getInt("import.task-pool-size"),
                configuration.getString("import.tika-url"),
                configuration.getString("import.storage-url"),
                configuration.getString("import.catalog-url"),
                configuration.getBoolean("import.delete-after-store"));
    }

    // Producer methods
    @PostConstruct
    public void initializeConfig() throws ConfigException {
        try {
            this.config = this.buildConfig();
        } catch (Exception e) {
            throw new ConfigException("Unable to initialize configuration.", e);
        }
    }

    @Produces
    public ImportAgentConfig produce() {
        return this.config;
    }
}
