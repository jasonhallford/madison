package io.miscellanea.madison.api.catalog;

import io.miscellanea.madison.api.catalog.entity.DocumentRepository;
import io.miscellanea.madison.document.Document;
import io.miscellanea.madison.entity.EntityMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@RequestScoped
@Path("documents")
public class DocumentsResource {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(DocumentsResource.class);

    private final DocumentRepository documentRepository;
    private final EntityMapper<io.miscellanea.madison.document.Document,
            io.miscellanea.madison.api.catalog.entity.Document> mapper;

    // Constructors
    @Inject
    public DocumentsResource(DocumentRepository documentRepository,
                             EntityMapper<io.miscellanea.madison.document.Document,
                                     io.miscellanea.madison.api.catalog.entity.Document> mapper) {
        this.documentRepository = documentRepository;
        this.mapper = mapper;
    }

    // Methods
    @POST
    @Consumes("application/json")
    @Transactional
    public Response postDocument(Document document) {
        logger.debug("Adding new document to repository.");
        if (document == null) {
            logger.debug("Received POST with empty body. Returning BAD REQUEST.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            io.miscellanea.madison.api.catalog.entity.Document persistentDocument =
                    this.mapper.map(document);

            this.documentRepository.persist(persistentDocument);
            logger.debug("Successfully added document to repository.");
            return Response.created(new URI("/documents")).build();
        } catch (Exception e) {
            logger.error("Unable to add document to repository.", e);
            return Response.serverError().build();
        }
    }
}
