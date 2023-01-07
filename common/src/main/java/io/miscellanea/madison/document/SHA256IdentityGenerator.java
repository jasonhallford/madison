package io.miscellanea.madison.document;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

/**
 * Generates a unique document identity as a string-encoded SHA256
 * hash of the source's bytes.
 */
public class SHA256IdentityGenerator implements IdentityGenerator {
    @Override
    public String fromUrl(@NotNull URL url) throws DocumentException {
        if (url == null) {
            throw new IllegalArgumentException("fromLocation must not be null.");
        }

        // Open the stream and use it to compute the SHA256 hash
        String id;
        try (InputStream in = url.openStream()) {
            id = DigestUtils.sha256Hex(in);
        } catch (IOException e) {
            throw new DocumentException(String.format("Unable to generate identity from resource at location '%s'.",
                    url.toExternalForm()), e);
        }

        return id;
    }

    @Override
    public String fromBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null.");
        }

        String id = DigestUtils.sha256Hex(bytes);

        return id;
    }

    @Override
    public String fromFile(File file) throws DocumentException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }

        String id;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            id = DigestUtils.sha256Hex(in);
        } catch (Exception e) {
            throw new DocumentException("Unable to generate ID from file '" + file.getAbsolutePath() + "'.", e);
        }

        return id;
    }
}
