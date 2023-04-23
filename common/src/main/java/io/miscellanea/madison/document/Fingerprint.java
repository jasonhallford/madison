package io.miscellanea.madison.document;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Fingerprint {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Fingerprint.class);

    private static final Pattern SHA256 = Pattern.compile("[A-Fa-f0-9]{64}");

    private final String fingerprint;

    // Constructors
    public Fingerprint(@NotNull String fingerprint) throws InvalidFingerprintException {
        logger.debug("Creating new fingerprint from string '{}'.", fingerprint);

        if (SHA256.matcher(fingerprint).matches()) {
            logger.debug("String {} is a valid fingerprint.", fingerprint);
            this.fingerprint = fingerprint.toUpperCase();
        } else {
            throw new InvalidFingerprintException("'" + fingerprint + "' is not a valid fingerprint.");
        }
    }

    // Public methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fingerprint that = (Fingerprint) o;
        return fingerprint.equals(that.fingerprint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fingerprint);
    }

    @Override
    public String toString() {
        return this.fingerprint;
    }
}
