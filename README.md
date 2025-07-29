# 🧬 Ehealth FHIR visualization App

This application exposes a REST API around the [laboResultVisualization](https://github.com/ehealthplatformstandards/laboResultVisualization) toolset, enabling users to submit FHIR resources and receive HTML visualizations and validation feedback.

If you wish to view the API documentation without running the app, you can access the latest published documentation at `resources/static/api/api-docs.yaml`.

---

## 🚀 Run the Application with Docker (Recommended)

A prebuilt Docker image is publicly available for quick testing.

To pull and run the latest image:

```bash
docker run -p 8912:8912 ehealthplatformstandards/fhir-vizualization-api:latest
```

Once running, open your browser at [http://localhost:8912/api/index.html](http://localhost:8912/api/index.html) to explore the API via Swagger UI.

---

## 🛠 Build and Run the Application Locally

This approach is ideal if you want to modify the source code or work with the latest unreleased changes.

### ✅ Step 1: Build and Publish `laboResultVisualization`

Clone and build the [laboResultVisualization](https://github.com/ehealthplatformstandards/laboResultVisualization) project.

```bash
git clone https://github.com/ehealthplatformstandards/laboResultVisualization.git
cd laboResultVisualization
mvn clean deploy -DskipTests
```

This will:

* Generate the JAR artifact
* Publish it to a local Maven repository under `target/mvn-repo/`

---

### ✅ Step 2: Link the Local Repository in `fhir-vizualization-api`

In your `fhir-vizualization-api` project’s `build.gradle.kts`, update the `repositories` block to include the local repo path:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("/home/<your-username>/IdeaProjects/laboResultVisualization/target/mvn-repo") }
}
```

> Replace `<your-username>` with your actual username or path as appropriate.

This ensures Gradle finds the `fhir-visualization-tool` dependency locally.

---

### ✅ Step 3: Build and Run the API

#### 🔹 Option 1: From IntelliJ

Open `EhealthFhirVisualization.kt` and run the `main()` method directly.

#### 🔹 Option 2: From Terminal

Build the project:

```bash
./gradlew build
```

Then run the JAR:

```bash
java -jar build/libs/ehealth-fhirviz-<version>.jar
```

> Example:
> `java -jar build/libs/ehealth-fhirviz-0.1-51-g96a7cbc.dirty.jar`

---

## 🧪 Test the API

Once running, access the Swagger UI:

📍 [http://localhost:8912/api/index.html](http://localhost:8912/api/index.html)

You can use this interface to:

* Explore available endpoints
* Submit test FHIR resources (in JSON or XML)
* View validation results and HTML rendering

---

## 📦 Output and Features

* Converts FHIR resources to human-readable HTML
* Validates resources against the Belgian eHealth vaccination profile
* Supports both `application/json` and `application/xml`
* Includes OpenAPI documentation (Swagger)