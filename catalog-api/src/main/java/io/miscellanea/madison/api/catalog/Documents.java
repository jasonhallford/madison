package io.miscellanea.madison.api.catalog;

import io.miscellanea.madison.document.Document;
import io.miscellanea.madison.repository.DocumentRepository;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@RequestScoped
@Path("documents")
public class Documents {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Documents.class);

    private final DocumentRepository documentRepository;

    // Constructors
    @Inject
    public Documents(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // Methods
    @POST
    @Consumes("application/json")
    @Blocking
    public Response postDocument(Document document) {
        logger.debug("Adding new document to repository.");
        if (document == null) {
            logger.debug("Received POST with empty body. Returning BAD REQUEST.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            this.documentRepository.add(document);
            logger.debug("Successfully added document to repository.");
            return Response.created(new URI("/documents")).build();
        } catch (Exception e) {
            logger.error("Unable to add document to repository.", e);
            return Response.serverError().build();
        }
    }
}
