package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorNotFoundException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Maps SensorNotFoundException → HTTP 404 Not Found.
 */
@Provider
public class SensorNotFoundMapper implements ExceptionMapper<SensorNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(SensorNotFoundMapper.class.getName());

    @Override
    public Response toResponse(SensorNotFoundException ex) {
        LOGGER.warning("[404] SensorNotFoundException: " + ex.getMessage());
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
