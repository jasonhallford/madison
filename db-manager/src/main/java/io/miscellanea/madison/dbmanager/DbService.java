package io.miscellanea.madison.dbmanager;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.flywaydb.core.Flyway;

public class DbService {
    public static void main(String[] args) {
        // Create the Guice injector to initialize our dependencies.
        Injector injector = Guice.createInjector(new ConfigurationModule());
        Configuration config = injector.getInstance(Configuration.class);

        // Create the Flyway instance to manage DB migration
        var pgUrl = String.format("jdbc:postgresql://%s:%d/%s", config.dbHost(), config.dbPort(), config.dbName());
        Flyway flyway = Flyway.configure().dataSource(pgUrl,config.dbUser(),config.dbPassword()).load();

        flyway.migrate();
    }
}
