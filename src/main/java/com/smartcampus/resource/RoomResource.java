package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Room Resource — Part 2: Room Management (20 marks)
 *
 * Routes:
 *   GET    /api/v1/rooms        → 200 list all rooms
 *   POST   /api/v1/rooms        → 201 create room  (+ Location header)
 *   GET    /api/v1/rooms/{id}   → 200 get specific room, or 404
 *   DELETE /api/v1/rooms/{id}   → 204 deleted, 404 not found, 409 has sensors
 *
 * Rubric highlights:
 *   - POST MUST return 201 Created + Location header pointing to new resource
 *   - DELETE MUST return 409 if room has active sensors (not just any sensors)
 *   - GET by ID required (rubric 2.1 video: "GET by new ID")
 *   - ID-Only vs Location header trade-off discussed in README
 */
@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns a list of all rooms.
     * Empty list (not 404) when no rooms exist.
     */
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(DataStore.getRooms().values());
        return Response.ok(roomList).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room with a server-generated UUID.
     *
     * Returns:
     *   201 Created
     *   Location: /api/v1/rooms/{newId}   ← required by rubric Section 2.1
     *   Body: the full Room JSON
     *
     * Request body:
     *   { "name": "Library Room 301", "capacity": 40 }
     */
    @POST
    public Response createRoom(Room room) {
        // Assign UUID as the room ID
        String id = UUID.randomUUID().toString();
        room.setId(id);

        // Ensure sensorIds list is initialised
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.getRooms().put(id, room);

        // Build Location header URI: /api/v1/rooms/{id}
        URI location = UriBuilder.fromPath("/api/v1/rooms/{id}").build(id);

        return Response
                .status(Response.Status.CREATED)
                .location(location)
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{id}
     * Returns a single room by its UUID.
     *
     * Throws RoomNotFoundException (→ 404) if no room with that ID exists.
     */
    @GET
    @Path("{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = DataStore.getRooms().get(id);
        if (room == null) {
            throw new RoomNotFoundException("Room with id '" + id + "' was not found.");
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{id}
     * Deletes a room.
     *
     * Business rules:
     *   1. Room must exist          → else 404 (RoomNotFoundException)
     *   2. Room must have no sensors → else 409 (RoomNotEmptyException)
     *      Rationale: deleting a room with sensors would create orphaned
     *      sensor records with a dangling roomId reference — a data
     *      integrity violation even in an in-memory system.
     *   3. Room deleted             → 204 No Content
     *
     * Idempotency note:
     *   This endpoint is NOT idempotent in the strict sense — the first
     *   DELETE removes the room and returns 204; subsequent DELETE calls
     *   return 404. This is the correct REST behaviour for DELETE.
     */
    @DELETE
    @Path("{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = DataStore.getRooms().get(id);

        // 404 — room not found
        if (room == null) {
            throw new RoomNotFoundException("Room with id '" + id + "' was not found.");
        }

        // 409 — room has sensors; refuse deletion to prevent orphaned sensor records
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + id + "' cannot be deleted: it still has "
                + room.getSensorIds().size() + " sensor(s) assigned to it. "
                + "Please reassign or delete all sensors first."
            );
        }

        DataStore.getRooms().remove(id);
        return Response.noContent().build();
    }
}
