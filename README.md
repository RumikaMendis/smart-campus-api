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

`@ApplicationPath` is a JAX-RS annotation that sets the base URI segment for the entire REST application. Placing `@ApplicationPath("/api/v1")` on the `AppConfig` class (which extends `ResourceConfig`) instructs the JAX-RS runtime that all resource paths are prefixed with `/api/v1`. For example, a resource annotated `@Path("rooms")` becomes accessible at `/api/v1/rooms`.

`ResourceConfig` is Jersey's concrete implementation of the abstract `Application` class defined in the JAX-RS specification. By extending `ResourceConfig` rather than `Application`, we gain Jersey-specific utilities such as `packages()` (classpath scanning), `register()` (manual component registration), and the ability to add features like `JacksonFeature`.

In this project, `AppConfig` combines both: it carries `@ApplicationPath("/api/v1")` for URI routing and extends `ResourceConfig` for Jersey-specific configuration. The Grizzly embedded server is started with the root URI `http://0.0.0.0:8080/`, and Jersey automatically applies the `/api/v1` prefix from the annotation.

**JAX-RS Lifecycle (Request-Scoped vs Singleton):**
- By default, JAX-RS resource classes (annotated with `@Path`) are **request-scoped** — a new instance is created for every HTTP request. This is safe for our use case since data is stored in static `ConcurrentHashMap` rather than as instance fields.
- The `DataStore` class uses `static` maps, effectively making it a **Singleton** — all request-scoped instances share the same data store. This is the correct pattern for in-memory storage without a database.
- For thread safety, `ConcurrentHashMap` is used instead of `HashMap` — this allows concurrent reads from multiple request threads while serialising writes at the bucket level.

---

### Q2: What is HATEOAS and why is a Discovery Endpoint valuable?

HATEOAS (Hypermedia As The Engine Of Application State) is the highest level of REST maturity (Richardson Maturity Model Level 3). In a HATEOAS-compliant API, responses include hyperlinks to related resources, allowing clients to navigate the API without hardcoded URLs.

The discovery endpoint `GET /api/v1` returns a resource map:
```json
{
  "resources": {
    "rooms":   "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

**Benefits over static documentation:**
1. **Self-documenting** — clients can discover available endpoints at runtime
2. **Decoupling** — if a URL changes, clients that follow links automatically adapt
3. **Onboarding** — new developers can explore the API from a single entry point
4. **Versioning clarity** — the version (`"v1"`) is explicit in the response, not just the URL

In contrast, static documentation (README, Swagger files) goes stale and requires manual updates. A live discovery endpoint always reflects the current API state.

---

### Q3: Explain the HTTP status codes used in this API and justify each choice.

| Code | Name | Used When | Justification |
|------|------|-----------|---------------|
| 200 | OK | Successful GET | Standard success response for reads |
| 201 | Created | Successful POST | Distinguishes creation from retrieval |
| 204 | No Content | Successful DELETE | No body needed; action completed |
| 404 | Not Found | Room/Sensor ID missing | The requested URI resource does not exist |
| 409 | Conflict | Delete room with sensors | Request valid, but conflicts with server state |
| 422 | Unprocessable Entity | Sensor with bad roomId | Body valid, but semantic reference invalid |
| 403 | Forbidden | Post to MAINTENANCE sensor | Access denied by business policy |
| 500 | Internal Server Error | Unexpected crash | Catch-all; hides internal details |

**Key distinctions:**
- **404 vs 422:** 404 means the *endpoint URL* doesn't exist. 422 means the URL is valid, but the *body payload references a non-existent resource* (roomId). Using 422 communicates to the client that the problem is in the request body, not the URL.
- **409 vs 400:** 400 means the request is malformed. 409 means it's a valid request that conflicts with current server state — the room exists, it's just not empty.
- **403 vs 401:** 401 means unauthenticated. 403 means authenticated but denied. MAINTENANCE is a business rule restriction — the sensor exists but is restricted by policy.

---

### Q4: What is the purpose of ExceptionMappers in JAX-RS, and how does the Global mapper improve API security?

`ExceptionMapper<T>` is a JAX-RS `@Provider` interface that maps a specific exception type to an HTTP `Response`. When a resource method throws `RoomNotFoundException`, JAX-RS catches it and delegates to `RoomNotFoundMapper`, which constructs a proper 404 JSON response.

**Without ExceptionMappers:**
- Uncaught exceptions would cause Jersey to return 500 with an HTML error page containing a full Java stack trace
- The stack trace exposes: class names, library versions, file paths, line numbers

**Security risks of stack traces (rubric 5.2):**
1. **Path disclosure** — `at com.smartcampus.store.DataStore.getRooms(DataStore.java:34)` reveals the internal package structure
2. **Version fingerprinting** — `jersey-server-2.41.jar` lets attackers look up known CVEs for that version
3. **Logic disclosure** — method call chains reveal business logic and data flow, enabling targeted attacks
4. **Library detection** — Jackson, Grizzly, HK2 versions become visible attack surface metadata

The `GlobalExceptionMapper<Throwable>` acts as a safety net — any exception not caught by a specific mapper is caught here, and a generic `{"status": 500, "error": "unexpected server error"}` is returned. The actual exception is logged server-side only.

---

### Q5: How does ConcurrentHashMap differ from HashMap, and why is it important in a JAX-RS application?

A `HashMap` is not thread-safe. In a JAX-RS server, each incoming HTTP request is handled on a separate thread from a thread pool. If two requests simultaneously attempt to write to a `HashMap` (e.g., two concurrent `POST /rooms`), the result is a `ConcurrentModificationException` or, worse, silent data corruption.

`ConcurrentHashMap` solves this by:
- **Bucket-level locking** — only the affected segment is locked on write; other segments remain accessible for concurrent reads/writes
- **Lock-free reads** — reads do not acquire any lock; they read the latest committed value
- **Atomic operations** — `putIfAbsent`, `computeIfAbsent` etc. are atomic; no external synchronisation needed

In this project's `DataStore`, all three maps (`rooms`, `sensors`, `readings`) use `ConcurrentHashMap` to safely handle concurrent requests without `synchronized` blocks or `AtomicReference` wrappers.

One remaining concurrency consideration: the UUID generation (`UUID.randomUUID()`) is thread-safe by Java specification, so no synchronisation is needed for ID assignment.

---

### Q6: What is the Sub-Resource Locator pattern in JAX-RS and why is it architecturally superior?

A **sub-resource locator** is a resource method that, instead of producing a response directly, returns another resource class instance for further method dispatch. It does NOT have an HTTP method annotation (`@GET`, `@POST`).

In this project:
```java
// In SensorResource.java
@Path("{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    Sensor sensor = DataStore.getSensors().get(sensorId);
    if (sensor == null) throw new SensorNotFoundException(...);
    return new SensorReadingResource(sensor);
}
```

When a request arrives for `/sensors/abc123/readings`, JAX-RS:
1. Matches `/sensors` → `SensorResource`
2. Matches `/{sensorId}/readings` → calls `getReadingResource("abc123")`
3. Receives `SensorReadingResource` instance
4. Dispatches `GET` or `POST` to the matching method in that class

**Architectural benefits:**
1. **Single Responsibility** — `SensorReadingResource` handles only reading logic; `SensorResource` handles only sensor logic
2. **Delegation** — the locator acts as a factory, creating a scoped resource instance
3. **Testability** — `SensorReadingResource` can be unit tested in isolation by injecting a mock `Sensor`
4. **Scalability** — in large APIs, sub-resource locators allow splitting feature teams across separate classes without URL conflicts

---

### Q7: Explain QueryParam vs PathParam. Why is `?type=CO2` better than `/sensors/CO2`?

| Feature | `@PathParam` | `@QueryParam` |
|---------|-------------|---------------|
| Position | Part of the URI path | After `?` in the URI |
| Example | `/sensors/CO2` | `/sensors?type=CO2` |
| Resource identity | Changes the resource | Does NOT change the resource |
| Optional? | No (required) | Yes (optional) |
| REST semantics | Identifies a resource | Filters/modifies a representation |

**Why QueryParam is correct for sensor type filtering:**

1. **Resource identity:** `/sensors` is the collection of ALL sensors. Using `/sensors/CO2` would imply that `CO2` is a sub-resource of `sensors` — semantically wrong. The URL structure should reflect resource hierarchy, not filtering criteria.

2. **Optionality:** `@QueryParam` is naturally optional — `GET /sensors` (no filter) returns all sensors; `GET /sensors?type=CO2` returns a subset. `PathParam` cannot be optional without creating a separate `@Path` method.

3. **REST Uniform Interface:** The Uniform Interface constraint requires that resources are identified by URI. `/sensors` identifies the sensors collection. The query string modifies the *representation* of that collection, not the collection itself.

4. **Caching:** In HTTP, query string parameters change the cache key, which is correct — a cached response for `/sensors?type=CO2` should be separate from `/sensors?type=Temperature`. PathParams use the same caching segment structure, which could cause collisions if the path is misinterpreted.

---

### Q8: What happens when a client sends the wrong Content-Type to a JAX-RS endpoint?

If a client sends a request with `Content-Type: text/plain` to an endpoint annotated with `@Consumes(MediaType.APPLICATION_JSON)`, Jersey automatically returns:

```
HTTP/1.1 415 Unsupported Media Type
```

This is handled entirely by the JAX-RS runtime — no custom code is needed. Jersey inspects the `Content-Type` header, compares it to the `@Consumes` annotation, and rejects the request before the resource method is ever invoked.

**Practical demonstration:**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: text/plain" \
  -d '{"name":"Lab A","capacity":30}'
# → 415 Unsupported Media Type
```

**Why this matters:**
- Prevents Jackson from attempting to parse non-JSON content (which would throw a `JsonProcessingException`)
- Provides a clear, standards-compliant error message to the client
- No performance cost from attempting to deserialise invalid content

If the `Accept` header is wrong (client asks for `Accept: text/html` but the resource only `@Produces(APPLICATION_JSON)`), Jersey returns `406 Not Acceptable`.

---

### Q9: Why is `422 Unprocessable Entity` the correct response when a sensor references a non-existent room, rather than `404 Not Found`?

This is a critical REST semantics distinction:

**HTTP 404 Not Found** means: "The URI you requested (`/api/v1/sensors`) does not exist on this server." The URI itself is the problem.

**HTTP 422 Unprocessable Entity** means: "The URI you requested exists and I understood your JSON body, but I cannot process this request because the body contains a semantic error." The payload is the problem.

In our case:
- Client `POST`s to `/api/v1/sensors` — this URI **exists** (200s on GET)
- The JSON body is **syntactically valid** — Jackson parses it without error
- But `roomId: "does-not-exist"` is a **semantic error** — the referenced entity doesn't exist

Returning 404 would be misleading — the client might think the `/sensors` endpoint itself doesn't exist and stop trying. Returning 422 clearly communicates: "your endpoint call is correct, but fix the `roomId` in your request body."

This distinction is defined in RFC 4918 (WebDAV) and adopted by REST best practices: 422 applies when the request is well-formed but contains unprocessable instructions.

---

### Q10: What is idempotency in REST, and which HTTP methods in this API are idempotent?

An HTTP method is **idempotent** if calling it multiple times with the same input produces the same server state as calling it once.

| Method | Idempotent? | Reason |
|--------|-------------|--------|
| GET | ✅ Yes | Read-only; no state change |
| DELETE | ✅ Yes* | Deletes resource; subsequent calls return 404 (state unchanged after first) |
| POST | ❌ No | Each call creates a new resource with a new UUID |
| PUT | ✅ Yes | Full replacement; same input produces same state |

**In this project:**

- `GET /rooms` — idempotent ✅ (always reads current state)
- `DELETE /rooms/{id}` — effectively idempotent† ✅  
  - First call: deletes room → 204
  - Second call: room gone → 404
  - Server state is the same (room absent) after both calls; client gets different response codes but this is acceptable per RFC 7231
- `POST /rooms` — NOT idempotent ❌  
  - Each call creates a new room with a new UUID and increments the collection

**Why idempotency matters:**
In unreliable networks, clients may retry requests when they don't receive a response. If a `DELETE` is retried and the resource is already gone (404), the client can safely treat this as success — the desired state (resource deleted) has been achieved. If `POST` were idempotent, retrying it would not create duplicates — but standard `POST` is not, so clients must be careful with POST retries.

---

*Smart Campus System REST API — 5COSC022W Coursework*  
*Stack: Java 11 | JAX-RS (Jersey 2.41) | Grizzly2 HTTP Server | Jackson JSON | Apache Maven*
