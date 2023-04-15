package io.miscellanea.madison.api.storage;

import io.vertx.core.Vertx;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageApiDeployer {
    private static final Logger logger = LoggerFactory.getLogger(StorageApiDeployer.class);

    public static void main(String[] args) {
        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Weld CDI container...");
        Weld weld = new Weld();
        try (WeldContainer cdi = weld.initialize()) {
            logger.debug("Weld successfully initialized.");

            // Start the service's REST interface.
            logger.debug("Initializing Vert.x runtime.");
            var vertx = Vertx.vertx();
            logger.debug("Vert.x successfully initialized.");

            logger.debug("Requesting API vertical from CDI container...");
            var vertical = cdi.select(StorageApiVertical.class).get();
            logger.debug("Vertical successfully instantiated.");

            logger.info("Deploying Store API verticle.");
            var response = vertx.deployVerticle(vertical);
            response.onSuccess(result -> logger.info("Store API verticle was successfully deployed."))
                    .onFailure(result -> logger.error("Unable to deploy Store API verticle!", result));
        }
    }
}
