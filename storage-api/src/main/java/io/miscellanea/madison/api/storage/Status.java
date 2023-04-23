package io.miscellanea.madison.api.storage;

import io.miscellanea.madison.content.ContentException;
import io.miscellanea.madison.document.DocumentStore;
import io.miscellanea.madison.document.Fingerprint;
import io.miscellanea.madison.document.InvalidFingerprintException;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Produces({MediaType.APPLICATION_JSON})
@Path("status")
public class Status {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Status.class);

    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public Status(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    // Action handlers.
    @GET
    @Path("{fingerprint}")
    @Blocking
    public Response getStatus(@RestPath(value = "fingerprint")
                              String fingerprintParam) {
        try {
            var fingerprint = new Fingerprint(fingerprintParam);
            logger.debug("Processing PUT request for source {}.", fingerprint);

            var status = this.documentStore.status(fingerprint);
            return Response.ok(status, MediaType.APPLICATION_JSON_TYPE).build();
        } catch (InvalidFingerprintException ife) {
            logger.error("Path element '" + fingerprintParam + "' is not a valid fingerprint.", ife);
            return Response.noContent().status(RestResponse.Status.BAD_REQUEST).build();
        } catch (ContentException ce) {
            logger.error("Unable to retrieve status for source " + fingerprintParam + ".", ce);
            return Response.serverError().build();
        }
    }
}
