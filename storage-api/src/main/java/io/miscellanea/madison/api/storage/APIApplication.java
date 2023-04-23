package io.miscellanea.madison.api.storage;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * A JAX-RS <code>Application</code> subclass that sets the base
 * path for all endpoints to </code>/api</code>.
 */
@ApplicationPath("/api")
public class APIApplication extends Application {
}
