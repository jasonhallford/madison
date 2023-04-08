package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class ImportAgentConfigModule extends ConfigModule<ImportAgentConfig> {
    public ImportAgentConfigModule() {
        super("/config/import-agent.properties");
    }

    @Override
    protected ImportAgentConfig produceConfiguration(CompositeConfiguration configuration) {
        return new ImportAgentConfig(configuration.getString("import.dir"),
                configuration.getString("import.content.dir"),
                configuration.getInt("import.task-pool-size"), configuration.getString("import.tika-url"),
                configuration.getString("import.storage-url"),
                configuration.getBoolean("import.delete-after-store"));
    }
}
