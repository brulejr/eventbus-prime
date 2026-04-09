# EventBus Prime

A reference Spring Boot + Kotlin project for an event-first workflow engine where an EventBus is the substrate and a workflow traffic cop governs legal progression.

## Included

- In-memory EventBus
- Workflow traffic cop
- Immutable workflow instance snapshots
- Workflow history tracking
- Middleware-enabled step execution
- Sample workflow
- REST endpoints to start and inspect workflows
- AssertJ-based integration test

## Run

```bash
./gradlew bootRun
```

If the Gradle wrapper is not present, install Gradle locally and run:

```bash
gradle bootRun
```

## Sample request

```bash
curl -X POST http://localhost:8080/api/work \
  -H 'Content-Type: application/json' \
  -d '{"requestId":"req-1","description":"hello world"}'
```

## Query instances

```bash
curl http://localhost:8080/api/work/instances
curl http://localhost:8080/api/work/instances/by-correlation/req-1
```
