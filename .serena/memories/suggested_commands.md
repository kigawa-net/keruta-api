# Suggested Commands

## Development Setup
```bash
# Start MongoDB
docker-compose up -d mongodb

# Run application
./gradlew bootRun

# Access admin interface
open http://localhost:8080/admin

# Access API docs
open http://localhost:8080/swagger-ui.html
```

## Building and Testing
```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run tests with continue on failure
./gradlew test --continue
```

## Code Quality
```bash
# Check code style
./gradlew ktlintCheckAll

# Format code
./gradlew ktlintFormatAll

# Clean build
./gradlew clean build
```

## Database
```bash
# Start MongoDB
docker-compose up -d mongodb

# View MongoDB logs
docker-compose logs -f mongodb

# Reset database
docker-compose down -v && docker-compose up -d mongodb
```

## Docker
```bash
# Build Docker image
docker build -t keruta-api .

# Run with Docker Compose (full stack)
docker-compose up -d
```

## Git and Version Control
```bash
# Format code before commit
./gradlew ktlintFormatAll

# Run tests before commit
./gradlew test
```