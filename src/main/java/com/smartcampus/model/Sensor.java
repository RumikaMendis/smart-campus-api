package com.smartcampus.model;

/**
 * Sensor — Smart Campus POJO model.
 *
 * Represents a physical sensor installed in a room.
 *
 * Status values (as per spec):
 *   ACTIVE       — sensor is operating normally
 *   MAINTENANCE  — sensor is under maintenance; readings are blocked (403)
 *   OFFLINE      — sensor is not transmitting
 *
 * ID Strategy: UUID string assigned on creation.
 */
public class Sensor {

    /** Unique sensor identifier — UUID string */
    private String id;

    /** Sensor type, e.g. "CO2", "Temperature", "Humidity", "Motion" */
    private String type;

    /**
     * Operational status: ACTIVE | MAINTENANCE | OFFLINE
     * MAINTENANCE triggers HTTP 403 when a new reading is attempted.
     */
    private String status;

    /** Last recorded sensor reading value; updated on each POST to /readings */
    private double currentValue;

    /** ID of the room this sensor is installed in */
    private String roomId;

    // ─── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required by Jackson */
    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    @Override
    public String toString() {
        return "Sensor{id='" + id + "', type='" + type + "', status='" + status
               + "', currentValue=" + currentValue + ", roomId='" + roomId + "'}";
    }
}
