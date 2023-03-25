package io.miscellanea.madison.import_agent;

import io.miscellanea.madison.content.ContentException;
import io.miscellanea.madison.content.ThumbnailGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public class PdfBoxThumbnailGenerator implements ThumbnailGenerator {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(PdfBoxThumbnailGenerator.class);

    // Constructors
    public PdfBoxThumbnailGenerator() {

    }

    // ThumbnailGenerator
    @Override
    public BufferedImage generate(String fingerprint, URL documentUrl) {
        BufferedImage thumbnail;

        try (InputStream in = documentUrl.openStream()) {
            PDDocument doc = PDDocument.load(in);
            PDFRenderer renderer = new PDFRenderer(doc);

            logger.debug("Generating thumbnail for PDF document at {}.", documentUrl.toExternalForm());
            thumbnail = renderer.renderImageWithDPI(0, 300.0F);
            logger.debug("Successfully rendered first page thumbnail.");
        } catch (Exception e) {
            throw new ContentException("Unable to generate thumbnail for " + documentUrl.toExternalForm() + ".", e);
        }

        return thumbnail;
    }
}
