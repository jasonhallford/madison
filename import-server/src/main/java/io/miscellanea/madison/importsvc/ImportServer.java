package io.miscellanea.madison.importsvc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.miscellanea.madison.broker.BrokerConfigModule;
import io.miscellanea.madison.dal.DataSourceModule;
import io.miscellanea.madison.dal.config.DataSourceConfigModule;
import io.miscellanea.madison.dal.config.DatabaseConfigModule;
import io.miscellanea.madison.entity.Document;
import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
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

public class ImportServer {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);

    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        logger.info("Starting Madison Import Service.");

        // Create the Guice injector to initialize our dependencies.
        logger.debug("Initializing Guice injector.");
        Injector injector = Guice.createInjector(new BrokerConfigModule(),
                new DatabaseConfigModule(),
                new DataSourceConfigModule(),
                new DataSourceModule(),
                new ImportServerConfigModule(),
                new ImportServerModule());
        logger.debug("Guice injector successfully initialized.");

        // Create an executor to manage import taskPoolSize. The underlying pool is closed via
        // the shutdown hook registered at the end of this method.
        ImportServerConfig config = injector.getInstance(ImportServerConfig.class);
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

    private static void processImportScanEvent(ImportServerConfig importServerConfig,
                                               EventService eventService, Event event) {
        logger.debug("Processing IMPORT_SCAN event.");

        // Walk the import directory and generate an IMPORT event for every entry.
        try (Stream<Path> files = Files.list(Paths.get(importServerConfig.importDir()))) {
            Set<Path> docsToImport = files.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());

            for (Path file : docsToImport) {
                String url = file.toUri().toString();
                var importEvent = new Event(Event.Type.IMPORT_DOCUMENT, url);
                eventService.publish(importEvent);
                logger.debug("Published event to import file at '{}'.", url);
            }

            logger.debug("Finished processing IMPORT_SCAN event.");
        } catch (Exception e) {
            logger.error("Unable to process IMPORT_SCAN event! This request will be ignored.", e);
        }
    }

    private static void processImportDocumentEvent(Injector injector, ImportServerConfig importServerConfig,
                                                   EventService eventService, Event event) {
        logger.debug("Processing IMPORT event.");

        try {
            String payload = event.getPayload();
            URL docUrl = new URL(payload);

            var importTask = injector.getInstance(ImportDocumentTask.class);
            importTask.setDocumentUrl(docUrl);

            CompletableFuture<Document> future = CompletableFuture.supplyAsync(importTask, executorService);
            future.whenComplete((document, ex) -> {
                if (ex == null) {
                    logger.debug("Successfully imported document into content store.");
                } else {
                    logger.error("Unable to import document into content store.", ex);
                }
            });
        } catch (Exception e) {
            logger.error("IMPORT event contains an invalid URL.", e);
        }
    }
}
