# Git Key Generation Endpoint Fix

## Problem Summary
The `/api/v1/git/keys/generate` endpoint was returning a "No static resource" error instead of properly routing to the controller. The error indicated Spring was treating the API endpoint as a static resource request.

## Root Cause Analysis
1. **Controller Implementation**: ✅ CORRECT - The `GitPublicKeyController` properly implements the `/generate` endpoint
2. **Service Implementation**: ✅ CORRECT - The `GitPublicKeyService` has a complete `generateGitKeyPair` implementation
3. **DTO Support**: ✅ CORRECT - All necessary DTOs (`GenerateGitKeyPairRequest`, `GenerateGitKeyPairResponse`) are implemented
4. **Spring Configuration**: ❌ ISSUE - Missing WebMVC configuration causing routing conflicts

## Applied Fixes

### 1. WebMVC Configuration (`/src/main/kotlin/net/kigawa/keruta/infra/web/config/WebMvcConfig.kt`)
Created a proper WebMVC configuration to ensure REST API endpoints take precedence over static resource handling:

```kotlin
@Configuration
@EnableWebMvc
class WebMvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Configure specific static resource paths only
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
        // ... other specific handlers
    }
}
```

### 2. Application Properties Update
Added Spring MVC configuration to `application.properties`:

```properties
# Spring MVC Configuration
spring.mvc.static-path-pattern=/static/**
spring.web.resources.static-locations=classpath:/static/
spring.mvc.dispatch-options-request=true
spring.mvc.servlet.path=/
```

### 3. Health Check Endpoint
Added a health check endpoint to verify controller registration:
- `GET /api/v1/git/keys/health`

## Endpoint Details

### Generate Git Key Pair
- **URL**: `POST /api/v1/git/keys/generate`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "name": "my-key-name",
  "keyType": "SSH",
  "keySize": 2048,
  "algorithm": "ssh-rsa"
}
```
- **Response**:
```json
{
  "publicKey": {
    "id": "uuid",
    "name": "my-key-name",
    "keyType": "SSH",
    "publicKey": "ssh-rsa AAAAB3...",
    "fingerprint": "SHA256:...",
    "algorithm": "ssh-rsa",
    "keySize": 2048,
    "associatedRepositories": [],
    "isActive": true,
    "lastUsed": null,
    "createdAt": "2025-09-05T...",
    "updatedAt": "2025-09-05T..."
  },
  "privateKey": "-----BEGIN OPENSSH PRIVATE KEY-----\n...\n-----END OPENSSH PRIVATE KEY-----"
}
```

## Testing

### Automated Test Script
Created `test-key-generation.sh` for comprehensive endpoint testing:

```bash
# Run the test script
./test-key-generation.sh
```

### Manual Testing
1. **Health Check**: `GET /api/v1/git/keys/health`
2. **Key Generation**: `POST /api/v1/git/keys/generate`
3. **Key Validation**: `POST /api/v1/git/keys/validate`

### Sample cURL Commands

```bash
# Health check
curl -X GET http://localhost:8080/api/v1/git/keys/health

# Generate SSH key
curl -X POST http://localhost:8080/api/v1/git/keys/generate \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-key",
    "keyType": "SSH",
    "keySize": 2048,
    "algorithm": "ssh-rsa"
  }'

# Generate GPG key
curl -X POST http://localhost:8080/api/v1/git/keys/generate \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-gpg-key",
    "keyType": "GPG",
    "keySize": 2048
  }'
```

## Next Steps

1. **Build and Deploy**: Compile and start the application
2. **Run Tests**: Execute the test script to verify functionality
3. **Monitor Logs**: Check application logs for any remaining issues
4. **Documentation**: Update API documentation if needed

## Files Modified/Created

1. ✅ `WebMvcConfig.kt` - Created WebMVC configuration
2. ✅ `application.properties` - Updated with MVC settings  
3. ✅ `GitPublicKeyController.kt` - Added health check endpoint
4. ✅ `test-key-generation.sh` - Created test script
5. ✅ `GIT_KEY_GENERATION_FIX.md` - This documentation

## Expected Outcome

After applying these fixes, the `/api/v1/git/keys/generate` endpoint should:
- ✅ Route correctly to the controller
- ✅ Accept POST requests with JSON payload
- ✅ Generate and return SSH/GPG key pairs
- ✅ Store public keys in the database
- ✅ Return proper error responses for invalid requests