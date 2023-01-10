package io.miscellanea.madison.importsvc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.miscellanea.madison.entity.Document;
import io.miscellanea.madison.entity.Event;
import io.miscellanea.madison.importsvc.task.ImportDocument;
import io.miscellanea.madison.service.EventService;
import io.miscellanea.madison.importsvc.config.ServiceConfig;
import io.miscellanea.madison.importsvc.config.ServiceConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        eventService.registerHandler((event) -> processImportScanEvent(config, eventService, event),
                Event.Type.IMPORT_SCAN);
        eventService.registerHandler((event) -> processImportDocumentEvent(injector, config, eventService, event),
                Event.Type.IMPORT_DOCUMENT);
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

    private static void processImportScanEvent(ServiceConfig serviceConfig,
                                               EventService eventService, Event event) {
        logger.debug("Processing IMPORT_SCAN event.");

        // Walk the import directory and generate an IMPORT event for every entry.
        try (Stream<Path> files = Files.list(Paths.get(serviceConfig.importDir()))) {
            Set<Path> docsToImport = files.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());

            for (Path file : docsToImport) {
                String url = file.toUri().toString();
                var importEvent = new Event(Event.Type.IMPORT_DOCUMENT, url);
                eventService.publish(importEvent);
                logger.debug("Published event to import file at '{}'.", url);
            }

            logger.debug("Finished processing IMPORT_SCAN event.");
            eventService.accepted(event, EventService.Disposition.SUCCESS);
        } catch (Exception e) {
            logger.error("Unable to process IMPORT_SCAN event! This request will be ignored.", e);
            eventService.accepted(event, EventService.Disposition.FAILURE_IGNORE);
        }
    }

    private static void processImportDocumentEvent(Injector injector, ServiceConfig serviceConfig,
                                                   EventService eventService, Event event) {
        logger.debug("Processing IMPORT event.");

        try {
            String payload = event.getPayload();
            URL docUrl = new URL(payload);

            var importer = injector.getInstance(ImportDocument.class);
            importer.setDocumentUrl(docUrl);

            CompletableFuture<Document> future = CompletableFuture.supplyAsync(importer, executorService);
            future.whenComplete((document, ex) -> {
                if (ex == null) {
                    logger.debug("Successfully imported document into content store.");
                    eventService.accepted(event, EventService.Disposition.SUCCESS);
                } else {
                    logger.error("Unable to import document into content store.", ex);
                    eventService.accepted(event, EventService.Disposition.FAILURE_IGNORE);
                }
            });
        } catch (Exception e) {
            logger.error("IMPORT event contains an invalid URL.", e);
            eventService.accepted(event, EventService.Disposition.FAILURE_IGNORE);
        }
    }
}
