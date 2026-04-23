package com.smartcampus.model;

/**
 * SensorReading — Smart Campus POJO model.
 *
 * Represents a single timestamped measurement from a sensor.
 * Stored as a nested sub-resource under /sensors/{sensorId}/readings.
 *
 * Key spec requirements:
 *   - id        : String UUID
 *   - timestamp : long (Unix epoch milliseconds) — NOT an ISO string
 *   - value     : double (the measured reading)
 *
 * Note: sensorId is deliberately NOT a field in the response body,
 * as the reading is always accessed in the context of its parent sensor.
 * However it is stored internally to allow efficient filtering in the
 * in-memory ConcurrentHashMap (DataStore.getReadings()).
 */
public class SensorReading {

    /** Unique reading identifier — UUID string */
    private String id;

    /**
     * Unix epoch timestamp in milliseconds.
     * Using long (primitive) rather than String or Date to:
     *  1. Allow easy sorting and comparison
     *  2. Avoid timezone ambiguity
     *  3. Minimise payload size
     */
    private long timestamp;

    /** The measured value (e.g., 450.5 ppm CO2, 22.3°C temperature) */
    private double value;

    /**
     * Internal sensor reference — used for filtering in the data store.
     * Excluded from JSON output via @JsonIgnore is NOT needed here;
     * it is useful to include for traceability in API responses.
     */
    private String sensorId;

    // ─── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required by Jackson */
    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value, String sensorId) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
        this.sensorId = sensorId;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    @Override
    public String toString() {
        return "SensorReading{id='" + id + "', sensorId='" + sensorId
               + "', value=" + value + ", timestamp=" + timestamp + "}";
    }
}
