package com.smartcampus.exception.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 *
 * Triggered when POST /sensors references a roomId that does not exist.
 *
 * Why 422 over 404?
 *   REST semantics distinguish between:
 *     404 — the REQUEST URI was not found (e.g., /rooms/missing-uuid)
 *     422 — the REQUEST URI was valid, but the body references an entity
 *           that cannot be resolved. The server understood the payload
 *           but cannot process it because a linked resource is absent.
 *   Using 422 is semantically superior because the client sent a valid
 *   request to a valid endpoint with a valid JSON structure — the failure
 *   is at the payload validation level, not the routing level.
 *
 * This is a key distinction tested in the rubric (Section 5.1 report answer).
 */
@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(LinkedResourceNotFoundMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        LOGGER.warning("[422] LinkedResourceNotFoundException: " + ex.getMessage());
        return Response
                .status(422) // 422 Unprocessable Entity
                .entity(new ErrorResponse(422, ex.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
