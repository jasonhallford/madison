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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RequestScoped
@Path("thumbnails")
public class ThumbnailsResource {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailsResource.class);

    private final DocumentStore documentStore;

    // Constructors
    @Inject
    public ThumbnailsResource(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    // Action handlers.
    @PUT
    @Path("{fingerprint}")
    @Blocking
    public Response putThumbnail(@RestPath(value = "fingerprint") String fingerprintParam,
                                 @RestForm("thumbnail") FileUpload thumbnailFile) {
        if (thumbnailFile == null || thumbnailFile.uploadedFile().toFile().length() < 1) {
            logger.warn("Upload form did not contain a 'thumbnail' field; returning BAD_REQUEST.");
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        }

        try {
            var fingerprint = new Fingerprint(fingerprintParam);
            logger.debug("Processing PUT request for thumbnail {}.", fingerprint);

            var uploadPath = thumbnailFile.uploadedFile();
            logger.debug("Processing thumbnail at path {}.", uploadPath);

            try (BufferedInputStream thumbnail = new BufferedInputStream(new FileInputStream(uploadPath.toFile()))) {
                BufferedImage image = ImageIO.read(thumbnail);
                this.documentStore.storeThumbnail(fingerprint, image);
                logger.debug("Successfully stored thumbnail {}.", fingerprint);
                return Response.created(new URI("/thumbnails/" + fingerprint)).build();
            } catch (URISyntaxException e) {
                logger.error("Unable to create URI for new resource.", e);
                return Response.serverError().build();
            }
        } catch (InvalidFingerprintException ife) {
            logger.error("Path element '" + fingerprintParam + "' is not a valid fingerprint.", ife);
            return Response.status(RestResponse.Status.BAD_REQUEST).build();
        } catch (IOException ioe) {
            logger.error("Unable to open thumbnail " + fingerprintParam + " at " + thumbnailFile.uploadedFile() + ".", ioe);
            return Response.serverError().build();
        }
    }
}
