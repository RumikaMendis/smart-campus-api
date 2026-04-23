package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Room — Smart Campus POJO model.
 *
 * Represents a physical room in the campus building.
 * Stores a list of sensor IDs (Strings) rather than full Sensor objects
 * to avoid circular references and reduce payload size.
 *
 * ID Strategy: UUID string (e.g., "550e8400-e29b-41d4-a716-446655440000")
 * assigned on creation.
 */
public class Room {

    /** Unique room identifier — UUID string */
    private String id;

    /** Human-readable room name, e.g. "Library Room 301" */
    private String name;

    /** Maximum number of occupants */
    private int capacity;

    /** IDs of all sensors currently installed in this room */
    private List<String> sensorIds;

    // ─── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required by Jackson for JSON deserialisation */
    public Room() {
        this.sensorIds = new ArrayList<>();
    }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = new ArrayList<>();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }

    @Override
    public String toString() {
        return "Room{id='" + id + "', name='" + name + "', capacity=" + capacity
               + ", sensorIds=" + sensorIds + "}";
    }
}
