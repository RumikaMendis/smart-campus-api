package com.smartcampus.exception;

/**
 * Thrown when attempting to DELETE a Room that still has sensors assigned to it.
 *
 * Mapped to HTTP 409 Conflict by RoomNotEmptyMapper.
 *
 * Why 409 Conflict?
 *   The request is valid, but it conflicts with the current state of the resource.
 *   Deleting a room that has active sensors would leave those sensors with a
 *   dangling roomId reference — a data integrity conflict.
 *
 * The client must remove or reassign all sensors before the room can be deleted.
 *
 * Idempotency of DELETE:
 *   A DELETE that returns 409 is NOT idempotent — repeating it with the same
 *   ID will produce the same 409 until sensors are removed, then 204, then 404.
 *   This is correct and expected REST behaviour.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
