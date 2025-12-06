# Task Completion Checklist

After completing any coding task, run the following commands to ensure code quality:

## Code Quality Checks
```bash
# Format code
./gradlew ktlintFormatAll

# Check code style
./gradlew ktlintCheckAll

# Run tests
./gradlew test

# Build project
./gradlew build
```

## Before Committing
1. **Format Code**: `./gradlew ktlintFormatAll`
2. **Run Tests**: `./gradlew test`
3. **Build Successfully**: `./gradlew build`
4. **Check for Security Issues**: Review code for potential security vulnerabilities
5. **Update Documentation**: If API changes, update OpenAPI spec and generated code

## Integration Testing
- Start MongoDB: `docker-compose up -d mongodb`
- Run integration tests: `./gradlew test --tests="*IntegrationTest"`
- Verify WebSocket connections work
- Test API endpoints with Postman/curl

## Deployment Preparation
- Ensure environment variables are documented
- Verify Docker build works: `docker build -t keruta-api .`
- Test with docker-compose: `docker-compose up -d`

## Common Issues to Check
- Service classes marked as `open`
- Proper dependency injection
- Exception handling in controllers
- CORS configuration for WebSocket
- JWT token validation
- MongoDB connection configuration