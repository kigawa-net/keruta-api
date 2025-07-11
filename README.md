# keruta-api

Keruta API is the backend component of the Keruta task execution system.

## Architecture Overview

Keruta API is structured as a multi-module Gradle project:

- `core:domain` - Domain models (Task, Agent, Repository, etc.)
- `core:usecase` - Business logic and use cases
- `infra:persistence` - MongoDB repository implementations
- `infra:security` - Security configuration
- `infra:app` - Kubernetes integration and job orchestration
- `api` - REST controllers and web layer

## Development

### Building and Running
```bash
# Build the entire project
./gradlew build

# Run the Spring Boot application
./gradlew :api:bootRun
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --continue
```

### Logging Configuration
The application uses INFO level logging by default. The log configuration is defined in:
- `api/src/main/resources/application.properties`
- `infra/app/src/main/resources/application.properties`

To enable more detailed DEBUG logging, modify these files and change the log levels from INFO to DEBUG.

### Code Quality
```bash
# Check code style (all modules)
./gradlew ktlintCheckAll

# Format code (all modules)
./gradlew ktlintFormatAll

# Clean build
./gradlew clean
```
