package io.miscellanea.madison.importsvc;

import io.miscellanea.madison.config.ConfigModule;
import org.apache.commons.configuration2.CompositeConfiguration;

public class ImportServerConfigModule extends ConfigModule<ImportServerConfig> {
    public ImportServerConfigModule() {
        super("/config/import-server.properties");
    }

    @Override
    protected ImportServerConfig produceConfiguration(CompositeConfiguration configuration) {
        return new ImportServerConfig(configuration.getString("import.dir"),
                configuration.getString("import.content.dir"),
                configuration.getInt("import.task-pool-size"), configuration.getString("import.tika-url"),
                configuration.getBoolean("import.delete-after-store"));
    }
}
