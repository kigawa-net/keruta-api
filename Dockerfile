# Build stage
FROM gradle:jdk21 AS builder
WORKDIR /app

# Copy gradle files first for better layer caching
COPY ./gradlew .
COPY ./gradle gradle
COPY ./build.gradle.kts .
COPY ./settings.gradle.kts .
COPY ./buildSrc buildSrc

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY ./api api
COPY ./core core
COPY ./infra infra

# Build the application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/api/build/libs/*.jar app.jar

# Copy terraform templates
COPY ./terraform-templates ./terraform-templates

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
