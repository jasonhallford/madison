package io.miscellanea.madison.dbmanager;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaMigrationService {
    private static final Logger logger = LoggerFactory.getLogger(SchemaMigrationService.class);

    public static void main(String[] args) {
        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Guice injector.");
        Injector injector = Guice.createInjector(new ServiceConfigModule());
        ServiceConfig config = injector.getInstance(ServiceConfig.class);
        logger.debug("Guice injector successfully initialized.");

        // Create the Flyway instance to manage DB migration
        var pgUrl = String.format("jdbc:postgresql://%s:%d/%s", config.dbHost(), config.dbPort(), config.dbName());
        logger.debug("Flyway will connect to database using URL {}.", pgUrl);

        Flyway flyway = Flyway.configure().dataSource(pgUrl, config.dbUser(), config.dbPassword()).load();

        logger.info("Executing flyway schema migration.");
        var migrateResult = flyway.migrate();
        if (migrateResult.success) {
            logger.info("Flyway schema migration executed.");
        } else {
            logger.warn("Flyway schema migration failed: {}", migrateResult.warnings);
        }

    }
}
