package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden.
 *
 * Triggered when POST /sensors/{id}/readings targets a MAINTENANCE sensor.
 *
 * Why 403 Forbidden?
 *   403 indicates the server understood the request but refuses to fulfil it.
 *   MAINTENANCE is a business-level access restriction:
 *   - The sensor exists (not 404)
 *   - The request syntax is valid (not 400 or 422)
 *   - But the server policy for MAINTENANCE sensors denies new readings
 *   This maps precisely to 403: request understood, permission denied by policy.
 */
@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {

    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        LOGGER.warning("[403] SensorUnavailableException: " + ex.getMessage());
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(403, ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
