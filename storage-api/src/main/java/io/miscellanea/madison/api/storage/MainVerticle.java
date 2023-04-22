package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.Fingerprint;
import io.miscellanea.madison.document.Status;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;


@ApplicationScoped
public class MainVerticle extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private final io.vertx.core.Vertx vertx;
    private final VerticleConfig verticleConfig;
    private final EventService eventService;
    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public MainVerticle(io.vertx.core.Vertx vertx,
                        VerticleConfig verticleConfig, DocumentStore documentStore, EventService eventService) {
        this.vertx = vertx;
        this.verticleConfig = verticleConfig;
        this.documentStore = documentStore;
        this.eventService = eventService;
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
        // Bind import endpoints
        router.put("/api/sources/:fingerprint").handler(this::putSource);
        router.put("/api/thumbnails/:fingerprint").handler(this::putThumbnail);
        router.put("/api/texts/:fingerprint").handler(this::putText);
        router.get("/api/status/:fingerprint").handler(this::getStatus);

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

    private void getStatus(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));
            logger.debug("Processing GET status for source {}.", fingerprint);

            vertx.executeBlocking(
                    promise -> {
                        try {
                            var status = this.documentStore.status(fingerprint);
                            promise.complete(status);
                        } catch (Exception e) {
                            promise.fail(e);
                        }
                    },
                    result -> {
                        if (result.succeeded()) {
                            Status status = (Status) result.result();
                            logger.debug("Retrieved status for {}: {}.", fingerprint, status);

                            ctx.response().setStatusCode(HttpResponseStatus.OK.code());
                            ctx.json(status);
                        } else {
                            logger.error("Failed to store source for " + fingerprint + ".", result.cause());
                            ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }
                    });
        } catch (IllegalArgumentException e) {
            logger.error(
                    "BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putSource(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));

            if (!ctx.body().isEmpty() && ctx.body().length() > 0) {
                logger.debug("Processing PUT for source {}.", fingerprint);
                byte[] content = ctx.body().buffer().getBytes();

                try (ByteArrayInputStream source = new ByteArrayInputStream(content)) {
                    vertx.executeBlocking(
                            promise -> {
                                try {
                                    this.documentStore.storeSource(fingerprint, source);
                                    promise.complete();
                                } catch (Exception e) {
                                    promise.fail(e);
                                }
                            },
                            result -> {
                                if (result.succeeded()) {
                                    logger.debug("Source successfully stored for {}.", fingerprint);
                                    ctx.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
                                } else {
                                    logger.error("Failed to store source for " + fingerprint + ".", result.cause());
                                    ctx.response()
                                            .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                            .end();
                                }
                            });
                } catch (Exception e) {
                    logger.error("Unable to create source URL for " + fingerprint + ".", e);
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }

            } else {
                logger.info("Received empty body; request ignored.");
            }
        } catch (IllegalArgumentException e) {
            logger.error(
                    "BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putThumbnail(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));

            if (!ctx.body().isEmpty() && ctx.body().length() > 0) {
                logger.debug("Processing PUT for thumbnail {}.", fingerprint);
                byte[] content = ctx.body().buffer().getBytes();

                try (ByteArrayInputStream source = new ByteArrayInputStream(content)) {
                    vertx.executeBlocking(
                            promise -> {
                                try {
                                    BufferedImage image = ImageIO.read(source);
                                    this.documentStore.storeThumbnail(fingerprint, image);
                                    promise.complete();
                                } catch (Exception e) {
                                    promise.fail(e);
                                }
                            },
                            result -> {
                                if (result.succeeded()) {
                                    logger.debug("Successfully stored thumbnail for {}.", fingerprint);
                                    ctx.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
                                } else {
                                    logger.error(
                                            "Failed to store thumbnail for " + fingerprint + ".", result.cause());
                                    ctx.response()
                                            .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                            .end();
                                }
                            });
                } catch (Exception e) {
                    logger.error("Unable to create thumbnail URL for " + fingerprint + ".", e);
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            }
        } catch (Exception e) {
            logger.error(
                    "BAD REQUEST: " + ctx.pathParam("fingerprint") + " is not a valid document fingerprint.",
                    e);
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putText(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));

            if (!ctx.body().isEmpty() && ctx.body().length() > 0) {
                logger.debug("Processing PUT for text {}.", fingerprint);
                var text = ctx.body().buffer().toString("UTF-8");

                vertx.executeBlocking(
                        promise -> {
                            try {
                                this.documentStore.storeText(fingerprint, text);
                                promise.complete();
                            } catch (Exception e) {
                                promise.fail(e);
                            }
                        },
                        result -> {
                            if (result.succeeded()) {
                                logger.debug("Successfully stored text for {}.", fingerprint);
                                ctx.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
                            } else {
                                logger.error("Failed to store text for " + fingerprint + ".", result.cause());
                                ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                            }
                        });
            }
        } catch (IllegalArgumentException e) {
            logger.error(
                    "BAD REQUEST: " + ctx.pathParam("fingerprint") + " is not a valid document fingerprint.",
                    e);
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }
}
