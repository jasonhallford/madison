package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.miscellanea.madison.broker.redis.RedisImportMessage;
import io.miscellanea.madison.document.Document;
import jakarta.enterprise.util.TypeLiteral;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportAgent {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ImportAgent.class);

    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        logger.info("Starting Madison Import Service.");

        logger.debug("Initializing Weld CDI container...");
        Weld weld = new Weld();
        WeldContainer cdi = weld.initialize();
        logger.debug("Weld successfully initialized.");

        // Create an executor to manage import taskPoolSize. The underlying pool is closed via
        // the shutdown hook registered at the end of this method.
        ImportAgentConfig config = cdi.select(ImportAgentConfig.class).get();
        executorService = Executors.newFixedThreadPool(config.taskPoolSize());

        // Inject a reference to the import message queue.
        Queue<ImportMessage> importQueue = cdi.select(new TypeLiteral<Queue<ImportMessage>>() {
        }).get();

        // Inject a reference to the Event Service and subscribe to the IMPORT_SCAN event.
        EventService eventService = cdi.select(EventService.class).get();
        eventService.subscribe(
                (event) -> processImportScanEvent(config, importQueue, event), Event.Type.IMPORT_SCAN);
        eventService.accept();

        // Register a shutdown handler to terminate the event service and any running
        // executors when the process receives a SIGTERM or SIGINT signal.
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    logger.debug("Terminating task pool executor.");
                                    executorService.shutdown();

                                    try {
                                        logger.debug("Closing event service connection.");
                                        eventService.close();

                                        logger.debug("Closing import queue connection.");
                                        importQueue.close();

                                        logger.debug("Shutting down Weld container.");
                                        cdi.close();
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
                executorService.submit(() -> importDocument(cdi, config, eventService, message));
            } else {
                logger.debug("No messages received; re-polling broker.");
            }
        }
    }

    private static void processImportScanEvent(
            ImportAgentConfig importAgentConfig, Queue<ImportMessage> importQueue, Event event) {
        logger.debug("Processing IMPORT_SCAN event.");

        // Walk the import directory and generate an IMPORT event for every entry.
        try (Stream<Path> files = Files.list(Paths.get(importAgentConfig.importDir()))) {
            Set<Path> docsToImport =
                    files.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());

            for (Path file : docsToImport) {
                String url = file.toUri().toString();
                var importMessage = new ImportMessage("import-agent", url);
                importQueue.publish(importMessage);
                logger.debug("Published message to import file at '{}'.", url);
            }

            logger.debug("Finished processing IMPORT_SCAN event.");
        } catch (Exception e) {
            logger.error("Unable to process IMPORT_SCAN event! This request will be ignored.", e);
        }
    }

    private static void importDocument(
            WeldContainer cdi,
            ImportAgentConfig importAgentConfig,
            EventService eventService,
            ImportMessage importMessage) {
        try {
            URL docUrl = new URL(importMessage.getDocumentUrl());

            var importer = cdi.select(DocumentImporter.class).get();
            importer.setSourceUrl(docUrl);

            logger.debug("Importing document from URL {}.", docUrl.toExternalForm());
            Document document = importer.get();
            logger.info("Successfully imported document from URL {}.", docUrl.toExternalForm());

            if (importAgentConfig.deleteAfterImport()) {
                if (docUrl.getProtocol().equalsIgnoreCase("file")) {
                    Path filePath = Path.of(docUrl.toURI());
                    try {
                        Files.delete(filePath);
                    } catch (Exception e) {
                        logger.warn("Unable to delete imported file {}: {}", filePath, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(
                    "An error occurred while importing document from URL "
                            + importMessage.getDocumentUrl()
                            + ".",
                    e);
        }
    }
}
