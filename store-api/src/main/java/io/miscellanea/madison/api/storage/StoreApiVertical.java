package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.Fingerprint;
import io.miscellanea.madison.document.Status;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StoreApiVertical extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(StoreApiVertical.class);

    private HttpServer httpServer;
    private final StoreApiConfig storeApiConfig;
    private final EventService eventService;
    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public StoreApiVertical(StoreApiConfig storeApiConfig, DocumentStore documentStore, EventService eventService) {
        this.storeApiConfig = storeApiConfig;
        this.documentStore = documentStore;
        this.eventService = eventService;
    }

    // Initialize the HTTP server on deploy.
    @Override
    public void start(Promise<Void> startPromise) {
        logger.debug("start() method invoked.");

        // Configure the REST endpoint
        logger.debug("Creating HTTP server on port TCP {}.", this.storeApiConfig.port());
        HttpServerOptions opts = new HttpServerOptions().setPort(this.storeApiConfig.port());
        httpServer = vertx.createHttpServer(opts);

        Router router = Router.router(vertx);
        this.configureRoutes(router);

        logger.debug("Staring API HTTP REST endpoint.");
        this.httpServer.requestHandler(router).listen()
                .onSuccess(httpResult -> {
                    logger.debug("Successfully started HTTP server. Madison Store REST API is listening on port TCP {}.",
                            this.storeApiConfig.port());
                    startPromise.complete();
                })
                .onFailure(cause -> {
                    logger.error("Failed to start Madison Store REST API HTTP server.", cause);
                    startPromise.fail(cause);
                });
    }

    private void configureRoutes(Router router) {
        // Register the body handler so we may access form data later on.
        router.route().handler(BodyHandler.create(this.storeApiConfig.uploadDirectory())
                .setDeleteUploadedFilesOnEnd(true));

        // Bind import endpoints
        router.put("/api/sources/:fingerprint").handler(this::putSource);
        router.put("/api/thumbnails/:fingerprint").handler(this::putThumbnail);
        router.put("/api/texts/:fingerprint").handler(this::putText);
        router.get("/api/status/:fingerprint").handler(this::getStatus);

        // Create the default handler (which will return a bad request code).
        router.route().handler(ctx -> {
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

            vertx.executeBlocking(promise -> {
                try {
                    var status = this.documentStore.status(fingerprint);
                    promise.complete(status);
                } catch (Exception e) {
                    promise.fail(e);
                }
            }, result -> {
                if (result.succeeded()) {
                    Status status = (Status) result.result();
                    logger.debug("Retrieved status for {}: {}.", fingerprint, status);

                    ctx.json(status);
                    ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                } else {
                    logger.error("Failed to store source for " + fingerprint + ".", result.cause());
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            });
        } catch (IllegalArgumentException e) {
            logger.error("BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putSource(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));
            logger.debug("Processing PUT for source {}.", fingerprint);

            List<FileUpload> uploads = ctx.fileUploads();
            if (uploads.size() > 0) {
                try {
                    URL docUrl = new File(uploads.get(0).uploadedFileName()).toURI().toURL();

                    vertx.executeBlocking(promise -> {
                        try {
                            this.documentStore.storeSource(fingerprint, docUrl);
                            promise.complete();
                        } catch (Exception e) {
                            promise.fail(e);
                        }
                    }, result -> {
                        if (result.succeeded()) {
                            logger.debug("Source successfully stored for {}.", fingerprint);
                            ctx.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();
                        } else {
                            logger.error("Failed to store source for " + fingerprint + ".", result.cause());
                            ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }
                    });
                } catch (Exception e) {
                    logger.error("Unable to create source URL for " + fingerprint + ".", e);
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            } else {
                // This is a bad request if the post doesn't contain a file.
                ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        } catch (IllegalArgumentException e) {
            logger.error("BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putThumbnail(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));
            logger.debug("Processing PUT for thumbnail {}.", fingerprint);

            List<FileUpload> uploads = ctx.fileUploads();
            if (uploads.size() > 0) {
                try {
                    String uploadPath = uploads.get(0).uploadedFileName();
                    URL imageURL = new File(uploadPath).toURI().toURL();

                    vertx.executeBlocking(promise -> {
                        try {
                            BufferedImage image = ImageIO.read(imageURL);
                            this.documentStore.storeThumbnail(fingerprint, image);
                            promise.complete();
                        } catch (Exception e) {
                            promise.fail(e);
                        }
                    }, result -> {
                        if (result.succeeded()) {
                            logger.debug("Successfully stored thumbnail for {}.", fingerprint);
                            ctx.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();
                        } else {
                            logger.error("Failed to store thumbnail for " + fingerprint + ".", result.cause());
                            ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }

                        try {
                            Files.delete(Path.of(uploadPath));
                        } catch (Exception e) {
                            logger.error("Unable to delete temporary thumbnail file at " + uploadPath + ".", e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Unable to create thumbnail URL for " + fingerprint + ".", e);
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            } else {
                // This is a bad request if the post doesn't contain a file.
                ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        } catch (IllegalArgumentException e) {
            logger.error("BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void putText(RoutingContext ctx) {
        try {
            Fingerprint fingerprint = new Fingerprint(ctx.pathParam("fingerprint"));
            logger.debug("Processing PUT for text {}.", fingerprint);

            List<FileUpload> uploads = ctx.fileUploads();
            if (uploads.size() > 0) {
                String uploadPath = uploads.get(0).uploadedFileName();

                vertx.executeBlocking(promise -> {
                    try {
                        String text = Files.readString(Path.of(uploadPath));
                        this.documentStore.storeText(fingerprint, text);
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, result -> {
                    if (result.succeeded()) {
                        logger.debug("Successfully stored text for {}.", fingerprint);
                        ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                    } else {
                        logger.error("Failed to store text for " + fingerprint + ".", result.cause());
                        ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                    }

                    try {
                        Files.delete(Path.of(uploadPath));
                    } catch (Exception e) {
                        logger.error("Unable to delete temporary text file at " + uploadPath + ".", e);
                    }
                });
            } else {
                // This is a bad request if the post doesn't contain a file.
                ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        } catch (IllegalArgumentException e) {
            logger.error("BAD REQUEST: {} is not a valid document fingerprint.", ctx.pathParam("fingerprint"));
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }
}
