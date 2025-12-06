# Code Style and Conventions

## Kotlin Style
- Use ktlint for code formatting and linting (version 0.50.0)
- Follow standard Kotlin coding conventions
- Use data classes for DTOs and immutable data structures
- Prefer val over var for immutability
- Use sealed classes for message types and error handling
- Use companion objects for static members and factory methods

## Naming Conventions
- **Packages**: lowercase, hierarchical (net.kigawa.keruta.api, infra.persistence)
- **Classes**: PascalCase (TaskController, MongoConfig)
- **Functions/Methods**: camelCase (createTask, getById)
- **Variables/Properties**: camelCase (taskId, userName)
- **Constants**: UPPER_SNAKE_CASE (MAX_RETRY_COUNT)
- **Test classes**: ClassNameTest (TaskServiceTest)

## Architecture Patterns
- **SOLID Principles**: Single responsibility, Open-closed, Liskov substitution, Interface segregation, Dependency inversion
- **Layered Architecture**: API → Use Case → Repository → Database
- **Pure Functions**: Prefer functional programming where possible
- **Dependency Injection**: Use Spring's @Component, @Service, @Repository annotations
- **Service Classes**: Mark all service classes as `open` for Spring CGLIB proxy creation

## Annotations
- `@Component` for general components
- `@Service` for business logic services
- `@Repository` for data access
- `@Controller` for REST endpoints
- `@RestController` for REST API controllers
- `@Configuration` for configuration classes

## Error Handling
- Use custom exception classes extending RuntimeException
- Use @ControllerAdvice for global exception handling
- Return appropriate HTTP status codes
- Use Optional for nullable return values

## Testing
- Use JUnit 5 with Jupiter
- Use TestContainers for integration tests (MongoDB, Kafka)
- Use Mockito for mocking dependencies
- Use @SpringBootTest for integration tests
- Use @WebMvcTest for controller tests
- Use @DataMongoTest for repository tests

## Documentation
- Use KDoc for public APIs
- Document complex business logic
- Keep comments concise and meaningful
- Avoid redundant comments

## Security
- Validate all inputs
- Use parameterized queries to prevent injection
- Implement proper authentication and authorization
- Use HTTPS in production
- Sanitize user inputs

## Performance
- Use coroutines for async operations
- Implement proper connection pooling
- Use lazy initialization where appropriate
- Monitor resource usage
- Implement caching for frequently accessed data