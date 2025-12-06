# Project Overview

## Purpose
Keruta API is the backend component of the Keruta task execution system, providing session and workspace management for Coder workspaces. It handles task execution, workspace lifecycle, and integrates with various providers via KTCP protocol.

## Tech Stack
- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.0
- **Database**: MongoDB with Kotlin Coroutine driver
- **Communication**: WebSocket (for KTCP), REST API
- **Security**: JWT authentication, Spring Security
- **Async**: Kotlin Coroutines, Reactor
- **Messaging**: Kafka, ZooKeeper
- **Build**: Gradle with Kotlin DSL
- **Code Quality**: ktlint
- **Testing**: JUnit 5, TestContainers, Mockito

## Architecture
Single-module Gradle project with layered architecture:
- `net.kigawa.keruta.api` - REST controllers and web layer
- `net.kigawa.keruta.infra.persistence` - MongoDB repository implementations
- `net.kigawa.keruta.infra.security` - Security configuration and JWT
- `net.kigawa.keruta.infra.web` - Web configuration (CORS, etc.)
- Generated API from OpenAPI spec

## Key Components
- Session management (1:1 with workspaces)
- Task execution via KTCP WebSocket protocol
- Coder workspace integration
- Document management
- Repository management
- Git public key management