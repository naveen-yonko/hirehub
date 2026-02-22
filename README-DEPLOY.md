Worker-Locator — Quick deploy (easy path: Render)

This file explains a minimal, easy path to deploy the Worker-Locator Spring Boot backend using a free MongoDB Atlas cluster and Render (no Docker required).

1) Create MongoDB Atlas (free)
- Sign up at https://www.mongodb.com/cloud/atlas and create a free cluster.
- Create a database user (username/password) and allow network access (for testing you can allow 0.0.0.0/0 but prefer your host's IP).
- In the Atlas UI get the connection string (Driver 4.2+):
  Example: `mongodb+srv://<user>:<password>@cluster0.mongodb.net/hirehub?retryWrites=true&w=majority`

2) Prepare repository (we include a Dockerfile optional)
- Add the following environment variables to the host (Render):
  - `SPRING_DATA_MONGODB_URI` = Atlas connection URI
  - `JWT_SECRET` = a long random secret
  - `SPRING_SECURITY_JWT_SECRET` = same as `JWT_SECRET`
  - (Optional) `PORT` = platform-provided; Render injects this automatically

3) Render — create a Web Service (easy, no Docker)
- Connect your GitHub repo to Render and create a new `Web Service`.
- Use the following settings:
  - Build Command: `./mvnw -DskipTests clean package`
  - Start Command: `java -jar target/*.jar`
  - Environment: set the vars listed above in the Render dashboard.

4) Local test commands
- Build locally:
  ```bash
  ./mvnw -DskipTests clean package
  ```
- Run locally (use your Atlas URI or local Mongo):
  ```bash
  export SPRING_DATA_MONGODB_URI="mongodb+srv://..."
  export JWT_SECRET="<your-random-secret>"
  export SPRING_SECURITY_JWT_SECRET="$JWT_SECRET"
  java -jar target/worker-locator-0.0.1-SNAPSHOT.jar
  ```

5) Optional: Deploy with Docker (if you prefer deterministic Java 21)
- A simple `Dockerfile` is included in the repo. If you choose Render's Docker option or Fly.io, build and push the image.

Notes
- The project already contains default Mongo settings in `src/main/resources/application.properties` for local dev. When deploying, set `SPRING_DATA_MONGODB_URI` to override those values.
- Ensure `jwt.secret` in production is set to a secure random value and keep it secret.

If you want, I can:
- Create a `render.yaml` or `.dockerignore`/`Dockerfile` tweaks
- Add a GitHub Actions workflow to build and push to Render (or Docker registry)

GitHub Actions — automatic deploy to Render
- Create a Render API key: Dashboard -> Account -> API Keys -> "Create API Key" (read/write).
- Get your service id: In the Render dashboard open your Web Service and copy the service id from the URL or the Service settings.
- Add two repository Secrets in GitHub: `RENDER_API_KEY` and `RENDER_SERVICE_ID` (Repository -> Settings -> Secrets -> Actions).
- A workflow file `.github/workflows/render-deploy.yml` is included which will:
  - Build the project with JDK 21 and Maven
  - Trigger a deploy by calling Render's API `POST /v1/services/{serviceId}/deploys`

Notes
- The workflow assumes your primary branch is named `main`. If you use a different branch, update the `on.push.branches` value in the workflow.
- The workflow uses `jq` in the deploy step to pretty-print the Render response. Render's runner images have `jq` preinstalled; if not, the `curl` output still succeeds.
- Keep your `SPRING_DATA_MONGODB_URI`, `JWT_SECRET`, and `SPRING_SECURITY_JWT_SECRET` configured in Render's Environment settings (not in GitHub secrets unless you plan to inject them during deploy).

Next steps I can take for you:
- Add a `render.yaml` for Render's spec-based deploy (optional).
- Add GitHub Actions steps to push built Docker images to a registry before deploying (if you prefer container-based deploys).

