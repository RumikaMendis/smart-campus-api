package com.smartcampus.exception;

/**
 * Thrown when a requested Sensor ID does not exist in the data store.
 *
 * Mapped to HTTP 404 Not Found by SensorNotFoundMapper.
 *
 * Triggered by:
 *   - GET  /api/v1/sensors/{id}/readings  when sensorId does not exist
 *   - POST /api/v1/sensors/{id}/readings  when sensorId does not exist
 */
public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String message) {
        super(message);
    }
}
