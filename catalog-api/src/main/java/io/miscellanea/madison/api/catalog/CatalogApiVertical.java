package io.miscellanea.madison.api.catalog;

import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogApiVertical extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(CatalogApiVertical.class);

    private HttpServer httpServer;
    private final CatalogApiConfig catalogApiConfig;
    private final EventService eventService;
    private final Queue<ImportMessage> importQueue;

    // Constructors
    @Inject
    public CatalogApiVertical(CatalogApiConfig catalogApiConfig, EventService eventService, Queue<ImportMessage> importQueue) {
        this.catalogApiConfig = catalogApiConfig;
        this.eventService = eventService;
        this.importQueue = importQueue;
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
        router.get("/api/import/scan").handler(this::handleImportScan);
        router.post("/api/import").handler(this::handleImport);

        // Create the default handler (which will return a bad request code).
        router.route().handler(ctx -> {
            var response = ctx.response();
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            response.end();
        });

        logger.debug("API routes successfully configured.");
    }

    private void handleImportScan(RoutingContext ctx) {
        logger.debug("Handling request to notify import service.");

        Event event = new Event(Event.Type.IMPORT_SCAN);

        // Dispatch the event to the event service. We execute this as blocking code because we have no guarantee
        // that the underlying implementation won't use blocking calls.
        vertx.executeBlocking(promise -> {
            logger.debug("Dispatching event '{}' to the event service.", event);
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

    private void handleImport(RoutingContext ctx) {
        logger.debug("Handling request to import document.");

        List<FileUpload> uploads = ctx.fileUploads();
        if (uploads.size() > 0) {
            try {
                String uploadPath = uploads.get(0).uploadedFileName();
                String docUrl = new File(uploadPath).toURI().toURL().toExternalForm();
                ImportMessage message = new ImportMessage("catalog-api", docUrl);

                vertx.executeBlocking(promise -> {
                    try {
                        this.importQueue.publish(message);
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, result -> {
                    if (result.succeeded()) {
                        logger.debug("Successfully dispatched import message for file {}.", docUrl);
                        ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                    } else {
                        logger.error("Unable to dispatch import message.", result.cause());
                        ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                    }
                });
            } catch (Exception e) {
                logger.error("Unable to dispatch import message.", e);
                ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        } else {
            // This is a bad request if the post doesn't contain a file.
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }
}
