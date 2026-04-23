package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory data store for the Smart Campus System.
 *
 * Uses ConcurrentHashMap instead of HashMap for thread safety.
 * JAX-RS containers can process requests concurrently, so a plain HashMap
 * would cause ConcurrentModificationException under parallel load.
 * ConcurrentHashMap uses bucket-level locking, allowing concurrent reads
 * while serialising writes on separate segments.
 *
 * IDs are String UUIDs (e.g., "a1b2-c3d4-...") rather than integers,
 * as specified in the coursework POJO models.
 */
public class DataStore {

    // ─── Room storage ─────────────────────────────────────────────────────────
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // ─── Sensor storage ───────────────────────────────────────────────────────
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // ─── SensorReading storage ────────────────────────────────────────────────
    private static final Map<String, SensorReading> readings = new ConcurrentHashMap<>();

    // Private constructor — utility class
    private DataStore() {}

    // ─── Accessors ────────────────────────────────────────────────────────────

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Map<String, SensorReading> getReadings() {
        return readings;
    }
}
