package io.miscellanea.madison.schema;

import org.flywaydb.core.Flyway;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaMigrationTool {
    private static final Logger logger = LoggerFactory.getLogger(SchemaMigrationTool.class);

    public static void main(String[] args) {
        logger.debug("Initializing Weld CDI container...");
        Weld weld = new Weld();
        try (WeldContainer cdi = weld.initialize()) {
            logger.debug("Weld successfully initialized.");

            // Create the Flyway instance to manage DB migration
            DatabaseConfig config = cdi.select(DatabaseConfig.class).get();
            var jdbcUrl = String.format("jdbc:h2://%s/%s", config.dbDir(), config.dbName());
            logger.debug("Flyway will connect to database using URL {}.", jdbcUrl);

            Flyway flyway = Flyway.configure().dataSource(jdbcUrl, config.dbUser(), config.dbPassword()).load();

            logger.info("Executing flyway schema migration.");
            var migrateResult = flyway.migrate();
            if (migrateResult.success) {
                logger.info("Flyway schema migration executed.");
            } else {
                logger.warn("Flyway schema migration failed: {}", migrateResult.warnings);
            }
        }
    }
}
