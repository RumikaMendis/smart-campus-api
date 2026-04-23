package com.smartcampus.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 *
 * @ApplicationPath("/api/v1") declares the application base path.
 * When using GrizzlyHttpServerFactory with an explicit URI in Main.java,
 * the URI takes precedence for routing; this annotation serves as documentation
 * and is checked by Jersey's validation layer.
 *
 * Scanning com.smartcampus discovers all @Path, @Provider classes automatically:
 *  - resource/   → DiscoveryResource, RoomResource, SensorResource, SensorReadingResource
 *  - exception/mapper/ → all ExceptionMappers
 *  - filter/     → LoggingFilter
 */
@ApplicationPath("/api/v1")
public class AppConfig extends ResourceConfig {

    public AppConfig() {
        packages("com.smartcampus");
        register(JacksonFeature.class);
    }
}
