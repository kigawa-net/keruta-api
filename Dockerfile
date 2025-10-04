# Build stage
FROM gradle:jdk21 AS builder
WORKDIR /app

# Copy gradle files first for better layer caching
COPY ./gradlew .
COPY ./gradlew.bat .
COPY ./gradle gradle
COPY ./build.gradle.kts .
COPY ./settings.gradle.kts .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY ./src src
COPY ./generated-api generated-api

# Build the application
RUN ./gradlew bootJar --no-daemon --parallel

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN groupadd -r keruta && useradd -r -g keruta keruta

# Copy the built jar file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy terraform templates
COPY ./terraform-templates ./terraform-templates

# Set file ownership
RUN chown -R keruta:keruta /app

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Switch to non-root user
USER keruta

# Expose the application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
