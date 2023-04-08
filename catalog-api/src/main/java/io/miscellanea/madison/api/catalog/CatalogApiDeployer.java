package io.miscellanea.madison.api.catalog;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.miscellanea.madison.broker.BrokerConfigModule;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogApiDeployer {
    private static final Logger logger = LoggerFactory.getLogger(CatalogApiDeployer.class);

    public static void main(String[] args) {
        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Guice injector.");
        Injector injector = Guice.createInjector(new BrokerConfigModule(),
                new CatalogApiConfigModule(),
                new CatalogApiModule());
        logger.debug("Guice injector successfully initialized.");

        // Start the service's REST interface.
        logger.debug("Initializing Vert.x runtime.");
        var vertx = Vertx.vertx();
        logger.debug("Vert.x runtime initialized.");

        var vertical = injector.getInstance(CatalogApiVertical.class);

        logger.info("Deploying API verticle.");
        var response = vertx.deployVerticle(vertical);
        response.onSuccess(result -> logger.info("API verticle was successfully deployed."))
                .onFailure(result -> logger.error("Unable to deploy API verticle!", result));
    }
}