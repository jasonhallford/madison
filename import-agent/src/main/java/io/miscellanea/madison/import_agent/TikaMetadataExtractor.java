package io.miscellanea.madison.import_agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.miscellanea.madison.content.ContentException;
import io.miscellanea.madison.content.MetadataExtractor;
import io.miscellanea.madison.entity.Author;
import io.miscellanea.madison.entity.Document;
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

public class TikaMetadataExtractor implements MetadataExtractor {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(TikaMetadataExtractor.class);

    private final ResteasyClient client;
    private final ImportAgentConfig config;

    // Constructors
    @Inject
    public TikaMetadataExtractor(@NotNull ImportAgentConfig config, @NotNull ResteasyClient client) {
        this.client = client;
        this.config = config;
    }

    // MetadataExtractionService
    @Override
    public Document extract(String fingerprint, URL documentURL) throws ContentException {
        if (documentURL == null) {
            throw new IllegalArgumentException("documentURL must not be null.");
        }

        String tikaUrl = String.format("%s/meta/form", this.config.tikaUrl());
        logger.debug("Tika metadata URL = {}.", tikaUrl);

        try (InputStream documentStream = documentURL.openStream()) {
            WebTarget target = this.client.target(tikaUrl);
            var mdo = new MultipartFormDataOutput();
            mdo.addFormData("upload", documentStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            var entity = new GenericEntity<>(mdo) {
            };

            logger.debug("Posting document to Tika server at {}.", tikaUrl);
            try (Response response = target.request().header("Accept", MediaType.APPLICATION_JSON)
                    .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE))) {
                if (response.getStatus() == HttpStatus.SC_OK) {
                    logger.debug("Received OK response from Tika.");
                    if (response.hasEntity()) {
                        String jsonResponse = response.readEntity(String.class);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode json = mapper.readTree(jsonResponse);

                        return this.createDocumentFromMetadata(fingerprint, json);
                    } else {
                        throw new ContentException("Tika did not provide metatdata.");
                    }
                } else {
                    throw new ContentException("Tika was unable to extract document metadata (SC = " + response.getStatus() + ").");
                }
            } catch (Exception e) {
                throw new ContentException("Unable to parse response from Tika server.", e);
            }
        } catch (IOException ioException) {
            throw new ContentException("Unable to open content stream from URL.", ioException);
        }
    }

    // Private methods
    private Document createDocumentFromMetadata(String fingerprint, JsonNode metadata) {
        int pageCount = metadata.withArray("pdf:unmappedUnicodeCharsPerPage").size();
        logger.debug("Document page count: {}", pageCount);

        String contentType = metadata.get("Content-Type").asText();
        logger.debug("Document content type: {}", contentType);

        String title = metadata.get("dc:title").asText();
        logger.debug("Document title: {}", title);

        String author = metadata.get("dc:creator").asText();
        logger.debug("Author(s): {}", author);

        return new Document(null, title, fingerprint, pageCount, contentType, null, null,
                Author.fromString(author));
    }
}
