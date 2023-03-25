package io.miscellanea.madison.api.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.miscellanea.madison.broker.BrokerConfigModule;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreApiDeployer {
    private static final Logger logger = LoggerFactory.getLogger(StoreApiDeployer.class);

    public static void main(String[] args) {
        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Guice injector.");
        Injector injector = Guice.createInjector(new BrokerConfigModule(),
                new StoreApiConfigModule(),
                new StoreApiModule());
        logger.debug("Guice injector successfully initialized.");

        // Start the service's REST interface.
        logger.debug("Initializing Vert.x runtime.");
        var vertx = Vertx.vertx();
        logger.debug("Vert.x runtime initialized.");

        var vertical = injector.getInstance(StoreApiVertical.class);

        logger.info("Deploying Store API verticle.");
        var response = vertx.deployVerticle(vertical);
        response.onSuccess(result -> logger.info("Store API verticle was successfully deployed."))
                .onFailure(result -> logger.error("Unable to deploy Store API verticle!", result));
    }
}
