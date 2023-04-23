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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RequestScoped
@Path("texts")
public class Texts {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Texts.class);

    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public Texts(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    // Action handlers.
    @PUT
    @Path("{fingerprint}")
    @Blocking
    public Response putThumbnail(@RestPath(value = "fingerprint") String fingerprintParam,
                                 @RestForm("text") FileUpload textFile) {
        if (textFile == null || textFile.uploadedFile().toFile().length() < 1) {
            logger.warn("Upload form did not contain a 'text' field; returning BAD_REQUEST.");
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        }

        try {
            var fingerprint = new Fingerprint(fingerprintParam);
            logger.debug("Processing PUT request for text {}.", fingerprint);

            var uploadPath = textFile.uploadedFile();
            logger.debug("Processing text at path {}.", uploadPath);

            String text = Files.readString(uploadPath, StandardCharsets.UTF_8);
            this.documentStore.storeText(fingerprint, text);
            logger.debug("Successfully stored text {}.", fingerprint);
            return Response.created(new URI("/texts/" + fingerprint)).build();
        } catch (InvalidFingerprintException ife) {
            logger.error("Path element '" + fingerprintParam + "' is not a valid fingerprint.", ife);
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        } catch (IOException ioe) {
            logger.error("Unable to open text " + fingerprintParam + " at " + textFile.uploadedFile() + ".", ioe);
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            logger.error("Unable to create URI for new resource.", e);
            return Response.serverError().build();
        }
    }
}
