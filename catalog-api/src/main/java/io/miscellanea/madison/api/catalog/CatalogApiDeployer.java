package io.miscellanea.madison.api.catalog;

import io.vertx.core.Vertx;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogApiDeployer {
    private static final Logger logger = LoggerFactory.getLogger(CatalogApiDeployer.class);

    public static void main(String[] args) {
        logger.debug("Initializing Weld CDI container...");
        Weld weld = new Weld();

        WeldContainer cdi = weld.initialize();

        // Start the service's REST interface.
        logger.debug("Initializing Vert.x runtime.");
        var vertx = Vertx.vertx();
        logger.debug("Vert.x runtime initialized.");

        var vertical = cdi.select(CatalogApiVertical.class).get();

        logger.info("Deploying API verticle.");
        var response = vertx.deployVerticle(vertical);
        response.onSuccess(result -> logger.info("API verticle was successfully deployed."))
                .onFailure(result -> logger.error("Unable to deploy API verticle!", result))
                .andThen(result -> {
                    cdi.close(); // We no longer need the CDI container; release the resources.
                });
    }
}
