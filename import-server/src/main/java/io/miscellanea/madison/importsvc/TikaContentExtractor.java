package io.miscellanea.madison.importsvc;

import io.miscellanea.madison.content.ContentException;
import io.miscellanea.madison.content.ContentExtractor;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TikaContentExtractor implements ContentExtractor {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(TikaMetadataExtractor.class);

    private final ResteasyClient client;
    private final ImportServerConfig config;

    // Constructors
    @Inject
    public TikaContentExtractor(@NotNull ImportServerConfig config, @NotNull ResteasyClient client) {
        this.client = client;
        this.config = config;
    }

    // ContentExtractor
    @Override
    public String extract(String fingerprint, URL documentURL) throws ContentException {
        String tikaUrl = String.format("%s/tika/form", this.config.tikaUrl());
        logger.debug("Tika content extraction URL = {}.", tikaUrl);

        try (InputStream documentStream = documentURL.openStream()) {
            WebTarget target = this.client.target(tikaUrl);
            var mdo = new MultipartFormDataOutput();
            mdo.addFormData("upload", documentStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            var entity = new GenericEntity<>(mdo) {
            };

            logger.debug("Posting document to Tika server at {}.", tikaUrl);
            try (Response response = target.request().header("Accept", MediaType.TEXT_PLAIN)
                    .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE))) {
                if (response.getStatus() == HttpStatus.SC_OK) {
                    logger.debug("Received OK response from Tika.");
                    if (response.hasEntity()) {
                        return response.readEntity(String.class);
                    } else {
                        throw new ContentException("Tika was unable to extract content.");
                    }
                } else {
                    throw new ContentException("Tika was unable to extract document conent (SC = " + response.getStatus() + ").");
                }
            } catch (Exception e) {
                throw new ContentException("Unable to parse response from Tika server.", e);
            }
        } catch (IOException ioException) {
            throw new ContentException("Unable to open stream from URL.", ioException);
        }
    }
}
