package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sensor Reading Resource — Part 4, Sections 4.1 & 4.2 (20 marks)
 *
 * This class is a JAX-RS sub-resource — it is NOT annotated with @Path
 * at class level. Instead, it is returned by the sub-resource locator
 * method in SensorResource:
 *
 *   @Path("{sensorId}/readings")
 *   public SensorReadingResource getReadingResource(...) { ... }
 *
 * JAX-RS resolves GET/POST on /sensors/{sensorId}/readings by calling
 * the locator, receiving an instance of this class, then invoking the
 * matching @GET or @POST method.
 *
 * Rubric 4.2 "Constant Data" requirement:
 *   POST /readings → triggers update of parent Sensor.currentValue
 *   This is the critical "side effect" that earns marks.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    /** The parent sensor — injected by SensorResource sub-resource locator */
    private final Sensor sensor;

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     *
     * Returns all historical readings for this sensor.
     * Readings are filtered from the global DataStore by sensorId.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> readings = DataStore.getReadings().values().stream()
                .filter(r -> sensor.getId().equals(r.getSensorId()))
                .collect(Collectors.toList());

        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     *
     * Records a new reading for this sensor.
     *
     * Validation:
     *   - Sensor status must NOT be "MAINTENANCE" → else 403 (SensorUnavailableException)
     *
     * Side effects (CRITICAL for rubric 4.2):
     *   1. Saves the new SensorReading to DataStore
     *   2. Updates sensor.currentValue to the new reading's value
     *
     * Auto-assigns:
     *   - id        : UUID
     *   - sensorId  : from parent sensor
     *   - timestamp : System.currentTimeMillis() — Unix epoch ms
     *
     * Request body:
     *   { "value": 450.5 }
     */
    @POST
    public Response addReading(SensorReading reading) {
        // Check sensor is not under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensor.getId() + "' is currently under MAINTENANCE. "
                + "New readings cannot be recorded. "
                + "Update sensor status to ACTIVE first."
            );
        }

        // Assign auto-generated fields
        reading.setId(UUID.randomUUID().toString());
        reading.setSensorId(sensor.getId());
        reading.setTimestamp(System.currentTimeMillis()); // Unix epoch milliseconds

        // Persist reading
        DataStore.getReadings().put(reading.getId(), reading);

        // *** CRITICAL SIDE EFFECT (rubric 4.2) ***
        // Update the parent Sensor's currentValue to the latest reading
        sensor.setCurrentValue(reading.getValue());

        return Response
                .status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
