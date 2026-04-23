package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Maps RoomNotFoundException → HTTP 404 Not Found.
 */
@Provider
public class RoomNotFoundMapper implements ExceptionMapper<RoomNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(RoomNotFoundMapper.class.getName());

    @Override
    public Response toResponse(RoomNotFoundException ex) {
        LOGGER.warning("[404] RoomNotFoundException: " + ex.getMessage());
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
