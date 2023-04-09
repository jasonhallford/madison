package io.miscellanea.madison.config;

import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigProducer<T> {
  private static final Logger logger = LoggerFactory.getLogger(ConfigProducer.class);

  private final String propertiesFileName;

  // Constructor to set ConfigObjectProducer
  public ConfigProducer(@NotNull String propertiesFileName) {
    this.propertiesFileName = propertiesFileName;
  }

  protected abstract T buildCustomConfig(CompositeConfiguration compositeConfiguration);

  protected final T buildConfig() throws ConfigException {
    // Read the JVM system properties
    SystemConfiguration systemConfiguration = new SystemConfiguration();

    // Read configuration from the environment. These values have the highest priority
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();

    // Read configuration from the classpath. Values set here have the lowest priority.
    PropertiesConfiguration classpathConfig = new PropertiesConfiguration();
    logger.debug("Loading classpath configuration properties from {}.", this.propertiesFileName);
    try (var input = this.getClass().getResourceAsStream(this.propertiesFileName)) {
      if (input != null) {
        try (var reader = new InputStreamReader(input)) {
          FileHandler handler = new FileHandler(classpathConfig);
          handler.load(reader);
        } catch (ConfigurationException e) {
          throw new ConfigException("Unable to read configuration file.", e);
        }
      }
    } catch (IOException e) {
      throw new ConfigException("Unable to read configuration from classpath resource.", e);
    }

    // Build a composite configuration to read layered configuration properties
    CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
    compositeConfiguration.addConfiguration(systemConfiguration);
    compositeConfiguration.addConfiguration(envConfig);
    compositeConfiguration.addConfiguration(classpathConfig);

    return this.buildCustomConfig(compositeConfiguration);
  }
}
