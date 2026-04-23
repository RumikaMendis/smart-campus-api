package com.smartcampus.exception;

/**
 * Thrown when a requested Room ID does not exist in the data store.
 *
 * Mapped to HTTP 404 Not Found by RoomNotFoundMapper.
 *
 * Triggered by:
 *   - GET  /api/v1/rooms/{id}    when ID does not exist
 *   - DELETE /api/v1/rooms/{id}  when ID does not exist
 */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
