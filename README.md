# Smart Campus System — REST API

**Module:** 5COSC022W — Component and Service-Based Architectures  
**Technology:** Java 11 | JAX-RS (Jersey 2.41) | Grizzly2 | Jackson | Maven

> ✅ No Spring Boot &nbsp;|&nbsp; ✅ No Database &nbsp;|&nbsp; ✅ Pure in-memory ConcurrentHashMap storage

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [How to Build & Run](#how-to-build--run)
3. [API Reference & curl Examples](#api-reference--curl-examples)
4. [Error Responses](#error-responses)
5. [Report Questions & Answers](#report-questions--answers)

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                             ← Grizzly server entry point
    ├── config/
    │   └── AppConfig.java                    ← @ApplicationPath("/api/v1") + ResourceConfig
    ├── model/
    │   ├── Room.java                         ← id(String), name, capacity, sensorIds
    │   ├── Sensor.java                       ← id(String), type, status, currentValue, roomId
    │   ├── SensorReading.java                ← id(String), timestamp(long), value, sensorId
    │   └── ErrorResponse.java                ← { status, error }
    ├── store/
    │   └── DataStore.java                    ← ConcurrentHashMap<String, T> storage
    ├── resource/
    │   ├── DiscoveryResource.java            ← GET /api/v1
    │   ├── RoomResource.java                 ← /api/v1/rooms  (GET, POST, GET/{id}, DELETE/{id})
    │   ├── SensorResource.java               ← /api/v1/sensors + sub-resource locator
    │   └── SensorReadingResource.java        ← sub-resource: /sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotFoundException.java        ← → 404
    │   ├── RoomNotEmptyException.java        ← → 409
    │   ├── SensorNotFoundException.java      ← → 404
    │   ├── LinkedResourceNotFoundException.java  ← → 422
    │   ├── SensorUnavailableException.java   ← → 403
    │   └── mapper/
    │       ├── RoomNotFoundMapper.java
    │       ├── RoomNotEmptyMapper.java
    │       ├── SensorNotFoundMapper.java
    │       ├── LinkedResourceNotFoundMapper.java
    │       ├── SensorUnavailableMapper.java
    │       └── GlobalExceptionMapper.java    ← Throwable → 500
    └── filter/
        └── LoggingFilter.java                ← Request/response logger
```

---

## How to Build & Run

### Prerequisites
- Java 11+ (`java -version`)
- Maven Wrapper included (`./mvnw`) — no separate Maven install needed

### Build
```bash
cd smart-campus-api
./mvnw clean package
# Produces: target/smart-campus-api-1.0.0.jar
```

### Run
```bash
java -jar target/smart-campus-api-1.0.0.jar
```
**Base URL:** `http://localhost:8080/api/v1`

Stop with **Ctrl+C**.

---

## API Reference & curl Examples

### Base URL: `http://localhost:8080/api/v1`

---

### 1. Discovery Endpoint

```bash
GET /api/v1
```

```bash
curl -X GET http://localhost:8080/api/v1
```

**Response 200:**
```json
{
  "version": "v1",
  "description": "Smart Campus System REST API — 5COSC022W Coursework",
  "baseUrl": "/api/v1",
  "resources": {
    "rooms":   "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  },
  "contact": {
    "name":  "Smart Campus Admin",
    "email": "admin@smartcampus.ac.uk"
  }
}
```

---

### 2. Rooms

#### Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Room 301", "capacity": 40}'
```
**Response 201 Created** _(+ Location header)_:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Library Room 301",
  "capacity": 40,
  "sensorIds": []
}
```
Headers:
```
Location: /api/v1/rooms/550e8400-e29b-41d4-a716-446655440000
```

#### Get All Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```
**Response 200:**
```json
[
  { "id": "550e8400-...", "name": "Library Room 301", "capacity": 40, "sensorIds": ["abc-123"] }
]
```

#### Get Room by ID
```bash
curl -X GET http://localhost:8080/api/v1/rooms/550e8400-e29b-41d4-a716-446655440000
```
**Response 200** — room JSON, or **404** if not found.

#### Delete a Room (success — no sensors)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/550e8400-e29b-41d4-a716-446655440000
```
**Response 204** No Content

#### Delete a Room (fails — has sensors → 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/550e8400-e29b-41d4-a716-446655440000
```
**Response 409 Conflict:**
```json
{
  "status": 409,
  "error": "Room '550e8400-...' cannot be deleted: it still has 1 sensor(s) assigned to it."
}
```

---

### 3. Sensors

#### Create a Sensor (valid room)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "550e8400-e29b-41d4-a716-446655440000"}'
```
**Response 201 Created:**
```json
{
  "id": "abc12345-0000-0000-0000-000000000001",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Create a Sensor (bad roomId → 422)
```bash
curl -sw "\nHTTP: %{http_code}" -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "roomId": "does-not-exist"}'
```
**Response 422 Unprocessable Entity:**
```json
{
  "status": 422,
  "error": "Cannot register sensor: Room with id 'does-not-exist' does not exist."
}
```

#### Get All Sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

#### Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```
**Response 200** — filtered array containing only CO2 sensors.

---

### 4. Sensor Readings (Sub-Resource)

#### Add a Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/abc12345-0000-0000-0000-000000000001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.5}'
```
**Response 201 Created:**
```json
{
  "id": "read-uuid-here",
  "sensorId": "abc12345-0000-0000-0000-000000000001",
  "value": 450.5,
  "timestamp": 1745389800000
}
```
> ⚡ **Side effect:** `sensor.currentValue` is updated to `450.5`

#### Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/abc12345-0000-0000-0000-000000000001/readings
```

#### Add Reading to MAINTENANCE Sensor (→ 403)
```bash
# First create a MAINTENANCE sensor:
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Motion", "status": "MAINTENANCE", "roomId": "<valid-room-id>"}'

# Then try to post a reading:
curl -sw "\nHTTP: %{http_code}" \
  -X POST http://localhost:8080/api/v1/sensors/<sensor-id>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 1.0}'
```
**Response 403 Forbidden:**
```json
{
  "status": 403,
  "error": "Sensor '<id>' is currently under MAINTENANCE. New readings cannot be recorded."
}
```

#### Trigger a 500 Error (for testing global mapper)
```bash
# Send malformed numeric type on ID — Jersey will fail internally
curl -X GET http://localhost:8080/api/v1/rooms/null/sensors/null/readings
```
**Response 500:**
```json
{
  "status": 500,
  "error": "An unexpected server error occurred. Please contact the administrator."
}
```
> ✅ No stack trace visible — safe response

---

## Error Responses

All errors return consistent JSON:
```json
{ "status": <code>, "error": "<message>" }
```

| Code | Meaning | Trigger |
|------|---------|---------|
| 404 | Not Found | Room or Sensor ID does not exist |
| 409 | Conflict | Deleting a room with sensors attached |
| 422 | Unprocessable Entity | Sensor created with non-existent roomId |
| 403 | Forbidden | Posting reading to a MAINTENANCE sensor |
| 500 | Internal Server Error | Unhandled exception (no stack trace) |

---

## Report Questions & Answers

---

### Q1: What is the role of `@ApplicationPath` in JAX-RS, and how does it interact with Jersey's ResourceConfig?

`@ApplicationPath` is basically what sets the base URL for the whole REST application. I placed `@ApplicationPath("/api/v1")` on my `AppConfig` class so that all endpoints are prefixed with `/api/v1` — for example a resource with `@Path("rooms")` becomes `/api/v1/rooms`.

`ResourceConfig` is Jersey's own version of the JAX-RS `Application` class. I chose to extend `ResourceConfig` instead of `Application` because it gives you extra Jersey-specific features like `packages()` for auto-scanning your classes, and `register()` for adding things like `JacksonFeature` for JSON support.

The way it works in this project — `AppConfig` carries the `@ApplicationPath` annotation and extends `ResourceConfig`. The Grizzly server is started with the full URI `http://0.0.0.0:8080/api/v1/` passed directly in `Main.java`, so Jersey picks up the path from there.

One thing about the JAX-RS lifecycle is that resource classes (those with `@Path`) are request-scoped by default, meaning a new instance gets created for each request. That is fine here because the actual data lives in static `ConcurrentHashMap` fields in `DataStore`, not in the resource instance itself. So all requests share the same data even though each request gets a fresh resource object.

---

### Q2: What is HATEOAS and why is a Discovery Endpoint valuable?

HATEOAS stands for Hypermedia As The Engine Of Application State. The basic idea is that API responses include links to related resources so clients know where to go next without needing to hardcode URLs.

My discovery endpoint `GET /api/v1` returns something like:
```json
{
  "resources": {
    "rooms":   "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

The main benefit I see is that it makes the API self-documenting — a new developer can hit the root URL and immediately see what resources are available. It also means if a URL ever changes, clients following the links would adapt rather than breaking. Static documentation like a README can go out of date but a live endpoint always reflects what is actually there.

---

### Q3: Explain the HTTP status codes used in this API and justify each choice.

| Code | Used When | Why |
|------|-----------|-----|
| 200 OK | Successful GET | Standard success for reads |
| 201 Created | Successful POST | Shows something was actually created |
| 204 No Content | Successful DELETE | No body needed |
| 404 Not Found | Room/Sensor ID not in store | Requested resource does not exist |
| 409 Conflict | Delete room that has sensors | Valid request but clashes with current data state |
| 422 Unprocessable Entity | Sensor with non-existent roomId | JSON is fine but the content is semantically wrong |
| 403 Forbidden | Reading to MAINTENANCE sensor | Business rule is blocking it |
| 500 Server Error | Unexpected crash | Catch-all so no stack trace leaks |

A few important distinctions worth mentioning — 404 vs 422 was something I had to think about. 404 should mean the URL itself does not exist, not that something inside the request body is wrong. So when someone creates a sensor with a bad roomId, the `/sensors` URL clearly does exist, it is the roomId in the body that is the problem, which is why 422 is more appropriate.

Similarly 409 vs 400 — 400 would mean the request is malformed, but the delete request for a room is perfectly valid, it just conflicts with the fact that the room still has sensors. That is a 409.

---

### Q4: What is the purpose of ExceptionMappers in JAX-RS, and how does the Global mapper improve API security?

`ExceptionMapper<T>` is a JAX-RS interface marked with `@Provider` that lets you intercept a specific exception and turn it into a proper HTTP response. So when `RoomNotFoundException` gets thrown, instead of Jersey returning an ugly 500 HTML error, my `RoomNotFoundMapper` catches it and returns a clean 404 JSON.

Without these mappers, any uncaught exception would cause Jersey to dump a full Java stack trace in the response body. That is a security problem because:
- It shows internal package names and class paths
- It reveals which library versions are being used (e.g. jersey-server-2.41) which attackers can look up for known vulnerabilities
- It shows method call chains that expose business logic

My `GlobalExceptionMapper<Throwable>` covers anything not caught by the specific mappers. It returns a generic `{"status": 500, "error": "unexpected server error"}` and logs the real exception server-side only. So no internal details ever reach the client.

---

### Q5: How does ConcurrentHashMap differ from HashMap, and why is it important in a JAX-RS application?

A regular `HashMap` is not thread-safe. JAX-RS servers handle multiple requests at the same time using a thread pool, so if two requests both try to write to a `HashMap` at the same time you will get a `ConcurrentModificationException` or even silent data corruption.

`ConcurrentHashMap` handles this by locking at the bucket/segment level rather than the whole map. This means different threads can read and write to different buckets simultaneously, which is much better for performance than putting a `synchronized` block around everything.

In `DataStore`, all three maps (rooms, sensors, readings) use `ConcurrentHashMap`. I also noted that `UUID.randomUUID()` is thread-safe by the Java spec so there is no need for extra synchronisation around ID generation.

---

### Q6: What is the Sub-Resource Locator pattern in JAX-RS and why is it architecturally superior?

A sub-resource locator is a method that instead of directly producing a response, returns another resource class for JAX-RS to dispatch the request to. The important thing is it has no HTTP method annotation like `@GET` or `@POST`.

In my project `SensorResource` has this method:
```java
@Path("{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

So a request to `/sensors/abc123/readings` hits `SensorResource` first, which calls this locator method and gets back a `SensorReadingResource` instance. Jersey then dispatches the actual `GET` or `POST` to that class.

The main advantage is that it keeps things separated — sensor logic in one class, readings logic in another. If I had put everything in one resource class it would get very messy. I also do the sensor existence check in the locator method so by the time the actual handler runs the sensor is already validated.

---

### Q7: Explain QueryParam vs PathParam. Why is `?type=CO2` better than `/sensors/CO2`?

`@PathParam` is for values that are part of the URL path and identify a specific resource — like `/rooms/{id}` where id is the room being identified. `@QueryParam` is for optional filters that come after the `?` in the URL.

For sensor type filtering I went with `@QueryParam("type")` so the URL is `GET /sensors?type=CO2`. The other option would have been a path param like `GET /sensors/CO2`.

The reason query param makes more sense here is that `/sensors` already represents the whole sensors collection — that is the resource. The type is just a filter on that collection, not a sub-resource. If I used `/sensors/CO2` it would suggest CO2 is a child resource of sensors which is semantically wrong.

Also query params are optional by default which is exactly what I needed — no type param means return all sensors, with type param means return the filtered set.

---

### Q8: What happens when a client sends the wrong Content-Type to a JAX-RS endpoint?

If a client sends `Content-Type: text/plain` to an endpoint that has `@Consumes(MediaType.APPLICATION_JSON)`, Jersey rejects it automatically with `415 Unsupported Media Type` before the resource method even gets called. No custom code needed.

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: text/plain" \
  -d '{"name":"Lab A","capacity":30}'
# → 415 Unsupported Media Type
```

Similarly if the client sends `Accept: text/html` but the endpoint only produces JSON, Jersey returns `406 Not Acceptable`.

This is actually one of the things I found useful about JAX-RS — content negotiation is all handled by the framework, you do not have to write any code for it yourself.

---

### Q9: Why is `422 Unprocessable Entity` the correct response when a sensor references a non-existent room, rather than `404 Not Found`?

The distinction comes down to what part of the request is wrong.

404 means the URL itself does not exist on the server. But when a client POSTs to `/api/v1/sensors` with a non-existent roomId, the `/api/v1/sensors` URL clearly exists — it works fine for valid requests. So 404 would be the wrong signal, the client might think they have the wrong URL and start debugging in the wrong place.

422 means I understood your request and the JSON is valid, but the content cannot be processed because of a semantic problem in the body. In this case the roomId is referencing something that does not exist in the system, which is a data validation issue not a URL issue.

So 422 tells the client: your endpoint is right, go fix the roomId in your request body.

---

### Q10: What is idempotency in REST, and which HTTP methods in this API are idempotent?

Idempotency means calling the same request multiple times with the same input gives the same result as calling it once — the server state does not keep changing.

- `GET` is idempotent — it only reads, nothing changes
- `DELETE` is effectively idempotent — first call deletes the room and returns 204, second call returns 404 because it is already gone. The server state is the same (room absent) either way
- `POST` is not idempotent — every `POST /rooms` creates a brand new room with a new UUID, so calling it twice gives you two rooms

This is mainly important in unreliable networks where a client might retry a request if it did not get a response. Retrying a DELETE is safe. Retrying a POST by accident would create duplicate data.

---

*Smart Campus System REST API — 5COSC022W Coursework*
*Stack: Java 11 | JAX-RS (Jersey 2.41) | Grizzly2 HTTP Server | Jackson JSON | Apache Maven*
