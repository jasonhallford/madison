package io.miscellanea.madison.importsvc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.miscellanea.madison.broker.*;
import io.miscellanea.madison.broker.redis.RedisImportMessage;
import io.miscellanea.madison.dal.DataSourceModule;
import io.miscellanea.madison.dal.config.DataSourceConfigModule;
import io.miscellanea.madison.dal.config.DatabaseConfigModule;
import io.miscellanea.madison.entity.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Set;
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

        // Inject a reference to the import message queue.
        Queue<ImportMessage> importQueue = injector.getInstance(new Key<>() {
        });

        // Inject a reference to the Event Service and subscribe to the IMPORT_SCAN event.
        EventService eventService = injector.getInstance(EventService.class);
        eventService.subscribe((event) -> processImportScanEvent(config, importQueue, event),
                Event.Type.IMPORT_SCAN);
        eventService.accept();

        // Register a shutdown handler to terminate the event service and any running
        // executors when the process receives a SIGTERM or SIGINT signal.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            logger.debug("Sent request to terminate task pool executor.");

            try {
                eventService.close();
                logger.debug("Closed event service connection.");

                importQueue.close();
                logger.debug("Closed import queue connection.");
            } catch (Exception e) {
                logger.error("Unable to close event service connection.", e);
            }

            logger.info("Madison Import Service terminated.");
        }));

        logger.info("Madison Import Service initialized and on-line.");

        // Enter into a loop where we consume messages from the import queue.
        while (importQueue.isConnected()) {
            ImportMessage message = importQueue.poll(5, RedisImportMessage.class);
            if (message != null) {
                logger.debug("Received import message: {}", message);
                logger.debug("Submitting import job to executor service.");
                executorService.submit(() -> importDocument(injector, config, eventService, message));
            } else {
                logger.debug("No messages received; re-polling broker.");
            }
        }
    }

    private static void processImportScanEvent(ImportServerConfig importServerConfig,
                                               Queue<ImportMessage> importQueue, Event event) {
        logger.debug("Processing IMPORT_SCAN event.");

        // Walk the import directory and generate an IMPORT event for every entry.
        try (Stream<Path> files = Files.list(Paths.get(importServerConfig.importDir()))) {
            Set<Path> docsToImport = files.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());

            for (Path file : docsToImport) {
                String url = file.toUri().toString();
                var importMessage = new ImportMessage("import-server", url);
                importQueue.publish(importMessage);
                logger.debug("Published message to import file at '{}'.", url);
            }

            logger.debug("Finished processing IMPORT_SCAN event.");
        } catch (Exception e) {
            logger.error("Unable to process IMPORT_SCAN event! This request will be ignored.", e);
        }
    }

    private static void importDocument(Injector injector, ImportServerConfig importServerConfig,
                                       EventService eventService, ImportMessage importMessage) {
        try {
            URL docUrl = new URL(importMessage.getDocumentUrl());

            var importTask = injector.getInstance(ImportDocumentTask.class);
            importTask.setDocumentUrl(docUrl);

            logger.debug("Importing document from URL {}.", docUrl.toExternalForm());
            Document document = importTask.get();
            logger.info("Successfully imported document from URL {}.", docUrl.toExternalForm());
        } catch (Exception e) {
            logger.error("An error occurred while importing document from URL " + importMessage.getDocumentUrl() + "."
                    , e);
        }
    }
}
