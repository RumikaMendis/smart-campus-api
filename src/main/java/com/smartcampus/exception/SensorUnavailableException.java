package com.smartcampus.exception;

/**
 * Thrown when attempting to add a reading to a sensor whose status is MAINTENANCE.
 *
 * Mapped to HTTP 403 Forbidden by SensorUnavailableMapper.
 *
 * Why 403 Forbidden?
 *   The server understands the request completely, but refuses to fulfil it
 *   because the sensor is temporarily in a restricted state (MAINTENANCE).
 *   The client has no permission to post readings to a sensor under maintenance.
 *   This is a business-level access restriction — not an authentication issue.
 *
 * This is distinct from 404 (sensor not found) and 422 (invalid reference).
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
