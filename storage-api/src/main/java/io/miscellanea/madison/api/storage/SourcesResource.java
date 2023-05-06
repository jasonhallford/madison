package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.Fingerprint;
import io.miscellanea.madison.document.InvalidFingerprintException;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RequestScoped
@Path("sources")
public class SourcesResource {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(SourcesResource.class);

    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public SourcesResource(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    // Action handlers.
    @PUT
    @Path("{fingerprint}")
    @Blocking
    public Response putSource(@RestPath(value = "fingerprint") String fingerprintParam,
                              @RestForm("source") FileUpload sourceFile) {
        if (sourceFile == null || sourceFile.uploadedFile().toFile().length() < 1) {
            logger.warn("Upload form did not contain a 'source' field; returning BAD_REQUEST.");
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        }

        try {
            var fingerprint = new Fingerprint(fingerprintParam);
            logger.debug("Processing PUT request for source {}.", fingerprint);

            var uploadPath = sourceFile.uploadedFile();
            logger.debug("Processing source at path {}.", uploadPath);

            try (BufferedInputStream source = new BufferedInputStream(new FileInputStream(uploadPath.toFile()))) {
                this.documentStore.storeSource(fingerprint, source);
                logger.debug("Successfully stored source {}.", fingerprint);
                return Response.created(new URI("/sources/" + fingerprint)).build();
            } catch (URISyntaxException e) {
                logger.error("Unable to create URI for new resource.", e);
                return Response.serverError().build();
            }
        } catch (InvalidFingerprintException ife) {
            logger.error("Path element '" + fingerprintParam + "' is not a valid fingerprint.", ife);
            return Response.ok().status(RestResponse.Status.BAD_REQUEST).build();
        } catch (IOException ioe) {
            logger.error("Unable to open source " + fingerprintParam + " at " + sourceFile.uploadedFile() + ".", ioe);
            return Response.serverError().build();
        }
    }
}
