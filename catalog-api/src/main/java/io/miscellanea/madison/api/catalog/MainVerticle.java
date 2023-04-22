package io.miscellanea.madison.api.catalog;

import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.document.Document;
import io.miscellanea.madison.repository.DocumentRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

@ApplicationScoped
public class MainVerticle extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private final io.vertx.core.Vertx vertx;
    private final VerticleConfig verticleConfig;
    private final EventService eventService;
    private final Queue<ImportMessage> importQueue;
    private final DocumentRepository documentRepository;

    // Constructors
    @Inject
    public MainVerticle(io.vertx.core.Vertx vertx,
                        VerticleConfig verticleConfig,
                        EventService eventService,
                        Queue<ImportMessage> importQueue,
                        DocumentRepository documentRepository) {
        this.vertx = vertx;
        this.verticleConfig = verticleConfig;
        this.eventService = eventService;
        this.importQueue = importQueue;
        this.documentRepository = documentRepository;
    }

    public void init(@Observes StartupEvent startupEvent, Vertx vertx, MainVerticle verticle,
                     Router router) {
        logger.debug("Configuring API routes.");
        router
                .route()
                .handler(
                        BodyHandler.create(this.verticleConfig.uploadDirectory())
                                .setDeleteUploadedFilesOnEnd(true));
        this.configureRoutes(router);
        vertx.deployVerticle(verticle).await().indefinitely();
    }

    private void configureRoutes(Router router) {
        // Register the body handler so we may access form data later on.
        router.route().handler(BodyHandler.create(this.verticleConfig.uploadDirectory()));

        // Bind import endpoints
        router.get("/api/import/scan").handler(this::getImportScan);
        router.post("/api/import").consumes("application/octet-stream").handler(this::postImport);
        router.post("/api/document").consumes("application/json").handler(this::postDocument);

        // Create the default handler (which will return a bad request code).
        router
                .route()
                .handler(
                        ctx -> {
                            var response = ctx.response();
                            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
                            response.end();
                        });

        logger.debug("API routes successfully configured.");
    }

    private void getImportScan(RoutingContext ctx) {
        logger.debug("Handling request to notify import service.");

        Event event = new Event(Event.Type.IMPORT_SCAN);

        // Dispatch the event to the event service. We execute this as blocking code because we have no
        // guarantee that the underlying implementation won't use blocking calls.
        this.vertx.executeBlocking(
                promise -> {
                    logger.debug("Dispatching event '{}' to the event service.", event);
                    try {
                        this.eventService.publish(event);
                        logger.debug("Event successfully published.");
                        promise.complete();
                    } catch (Exception e) {
                        logger.error("An error occurred while publishing event.", e);
                        promise.fail(e);
                    }
                },
                result -> {
                    if (result.succeeded()) {
                        ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                    } else {
                        ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                    }
                });
    }

    private void postImport(RoutingContext ctx) {
        logger.debug("Handling request to upload POSTed document for import.");

        final Path importFile =
                Path.of(this.verticleConfig.uploadDirectory(), UUID.randomUUID().toString());
        logger.debug("Writing uploaded file to location {}.", importFile);

        if (!ctx.body().isEmpty() && ctx.body().length() > 0) {
            vertx
                    .fileSystem()
                    .writeFile(importFile.toString(), ctx.body().buffer())
                    .onSuccess(
                            result -> {
                                logger.debug("Uploaded file successfully written to {}.", importFile);

                                vertx.executeBlocking(
                                        promise -> {
                                            try {
                                                String docUrl = importFile.toFile().toURI().toURL().toExternalForm();
                                                ImportMessage message = new ImportMessage("catalog-api", docUrl);
                                                this.importQueue.publish(message);
                                                promise.complete();
                                            } catch (Exception e) {
                                                promise.fail(e);
                                            }
                                        },
                                        queueResult -> {
                                            if (queueResult.succeeded()) {
                                                logger.debug(
                                                        "Successfully dispatched import message for file {}.", importFile);
                                                ctx.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();
                                            } else {
                                                logger.error("Unable to dispatch import message.", queueResult.cause());
                                                ctx.response()
                                                        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                                        .end();
                                            }
                                        });
                            })
                    .onFailure(
                            cause -> {
                                logger.error("Unable to write buffer to import file " + importFile + ".", cause);
                                ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                            });
        } else {
            // This is a bad request if the post doesn't contain a file.
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void postDocument(RoutingContext ctx) {
        logger.debug("Handling request to PUT document.");

        if (!ctx.body().isEmpty() && ctx.body().length() > 0) {
            vertx.executeBlocking(
                    promise -> {
                        try {
                            Document document = ctx.body().asPojo(Document.class);
                            this.documentRepository.add(document);
                            promise.complete();
                        } catch (Exception e) {
                            promise.fail(e);
                        }
                    },
                    result -> {
                        if (result.succeeded()) {
                            logger.debug("Successfully stored document in repository.");
                            ctx.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
                        } else {
                            logger.error("Unable to dispatch import message.", result.cause());
                            ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }
                    });
        } else {
            // This is a bad request if the post doesn't contain a file.
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }
}
