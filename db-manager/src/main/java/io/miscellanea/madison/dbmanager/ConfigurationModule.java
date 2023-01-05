package io.miscellanea.madison.dbmanager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigurationModule extends AbstractModule {
    @Provides
    @Singleton
    static Configuration provideConfiguration() throws ConfigurationException {
        // Read configuration from the environment. These values have the highest priority
        EnvironmentConfiguration envConfig = new EnvironmentConfiguration();

        // Read configuration from the classpath. Values set here have the lowest priority.
        PropertiesConfiguration classpathConfig = new PropertiesConfiguration();
        try (var input = ConfigurationModule.class.getResourceAsStream("/config/db-manager.properties")) {
            if (input != null) {
                try (var reader = new InputStreamReader(input)) {
                    FileHandler handler = new FileHandler(classpathConfig);
                    handler.load(reader);
                } catch (ConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Build a composite configuration to read layered configuration properties
        CompositeConfiguration compConfig = new CompositeConfiguration();
        compConfig.addConfiguration(envConfig);
        compConfig.addConfiguration(classpathConfig);

        Configuration config = new Configuration(compConfig.getString("db.host"), compConfig.getInt("db.port"), compConfig.getString("db.user"),
                compConfig.getString("db.password"), compConfig.getString("db.name"));

        return config;
    }
}
