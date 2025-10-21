# finflow Customer Account & Transactions Solution (Kafka-enabled) — Build & Run Guide

This README contains step-by-step instructions to build and run the Kafka-enabled reactive Spring Boot Customer Account & Transactions project. It covers prerequisites, starting Kafka via Docker Compose, building the app, running locally or in Docker, sample API calls, 

---

## Prerequisites

Make sure you have the following installed on your machine:

- Java 22 (check with `java -version`)
- Maven 3.9+ (check with `mvn -v`)
- Docker & Docker Compose (for Kafka and optional containerized run)

---
## 1) Build project jar

```bash
mvn clean package -DskipTests
```
This produces the executable JAR under `target/finflow-reactive-solution-0.0.1-SNAPSHOT.jar`.

Build Docker image to Run the application in Docker:

```bash
docker-compose build --no-cache
```

## 2) Start Kafka (Zookeeper + Kafka) using Docker Compose

Open a terminal in the project root and run:

```bash
docker-compose up -d
```

This will start two containers:
- Zookeeper on port `2181`
- Kafka on port `9092`

Check containers are running:

```bash
docker ps 
```

If you need to stop them later:

```bash
docker-compose down
docker logs -f finflow-app
```

---

## 3) Run the application locally if needed.

Run the jar:

```bash
java -jar target/finflow-reactive-solution-0.0.1-SNAPSHOT.jar
```

The app starts on port **8080** (configurable in `application.yml`).

You should see Spring Boot startup logs and messages indicating the application is ready. The Kafka consumer will subscribe automatically on startup (the consumer logs will show messages when events are received).

---

## 4) API docs (Swagger)

Once the application is running, you can open Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

It lists `/accounts` and `/transactions` endpoints and request/response schemas.

---

## 5) Troubleshooting & tips

- **"Connection refused" to Kafka (localhost:9092)**  
  - Make sure `docker-compose up -d` succeeded and that Kafka container is healthy.  
  - Check logs:
    ```bash
    docker-compose logs kafka
    docker-compose logs zookeeper
    ```

---

## 6) Cleanup

To stop Kafka and clean containers:

```bash
docker-compose down
```

Stop & remove old containers and images if needed:

```bash
docker-compose down -v
docker system prune -af --volumes
```






## Error Handling

This project uses a centralized `GlobalExceptionHandler` (Spring `@RestControllerAdvice`) to map exceptions to HTTP responses:

- `ResourceNotFoundException` → **404**
- `BusinessException`, validation errors → **400**
- `DataIntegrityViolationException` → **409**
- Any other unhandled exception → **500**
