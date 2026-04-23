package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Request/Response Logging Filter — Part 5 (Logging requirement)
 *
 * Automatically wraps every API call with structured log output.
 * Implements both ContainerRequestFilter (before processing) and
 * ContainerResponseFilter (after processing).
 *
 * Log format:
 *   [REQUEST]  GET    http://localhost:8080/api/v1/rooms
 *   [RESPONSE] 200   GET    http://localhost:8080/api/v1/rooms
 *
 * Implementation notes:
 *   - Uses java.util.logging (JUL) — no extra dependency
 *   - @Provider annotation ensures Jersey auto-discovers this filter
 *     via the packages() scan in AppConfig
 *   - Both filter methods are required (two interfaces)
 *
 * JAX-RS lifecycle for this filter:
 *   Request → LoggingFilter.filter(request) → Resource Method
 *            → LoggingFilter.filter(request, response) → Client
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Invoked BEFORE the resource method.
     * Logs HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        LOGGER.info(String.format("[REQUEST]  %-6s %s",
                req.getMethod(),
                req.getUriInfo().getRequestUri()));
    }

    /**
     * Invoked AFTER the resource method has produced a response.
     * Logs HTTP status code, method, and URI.
     */
    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {
        LOGGER.info(String.format("[RESPONSE] %d    %-6s %s",
                res.getStatus(),
                req.getMethod(),
                req.getUriInfo().getRequestUri()));
    }
}
