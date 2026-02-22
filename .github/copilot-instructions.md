<!-- .github/copilot-instructions.md -->
# Worker-Locator — Copilot instructions (concise)

Purpose: help an AI coding agent become productive quickly in this Spring Boot + MongoDB backend.

Quick facts
- Java: 21 (see `pom.xml` -> <java.version>21</java.version>)
- Build: Maven (root `pom.xml`). Typical commands: `mvn clean package`, `mvn spring-boot:run`, `mvn test`.
- DB: MongoDB (Spring Data). Set `spring.data.mongodb.uri` in `src/main/resources/application.properties` before running.
- JWT: `jwt.secret` must be set in `application.properties` for auth to work.

Architecture overview (big picture)
- Spring Boot REST backend. Entry: `WorkerLocatorApplication.java`.
- Controllers expose REST boundaries:
  - `src/main/java/com/example/workerlocator/controller/AuthController.java` — register/login; returns JWT token (subject = email).
  - `.../CustomerController.java` — customer-facing endpoints (nearby-workers, request-service, rate-worker).
  - `.../WorkerController.java` — worker-facing endpoints (incoming-requests, accept/decline, update-availability).
- Services encapsulate business logic:
  - `UserService` — user lifecycle, geo queries (uses `MongoTemplate` and `GeoNear` aggregation).
  - `RequestService` — request lifecycle (pending → accepted/declined → completed); updates user availability/currentJobId.
  - `RatingService` — saves Rating and updates worker averages.
- Repositories: Spring Data MongoDB repositories under `repository/` (CRUD + simple finder methods).
- Security: JWT filter + SecurityConfig
  - `JwtAuthenticationFilter` reads Authorization: Bearer <token>
  - `JwtUtil` generates/parses tokens (jjwt 0.12.6 used)
  - `SecurityConfig` allows static assets and `/api/auth/**` unauthenticated.

Important project-specific conventions and gotchas
- Roles are stored as lowercase strings: `"customer"` and `"worker"`. When loaded into Spring Security roles are uppercased in `UserService.loadUserByUsername()` (.roles(user.getRole().toUpperCase())).
- Geo Point ordering: `org.springframework.data.geo.Point` is (x=longitude, y=latitude). Controllers intentionally create Points as `new Point(lng, lat)` (see `CustomerController.addLocation` and `getNearbyWorkers`).
- Distance units: `UserService.findNearbyWorkers()` converts meters to radians by dividing by 6,371,000 (Earth radius). maxDistance is expected in meters.
- Persistence quirk: some places call `userService.registerUser(worker)` to persist a modified existing user (see `RequestService.acceptRequest` and `completeRequest`). `registerUser` checks for existing email and encodes password — this is a fragile reuse; be cautious when changing save logic.
- JWT handling: tokens are created with subject=email and a `role` claim. `AuthController.login` returns `{ token, userId, role }`.

Data flow examples (concrete)
- Register (customer):
  POST /api/auth/register
  Body: { "email": "a@x.com", "password": "p", "role": "customer" }
- Login:
  POST /api/auth/login
  Body: { "email":"a@x.com", "password":"p", "role":"customer" }
  Response: { "token": "<jwt>", "userId": "<id>", "role": "customer" }
- Create service request (customer):
  POST /api/customer/request-service  (body = ServiceRequest JSON)
  ServiceRequest fields: customerId, workerId, location (Point), serviceType
- Worker accepts request:
  POST /api/worker/accept-request?requestId=<>&userId=<>  — moves request to `accepted`, sets worker.currentJobId and availability=false.

Where to look first (quick map)
- app entry: `src/main/java/com/example/workerlocator/WorkerLocatorApplication.java`
- security: `src/main/java/com/example/workerlocator/security/*` and `config/SecurityConfig.java`
- controllers: `src/main/java/com/example/workerlocator/controller/*`
- services: `src/main/java/com/example/workerlocator/service/*`
- models: `src/main/java/com/example/workerlocator/model/*` (User, ServiceRequest, Rating)
- repos: `src/main/java/com/example/workerlocator/repository/*`
- runtime config: `src/main/resources/application.properties` (set `spring.data.mongodb.uri` and `jwt.secret`)
- static front-end (examples): `src/main/resources/static/*.html` (login/signup) — helpful when changing auth flows.

Developer workflows / commands
- Start locally (after editing application.properties):
  mvn -DskipTests=false spring-boot:run
  (or `mvn clean package` then `java -jar target/worker-locator-0.0.1-SNAPSHOT.jar`)
- Run tests: `mvn test` (no test classes exist today, but standard lifecycle applies).

Integration points & external dependencies
- MongoDB Atlas (spring.data.mongodb.uri) — no migrations or change scripts in repo.
- JJWT library (io.jsonwebtoken 0.12.6) for JWT generation/parsing — verify `JwtUtil` API if you change jjwt version.

Quick improvement hints for PRs (observable from code)
- Avoid using `registerUser()` to save existing users — add a `saveUser(User)` or repository call to avoid password re-encoding / duplicate-email checks.
- Add null-safety checks around Optional.get() usage in services.
- Add unit tests around `UserService.findNearbyWorkers()` (geo query) and `RequestService` acceptance flow (conflicting pending requests).

If anything in this file is unclear or you want extra examples (curl payloads, unit-test scaffolds, or a small Postman collection), say which section to expand and I'll add it.
