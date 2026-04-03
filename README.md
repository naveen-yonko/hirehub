# HireHub (Worker Locator)

Simple Spring Boot backend + static frontend for a local worker matching app.

## Quick status
- Project builds successfully with `./mvnw -DskipTests package`.
- Use `src/main/resources/application.properties` to set MongoDB URI and `jwt.secret` before running.

## Screenshots (opscreenshots)
The `opscreenshots` folder contains UI screenshots used below and in the project documentation.

- Login / Landing
  ![Login](opscreenshots/Screenshot (407).png)
- Dashboard / Customer
  ![Customer Dashboard](opscreenshots/Screenshot (408).png)
- Dashboard / Worker
  ![Worker Dashboard](opscreenshots/Screenshot (409).png)
- Other flows
  ![Flow 1](opscreenshots/Screenshot (410).png)
  ![Flow 2](opscreenshots/Screenshot (411).png)
  ![Flow 3](opscreenshots/Screenshot (412).png)
  ![Flow 4](opscreenshots/Screenshot (413).png)
  ![Flow 5](opscreenshots/Screenshot (414).png)

## Run locally

1. Configure MongoDB and JWT in `src/main/resources/application.properties` (or set `spring.data.mongodb.uri`):

   - `spring.data.mongodb.uri=mongodb://<user>:<pass>@host:port/db` OR set host/port/database properties.
   - `jwt.secret=` a long random string.

2. Build the jar:

```bash
./mvnw -DskipTests package
```

3. Run locally:

```bash
java -jar target/worker-locator-0.0.1-SNAPSHOT.jar
```

The frontend static files are served from `src/main/resources/static` at `http://localhost:8080`.

## Docker
The repository includes a simple `Dockerfile` that runs the packaged jar.

Build image:

```bash
docker build -t hirehub:latest .
```

Run container (example with MongoDB connection):

```bash
docker run -e SPRING_DATA_MONGODB_URI="mongodb://host:27017/hirehub" -e JWT_SECRET="your_jwt" -p 8080:8080 hirehub:latest
```

## Deploy to a host (suggestions)
- Render / Railway / Heroku: push Docker image or use `java -jar` with environment variables.
- Ensure `spring.data.mongodb.uri` points to an accessible MongoDB (Atlas recommended for production).

## Common issues & fixes
- CORS: static frontend expects backend at `http://localhost:8080`. Enable CORS or update `application.properties`.
- JWT: set `jwt.secret` in `application.properties` or via env var `JWT_SECRET`.
- Password re-encoding: some code reuses `registerUser()` when saving existing users — avoid calling that method for updates.

## GitHub upload
I can prepare the repo for push (create/modify files). To push to your repository `https://github.com/naveen-yonko/hirehub` run:

```bash
git remote add origin https://github.com/naveen-yonko/hirehub.git
git branch -M main
git add .
git commit -m "Prepare project for deployment: README, docs"
git push -u origin main
```

If you want me to attempt to add a remote and push from here I will need your Git credentials or a personal access token configured in the environment — please confirm how you'd like to proceed.

## Next steps I will take (confirm to proceed):
- Fix runtime issues (if you provide error logs from running the app).
- Harden `application.properties` for deployment and add sample `.env` or profiles.
- Add a GitHub Actions workflow to build and push a Docker image (optional).

---
_Prepared using the repo opscreenshots. Tell me if you want a shorter README or to include additional screenshots inline._
