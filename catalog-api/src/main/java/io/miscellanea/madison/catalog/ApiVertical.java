package io.miscellanea.madison.catalog;

import io.miscellanea.madison.catalog.config.ApiConfig;
import io.miscellanea.madison.event.Event;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ApiVertical extends AbstractVerticle {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ApiVertical.class);

    private HttpServer httpServer;
    private RabbitMQClient rabbitMQClient;
    private final ApiConfig config;

    // Constructors
    @Inject
    public ApiVertical(ApiConfig config) {
        this.config = config;
    }

    // Initialize the HTTP server on deploy.
    @Override
    public void start(Promise<Void> startPromise) {
        logger.debug("start() method invoked.");

        // Configure the REST endpoint
        logger.debug("Creating API HTTP server.");
        HttpServerOptions opts = new HttpServerOptions().setPort(this.config.restConfig().port());
        httpServer = vertx.createHttpServer(opts);

        Router router = Router.router(vertx);
        this.configureRoutes(router);

        // Configure the RabbitMQ client we use to send event notifications to other services.
        logger.debug("Creating RabbtMQ client.");
        RabbitMQOptions rabbitOpts = new RabbitMQOptions().setHost(this.config.brokerConfig().host())
                .setPort(this.config.brokerConfig().port()).setUser(this.config.brokerConfig().user())
                .setPassword(this.config.brokerConfig().password())
                .setAutomaticRecoveryEnabled(true);
        this.rabbitMQClient = RabbitMQClient.create(vertx, rabbitOpts);

        // Start the RabbitMQ client and start listening for HTTP connections.
        logger.debug("Starting Vert.x RabbitMQ client.");
        this.rabbitMQClient.start()
                .compose(result -> this.rabbitMQClient.exchangeDeclare("madison.event", "fanout", true, false))
                .onSuccess(result -> {
                    logger.debug("Successfully started Vert.x RabbitMQ client and connected to event exchange.");

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
                }).onFailure(cause -> {
                    logger.error("Failed to start RabbitMQ client.", cause);
                    startPromise.fail(cause);
                });
    }

    private void configureRoutes(Router router) {
        // Register the body handler so we may access form data later on.
        router.route().handler(BodyHandler.create(this.config.restConfig().uploadDirectory()));

        // Bind import endpoints
        router.get("/api/import/notify").handler(this::notifyImportService);

        // Create the default handler (which will return a bad request code).
        router.route().handler(ctx -> {
            var response = ctx.response();
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            response.end();
        });
    }

    private void notifyImportService(RoutingContext ctx) {
        logger.debug("Handling request to notify import service.");

        Event event = new Event(Event.Type.IMPORT_ALL);
        JsonObject json = JsonObject.mapFrom(event);

        Buffer msg = Buffer.buffer(json.toString(),"UTF-8");
        this.rabbitMQClient.basicPublish("madison.event", "", msg)
                .onSuccess(result -> {
                    logger.debug("Successfully published IMPORT_ALL event to RabbitMQ event exchange.");
                    ctx.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                })
                .onFailure(cause -> {
                    logger.error("Unable to publish IMPORT_ALL event to RabbitMQ event exchange.", cause);
                    ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                });
    }
}
