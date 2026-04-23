package com.smartcampus.exception.mapper;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Global Safety Net — Part 5, Section 5.2 (10 marks)
 *
 * Catch-all ExceptionMapper for ANY Throwable not handled by a specific mapper.
 * Returns HTTP 500 Internal Server Error.
 *
 * CRITICAL: The response body MUST NOT expose stack traces.
 *
 * Cybersecurity rationale (rubric 5.2 report requirement):
 *   Stack traces expose:
 *     1. Internal file paths   → attacker maps the server directory structure
 *     2. Library versions      → attacker finds known CVEs for those versions
 *     3. Class names & logic   → attacker understands internal flow and attack surface
 *     4. Database queries      → SQL injection points (if applicable)
 *   By catching Throwable and returning a generic 500 response, we follow
 *   the principle of "fail securely" — the server acknowledges the error
 *   without disclosing implementation details.
 *
 * The actual exception is logged server-side (SEVERE level) for debugging,
 * but NEVER returned to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log full details server-side — NEVER exposed to the client
        LOGGER.severe("[500] Unhandled exception: " + ex.getClass().getName()
                + " — " + ex.getMessage());

        // Return a generic, safe error response with NO internal details
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(500,
                    "An unexpected server error occurred. "
                    + "Please contact the administrator."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
