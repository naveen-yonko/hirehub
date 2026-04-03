# HireHub — Worker Locator

This repository contains a Spring Boot backend and a small static frontend for HireHub, a simple app that helps customers find local workers (plumbers, electricians, carpenters, cleaners, etc.).

This README highlights the app UI with annotated screenshots (in `opscreenshots/`) and provides quick run and deployment examples.

## UI walkthrough (screenshots)

All screenshots are in the `opscreenshots/` folder. Below each image is a short description and an example user flow.

- Login / Landing (opscreenshots/Screenshot (407).png)

  Description: The public landing page with the login card. The form lets users choose `Customer` or `Worker`, enter email/password, and sign in.

  Example: A customer signs in with `customer@example.com` and is redirected to the customer dashboard to request services.

  ![Landing / Login](opscreenshots/Screenshot (407).png)

- Customer dashboard (opscreenshots/Screenshot (408).png)

  Description: Customer view showing nearby workers, request creation form, and active requests list.

  Example: Customer picks a worker, sends a service request with a location and service type, and waits for the worker to accept.

  ![Customer Dashboard](opscreenshots/Screenshot (408).png)

- Worker dashboard (opscreenshots/Screenshot (409).png)

  Description: Worker view showing incoming requests, current job, and availability toggle.

  Example: A worker toggles availability to `true` and sees new nearby requests appear in the incoming list.

  ![Worker Dashboard](opscreenshots/Screenshot (409).png)

- Request lifecycle screenshots (opscreenshots/Screenshot (410).png — Screenshot (414).png)

  Description: Additional screenshots demonstrating the request flow, accept/decline actions, and rating flow after a job is completed.

  Example: After a worker completes a job, the customer rates the worker and the worker's average rating is updated.

  ![Request Flow 1](opscreenshots/Screenshot (410).png)
  ![Request Flow 2](opscreenshots/Screenshot (411).png)
  ![Request Flow 3](opscreenshots/Screenshot (412).png)
  ![Request Flow 4](opscreenshots/Screenshot (413).png)
  ![Request Flow 5](opscreenshots/Screenshot (414).png)

## Quick start (developer)

1. Configure runtime properties in `src/main/resources/application.properties`:

   - For local MongoDB (default example):

     ```properties
     spring.data.mongodb.host=localhost
     spring.data.mongodb.port=27017
     spring.data.mongodb.database=hirehub
     jwt.secret=your_jwt_secret_here
     ```

   - Or set a single connection string:

     ```properties
     spring.data.mongodb.uri=mongodb://user:pass@host:27017/hirehub
     ```

2. Build the project:

```bash
./mvnw -DskipTests package
```

3. Run locally:

```bash
java -jar target/worker-locator-0.0.1-SNAPSHOT.jar
```

Open `http://localhost:8080` in your browser to see the static frontend. The frontend expects the backend API to be on the same host/port (CORS is enabled for local development in `application.properties`).

## Docker (quick)

Build the Docker image:

```bash
docker build -t hirehub:latest .
```

Run with environment variables:

```bash
docker run -e SPRING_DATA_MONGODB_URI="mongodb://host:27017/hirehub" -e JWT_SECRET="your_jwt" -p 8080:8080 hirehub:latest
```

## Why the app failed to start earlier

- Root cause observed during runs: the local MongoDB server at `localhost:27017` was not reachable, causing index-creation and data-seeding code to throw exceptions during startup. This prevented Spring Boot from finishing initialization.

- What I changed:
  - Wrapped index creation in `MongoConfig` with a try/catch and logged a warning if index creation failed.
  - Guarded the test-data initializers (`MongoInitializer` and `DataSeeder`) with try/catch blocks so the application can start even if MongoDB is temporarily unavailable. Seeding will be skipped until MongoDB becomes reachable.

These changes let the web application start and serve static pages even when MongoDB is down; database-dependent features will be disabled until the database becomes available.

## Next recommended steps (I'll continue if you want)

- Start a MongoDB instance (locally or Atlas) and update `application.properties` with the URI so data seeding and indexes run correctly.
- Add a small healthcheck endpoint and readiness probe (useful for Docker/Kubernetes deployments).
- Optional: add GitHub Actions that build the jar and the Docker image and run basic smoke tests.

---
If you'd like the README to include smaller inline thumbnails with captions (or move screenshots into `docs/`), tell me which layout you prefer and I will update it and push the change.
