package io.miscellanea.madison.catalog;

import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ApiVertical extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ApiVertical.class);

    private HttpServer httpServer;
    private final CatalogApiConfig catalogApiConfig;
    private final EventService eventService;

    // Constructors
    @Inject
    public ApiVertical(CatalogApiConfig catalogApiConfig, EventService eventService) {
        this.catalogApiConfig = catalogApiConfig;
        this.eventService = eventService;
    }

    // Initialize the HTTP server on deploy.
    @Override
    public void start(Promise<Void> startPromise) {
        logger.debug("start() method invoked.");

        // Configure the REST endpoint
        logger.debug("Creating API HTTP server.");
        HttpServerOptions opts = new HttpServerOptions().setPort(this.catalogApiConfig.port());
        httpServer = vertx.createHttpServer(opts);

        Router router = Router.router(vertx);
        this.configureRoutes(router);

        logger.debug("Staring API HTTP REST endpoint.");
        this.httpServer.requestHandler(router).listen()
                .onSuccess(httpResult -> {
                    logger.debug("Successfully started API REST endpoint.");
                    startPromise.complete();
                })
                .onFailure(cause -> {
                    logger.error("Failed to start REST endpoint.", cause);
                    startPromise.fail(cause);
                });
    }

    private void configureRoutes(Router router) {
        // Register the body handler so we may access form data later on.
        router.route().handler(BodyHandler.create(this.catalogApiConfig.uploadDirectory()));

        // Bind import endpoints
        router.get("/api/import/scan").handler(this::requestImportScan);

        // Create the default handler (which will return a bad request code).
        router.route().handler(ctx -> {
            var response = ctx.response();
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            response.end();
        });

        logger.debug("API routes successfully configured.");
    }

    private void requestImportScan(RoutingContext ctx) {
        logger.debug("Handling request to notify import service.");

        Event event = new Event(Event.Type.IMPORT_SCAN);

        // Dispatch the event to the event service. We execute this as blocking code because we have no guarantee
        // that the underlying implementation won't use blocking calls.
        vertx.executeBlocking(promise -> {
            logger.debug("Dispatching event '{}' to the event service.", event.toString());
            try {
                this.eventService.publish(event);
                logger.debug("Event successfully published.");
                promise.complete();
            } catch (Exception e) {
                logger.error("An error occurred while publishing event.", e);
                promise.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            } else {
                ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
    }
}
