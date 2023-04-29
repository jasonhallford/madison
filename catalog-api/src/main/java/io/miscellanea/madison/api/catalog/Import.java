package io.miscellanea.madison.api.catalog;

import io.miscellanea.madison.broker.Event;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.broker.ImportMessage;
import io.miscellanea.madison.broker.Queue;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Path("import")
public class Import {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Import.class);

    private final EventService eventService;
    private final Queue<ImportMessage> importQueue;

    // Constructors
    @Inject
    public Import(EventService eventService, Queue<ImportMessage> importQueue) {
        this.eventService = eventService;
        this.importQueue = importQueue;
    }

    // Methods
    @GET
    @Path("scan")
    @Blocking
    public Response scan() {
        logger.debug("Emitting IMPORT_SCAN event.");

        try {
            Event event = new Event(Event.Type.IMPORT_SCAN);
            this.eventService.publish(event);
            logger.debug("IMPORT_SCAN event dispatched to event service.");
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Unable to dispatch IMPORT_SCAN event.", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Blocking
    public Response postSource(@RestForm("source") FileUpload sourceFile) {
        logger.debug("Importing document into catalog.");
        if (sourceFile == null || sourceFile.uploadedFile().toFile().length() < 1) {
            logger.warn("Upload form did not contain a 'source' field; returning BAD_REQUEST.");
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        }

        try {
            String docUrl = sourceFile.uploadedFile().toFile().toURI().toString();
            ImportMessage message = new ImportMessage("catalog-api", docUrl);
            this.importQueue.publish(message);
            logger.debug("Import message successfully published to queue.");
            return Response.accepted().build();
        } catch (Exception e) {
            logger.error("Unable to publish import message to queue.", e);
            return Response.serverError().build();
        }
    }
}
