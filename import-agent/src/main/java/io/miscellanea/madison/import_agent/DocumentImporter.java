package io.miscellanea.madison.import_agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.miscellanea.madison.broker.EventService;
import io.miscellanea.madison.content.*;
import io.miscellanea.madison.document.FingerprintGenerator;
import io.miscellanea.madison.entity.Document;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentImporter implements Supplier<Document> {
  // Fields
  private static final Logger logger = LoggerFactory.getLogger(DocumentImporter.class);

  private final ImportAgentConfig config;
  private final FingerprintGenerator fingerprintGenerator;
  private final MetadataExtractor metadataExtractor;
  private final ContentExtractor contentExtractor;
  private final EventService eventService;
  private final ThumbnailGenerator thumbnailGenerator;
  private final ResteasyClient resteasyClient;
  private URL sourceUrl;
  private final ObjectMapper objectMapper;

  // Constructors
  @Inject
  public DocumentImporter(
      @NotNull ImportAgentConfig config,
      @NotNull FingerprintGenerator fingerprintGenerator,
      @NotNull MetadataExtractor metadataExtractor,
      @NotNull ContentExtractor contentExtractor,
      @NotNull ThumbnailGenerator thumbnailGenerator,
      @NotNull EventService eventService,
      @NotNull ResteasyClient resteasyClient) {
    this.config = config;
    this.fingerprintGenerator = fingerprintGenerator;
    this.metadataExtractor = metadataExtractor;
    this.contentExtractor = contentExtractor;
    this.thumbnailGenerator = thumbnailGenerator;
    this.eventService = eventService;
    this.resteasyClient = resteasyClient;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  // Properties
  public void setSourceUrl(URL sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  // Callable
  @Override
  public Document get() {
    if (this.sourceUrl == null) {
      throw new IllegalStateException("Unable to execute task: document URL is null.");
    }
    logger.info("Importing document at {}.", this.sourceUrl.toExternalForm());

    String fingerprint = this.fingerprintGenerator.fromUrl(this.sourceUrl);
    logger.debug(
        "Fingerprint for document at {} is {}.", this.sourceUrl.toExternalForm(), fingerprint);

    try {
      Document document = this.metadataExtractor.extract(fingerprint, sourceUrl);
      BufferedImage thumbnail = this.thumbnailGenerator.generate(fingerprint, sourceUrl);
      String text = this.contentExtractor.extract(fingerprint, sourceUrl);

      this.storeContent(document, thumbnail, text);
      this.updateCatalog(document);

      return document;
    } catch (ContentException ce) {
      throw ce;
    } catch (Exception e) {
      throw new ContentException(
          "Unable to extract metadata from document at URL "
              + this.sourceUrl.toExternalForm()
              + ".",
          e);
    }
  }

  // Private methods
  private void updateCatalog(Document document) throws ContentException {
    String targetUrl =
        String.format("%s/api/document", this.config.catalogUrl(), document.getFingerprint());

    try {
      WebTarget target = this.resteasyClient.target(targetUrl);
      if (target instanceof ResteasyWebTarget resteasyWebTarget) {
        resteasyWebTarget.setChunked(true);
      }

      logger.debug("POSTing document to catalog API at {}.", targetUrl);
      String json = this.objectMapper.writeValueAsString(document);
      try (Response response =
          target.request().post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE))) {
        if (response.getStatus() == HttpStatus.SC_CREATED) {
          logger.debug("Received CREATED response from Catalog API.");
        } else {
          throw new ContentException(
              "Catalog API rejected content (SC = " + response.getStatus() + ").");
        }
      } catch (Exception e) {
        throw new ContentException("Unable to parse response from Catalog API.", e);
      }
    } catch (Exception e) {
      if (e instanceof ContentException ce) {
        throw ce;
      } else {
        throw new ContentException("Unable to POST document to Catalog API.", e);
      }
    }
  }

  private void storeContent(Document document, BufferedImage thumbnail, String text)
      throws ContentException {
    // Step 1: Store the source document
    String targetUrl =
        String.format("%s/api/sources/%s", this.config.storageUrl(), document.getFingerprint());
    try (InputStream sourceStream = this.sourceUrl.openStream()) {
      this.putDocumentResource(targetUrl, sourceStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    } catch (Exception e) {
      if (e instanceof ContentException contentException) {
        throw contentException;
      } else {
        throw new ContentException("Unable to PUT source document to storage API.", e);
      }
    }

    // Step 2: Store the thumbnail
    targetUrl =
        String.format("%s/api/thumbnails/%s", this.config.storageUrl(), document.getFingerprint());
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(thumbnail, "png", byteArrayOutputStream);

      try (InputStream thumbnailStream =
          new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
        this.putDocumentResource(
            targetUrl, thumbnailStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
      } catch (Exception e) {
        if (e instanceof ContentException contentException) {
          throw contentException;
        } else {
          throw new ContentException("Unable to PUT source document to storage API.", e);
        }
      }
    } catch (Exception e) {
      throw new ContentException(
          "Unable to serialize thumbnail for transmission to Storage API.", e);
    }

    // Step 1: Store the text
    targetUrl =
        String.format("%s/api/texts/%s", this.config.storageUrl(), document.getFingerprint());
    try (InputStream textStream = new ByteArrayInputStream(text.getBytes("UTF-8"))) {
      this.putDocumentResource(targetUrl, textStream, MediaType.TEXT_PLAIN_TYPE);
    } catch (Exception e) {
      if (e instanceof ContentException contentException) {
        throw contentException;
      } else {
        throw new ContentException("Unable to PUT source document to storage API.", e);
      }
    }
  }

  private void putDocumentResource(String targetUrl, InputStream contentStream, MediaType mediaType)
      throws ContentException {

    try {
      WebTarget target = this.resteasyClient.target(targetUrl);
      if (target instanceof ResteasyWebTarget resteasyWebTarget) {
        resteasyWebTarget.setChunked(true);
      }

      logger.debug("PUTting document to Storage API at {}.", targetUrl);
      try (Response response = target.request().put(Entity.entity(contentStream, mediaType))) {
        if (response.getStatus() == HttpStatus.SC_CREATED) {
          logger.debug("Received CREATED response from Storage API.");
        } else {
          throw new ContentException(
              "Storage API rejected content (SC = " + response.getStatus() + ").");
        }
      } catch (Exception e) {
        throw new ContentException("Unable to parse response from Storage API.", e);
      }
    } catch (Exception e) {
      if (e instanceof ContentException ce) {
        throw ce;
      } else {
        throw new ContentException("Unable to read content from input stream.", e);
      }
    }
  }
}
