# Smart Campus Sensor & Room Management API

**Name:** Degiri Rumika Damsath Mendis Senevirathne  
**Student ID:** 20233014 / w2120605  
**Module:** 5COSC022W - Client-Server Architectures Coursework  
**Technology:** Java 11 | JAX-RS (Jersey 2.41) | Grizzly2 | Jackson | Maven

---

## Project Overview

This project is a RESTful API for a Smart Campus system built as part of the 5COSC022W coursework. It manages three resources — Rooms, Sensors, and Sensor Readings — using Java and JAX-RS (Jersey) on an embedded Grizzly HTTP server. No Spring Boot or external database is used; data is stored in-memory with `ConcurrentHashMap`.

The lifecycle I followed: requirements analysis from the spec, design of the resource hierarchy and exception handling strategy, implementation of models/resources/mappers, and manual testing via Postman to verify all endpoints and error scenarios.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                          ← Grizzly server entry point
    ├── config/AppConfig.java              ← @ApplicationPath("/api/v1") + ResourceConfig
    ├── model/                             ← Room, Sensor, SensorReading, ErrorResponse POJOs
    ├── store/DataStore.java               ← ConcurrentHashMap in-memory storage
    ├── resource/                          ← DiscoveryResource, RoomResource,
    │                                         SensorResource, SensorReadingResource
    ├── exception/                         ← Custom exceptions (404, 409, 422, 403)
    │   └── mapper/                        ← ExceptionMappers + GlobalExceptionMapper
    └── filter/LoggingFilter.java          ← Request/response logging filter
```

---

## How to Build & Run

**Prerequisites:** Java 11+, Maven Wrapper included (`./mvnw`)

```bash
# Build
./mvnw clean package

# Run
java -jar target/smart-campus-api-1.0.0.jar
```

**Base URL:** `http://localhost:8080/api/v1` — Stop with **Ctrl+C**

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery / HATEOAS |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a room |
| GET | `/api/v1/rooms/{id}` | Get room by ID |
| DELETE | `/api/v1/rooms/{id}` | Delete room (→ 409 if sensors exist) |
| GET | `/api/v1/sensors` | List sensors (optional `?type=CO2`) |
| POST | `/api/v1/sensors` | Create sensor (validates roomId → 422) |
| GET | `/api/v1/sensors/{id}` | Get sensor by ID |
| POST | `/api/v1/sensors/{id}/readings` | Add reading (→ 403 if MAINTENANCE) |
| GET | `/api/v1/sensors/{id}/readings` | Get all readings for sensor |

### Key curl Examples

```bash
# Discovery
curl http://localhost:8080/api/v1

# Create Room → returns 201 + UUID id
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lab 301", "capacity": 40}'

# Create Sensor (roomId must exist, else 422)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "<room-uuid>"}'

# Post Reading → also updates sensor currentValue (side effect)
curl -X POST http://localhost:8080/api/v1/sensors/<sensor-uuid>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.5}'

# Filter sensors by type
curl "http://localhost:8080/api/v1/sensors?type=CO2"

# Delete room that has sensors → 409 Conflict
curl -X DELETE http://localhost:8080/api/v1/rooms/<room-uuid>
```

### Sample Responses

**POST /rooms → 201 Created:**
```json
{ "id": "b1278683-6a80-43a9-bb08-b3128401a430", "name": "Lab 301", "capacity": 40, "sensorIds": [] }
```

**POST /sensors (bad roomId) → 422:**
```json
{ "status": 422, "error": "Cannot register sensor: Room with id 'xyz' does not exist." }
```

**DELETE /rooms (has sensors) → 409:**
```json
{ "status": 409, "error": "Room 'b1278683-...' cannot be deleted: it still has 1 sensor(s) assigned." }
```

---

## Error Responses

All errors return: `{ "status": <code>, "error": "<message>" }`

| Code | Trigger |
|------|---------|
| 404 | Room or Sensor ID not found |
| 409 | Deleting a room that still has sensors |
| 422 | Creating sensor with non-existent roomId |
| 403 | Posting reading to a MAINTENANCE sensor |
| 500 | Unexpected error (no stack trace exposed) |

---

## Testing

All endpoints were tested manually using **Postman**. The following scenarios were verified:

| # | Test Scenario | Expected Result | Status |
|---|--------------|-----------------|--------|
| 1 | GET /api/v1 discovery | 200 + JSON metadata with resource links | ✅ Pass |
| 2 | POST /rooms (valid body) | 201 + room JSON + Location header | ✅ Pass |
| 3 | GET /rooms | 200 + list of all rooms | ✅ Pass |
| 4 | GET /rooms/{id} (valid) | 200 + room JSON | ✅ Pass |
| 5 | GET /rooms/{id} (invalid) | 404 + error JSON | ✅ Pass |
| 6 | POST /sensors (valid roomId) | 201 + sensor JSON (currentValue: 0.0) | ✅ Pass |
| 7 | POST /sensors (bad roomId) | 422 + error JSON | ✅ Pass |
| 8 | GET /sensors?type=CO2 | 200 + filtered list | ✅ Pass |
| 9 | POST /sensors/{id}/readings | 201 + reading JSON, sensor currentValue updated | ✅ Pass |
| 10 | POST /sensors/{id}/readings (MAINTENANCE) | 403 + error JSON | ✅ Pass |
| 11 | DELETE /rooms/{id} (no sensors) | 204 No Content | ✅ Pass |
| 12 | DELETE /rooms/{id} (has sensors) | 409 + error JSON | ✅ Pass |

---

## Security Considerations

- **Input Validation:** The API validates incoming JSON. Creating a sensor with a non-existent `roomId` returns 422 rather than silently saving bad data.
- **No Stack Trace Exposure:** The `GlobalExceptionMapper` catches all unhandled exceptions and returns a safe generic error message. Raw stack traces are never sent to the client — they only appear in server logs.
- **Concurrency Safety:** All data is stored in `ConcurrentHashMap` to prevent race conditions under parallel requests.
- **Future Improvement:** The API currently has no authentication. A production version would require JWT or OAuth2 to restrict access to sensitive endpoints like DELETE.

---

## Report Questions & Answers

### Q1: What is the role of `@ApplicationPath` in JAX-RS, and how does it interact with Jersey's ResourceConfig?

`@ApplicationPath("/api/v1")` sets the base URL prefix for all endpoints. I placed this on `AppConfig` which extends `ResourceConfig` — Jersey's version of the JAX-RS `Application` class. `ResourceConfig` lets me call `packages("com.smartcampus")` to auto-scan all resources and providers, and `register(JacksonFeature.class)` for JSON support. Resource classes are request-scoped by default, which is fine here since all data lives in static `ConcurrentHashMap` fields in `DataStore`, not in the resource instance itself.

---

### Q2: What is HATEOAS and why is a Discovery Endpoint valuable?

HATEOAS means API responses include links to related resources so clients can navigate without hardcoding URLs. My `GET /api/v1` returns the available resource paths, making the API self-documenting. If URLs change in future, clients following the links would adapt automatically instead of breaking. Static documentation like a README can go out of date but a live endpoint always reflects what is actually there.

---

### Q3: Explain the HTTP status codes used in this API and justify each choice.

| Code | Used When | Why |
|------|-----------|-----|
| 200 | GET success | Standard read response |
| 201 | POST success | Confirms resource was created |
| 204 | DELETE success | No body needed |
| 404 | ID not found | Resource does not exist |
| 409 | Delete room with sensors | Valid request conflicts with current state |
| 422 | Sensor with bad roomId | JSON valid but semantically wrong |
| 403 | Reading on MAINTENANCE sensor | Business rule blocking access |
| 500 | Unexpected crash | Catch-all — hides internal details |

The key distinction is 404 vs 422 — 404 means the URL itself does not exist, 422 means the URL is fine but the request body has a semantic problem. Similarly 409 not 400 because the delete request is valid, it just conflicts with current data state.

---

### Q4: What is the purpose of ExceptionMappers in JAX-RS, and how does the Global mapper improve API security?

`ExceptionMapper<T>` intercepts a specific exception and returns a proper HTTP response instead of a raw Jersey error. Without it, JAX-RS would return a Java stack trace exposing internal class names, package paths, and library versions — all useful to attackers. My `GlobalExceptionMapper<Throwable>` catches anything not handled by a specific mapper and returns `{"status": 500, "error": "unexpected server error"}`, with the actual exception only logged server-side.

---

### Q5: How does ConcurrentHashMap differ from HashMap, and why is it important in a JAX-RS application?

`HashMap` is not thread-safe — concurrent writes cause `ConcurrentModificationException`. JAX-RS processes requests on multiple threads simultaneously, so this is a real risk. `ConcurrentHashMap` uses bucket-level locking, allowing concurrent reads while only locking the affected segment on writes — safe without needing `synchronized` blocks everywhere.

---

### Q6: What is the Sub-Resource Locator pattern in JAX-RS and why is it architecturally superior?

A sub-resource locator returns another resource class instance instead of directly handling the request — it has no `@GET` or `@POST` annotation. In my project, `SensorResource` has a locator for `/{sensorId}/readings` that returns a `SensorReadingResource` instance, which JAX-RS then dispatches the request to. This keeps sensor and reading logic separated, and I also validate the sensor exists inside the locator before the handler even runs.

---

### Q7: Explain QueryParam vs PathParam. Why is `?type=CO2` better than `/sensors/CO2`?

`@PathParam` identifies a specific resource in the URL path (e.g. `/rooms/{id}`). `@QueryParam` is for optional filters after the `?`. For type filtering, `/sensors?type=CO2` is correct because `/sensors` is the resource — type is just a filter on that collection. Using `/sensors/CO2` would wrongly imply CO2 is a sub-resource. Query params are also optional by default, so `GET /sensors` still returns everything without any filter.

---

### Q8: What happens when a client sends the wrong Content-Type to a JAX-RS endpoint?

Jersey automatically rejects it with `415 Unsupported Media Type` before the resource method is called — no custom code needed. If the client sends the wrong `Accept` header (e.g. asking for HTML when the endpoint only produces JSON), Jersey returns `406 Not Acceptable`. Content negotiation is handled entirely by the framework.

---

### Q9: Why is `422 Unprocessable Entity` the correct response when a sensor references a non-existent room?

`POST /api/v1/sensors` is a valid endpoint — the problem is the `roomId` in the body references something that does not exist. The JSON is syntactically fine, it just has a semantic error. 422 tells the client: your URL is right, fix the content of your request body. Returning 404 would mislead the client into thinking the endpoint itself does not exist, which is wrong.

---

### Q10: What is idempotency in REST, and which HTTP methods in this API are idempotent?

Idempotency means calling the same operation multiple times has the same effect as calling it once. `GET` is idempotent since it only reads. `DELETE` is effectively idempotent — after the first call the room is gone, a second returns 404 but the server state is the same. `POST` is not idempotent — each call creates a new room with a new UUID. This matters for network retries — retrying a DELETE is safe, retrying a POST would create duplicate data.

---

*5COSC022W Coursework — Java 11 | JAX-RS (Jersey 2.41) | Grizzly2 | Jackson | Maven*
