package com.smartcampus.exception;

/**
 * Thrown when a Sensor or SensorReading references a resource (Room or Sensor)
 * that does not exist in the data store.
 *
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundMapper.
 *
 * Why 422 instead of 404?
 *   HTTP 404 means the REQUEST URI itself was not found.
 *   HTTP 422 means the URI was valid and the server understood the body,
 *   but the body references a resource (roomId) that does not exist.
 *   The entity cannot be processed because of a broken foreign-key reference.
 *   This is the semantically correct choice (rubric 5.1 report requirement).
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
