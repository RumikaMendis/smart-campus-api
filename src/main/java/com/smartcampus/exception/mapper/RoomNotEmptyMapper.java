package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Maps RoomNotEmptyException → HTTP 409 Conflict.
 *
 * Triggered when attempting to DELETE a room that still has sensors.
 *
 * Why 409 Conflict?
 *   The DELETE request is syntactically and semantically valid — the room
 *   exists and the client has permission to delete it. However, the current
 *   STATE of the server conflicts with the request: active sensor records
 *   would be orphaned if the room were deleted. 409 correctly communicates
 *   "your request is valid but conflicts with current state."
 *
 *   Client resolution: delete or reassign all sensors in the room first.
 */
@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {

    private static final Logger LOGGER = Logger.getLogger(RoomNotEmptyMapper.class.getName());

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        LOGGER.warning("[409] RoomNotEmptyException: " + ex.getMessage());
        return Response
                .status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(409, ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
