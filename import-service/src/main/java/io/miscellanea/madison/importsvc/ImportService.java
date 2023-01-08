package io.miscellanea.madison.importsvc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.miscellanea.madison.event.Event;
import io.miscellanea.madison.event.EventService;
import io.miscellanea.madison.importsvc.config.ServiceConfig;
import io.miscellanea.madison.importsvc.config.ServiceConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportService {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        logger.info("Starting Madison Import Service.");

        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Guice injector.");
        Injector injector = Guice.createInjector(new ServiceConfigModule(),
                new ServiceModule());
        logger.debug("Guice injector successfully initialized.");

        // Create an executor to manage import taskPoolSize. The underlying pool is closed via
        // the shutdown hook registered at the end of this method.
        ServiceConfig config = injector.getInstance(ServiceConfig.class);
        executorService = Executors.newFixedThreadPool(config.taskPoolSize());

        EventService eventService = injector.getInstance(EventService.class);

        // Register a handler for the IMPORT_ALL event. This
        eventService.registerHandler((event) -> {
            var disposition = processImportAllEvent(eventService, event);
            eventService.handled(event, disposition);
        }, Event.Type.IMPORT_ALL);
        eventService.accept();

        // Register a shutdown handler to terminate the event service and any running
        // executors when the process receives a SIGTERM or SIGINT signal.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            logger.debug("Sent request to terminate task pool executor.");

            try {
                eventService.close();
                logger.debug("Closed event service connection.");
            } catch (Exception e) {
                logger.error("Unable to close event service connection.", e);
            }
            logger.info("Madison Import Service terminated.");
        }));

        logger.info("Madison Import Service online and listening for events.");
    }

    private static EventService.Disposition processImportAllEvent(EventService eventService, Event event) {
        logger.debug("Processing IMPORT_ALL event.");

        return EventService.Disposition.FAILURE_IGNORE;
    }
}
