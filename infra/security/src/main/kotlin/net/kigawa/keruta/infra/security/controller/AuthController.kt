package net.kigawa.keruta.infra.security.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.infra.security.dto.LoginRequest
import net.kigawa.keruta.infra.security.dto.RefreshTokenRequest
import net.kigawa.keruta.infra.security.dto.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for authentication.
 * Authentication has been disabled, but endpoints are kept for compatibility.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API (Disabled)")
class AuthController {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    // Dummy tokens that will be returned for all requests
    private val dummyAccessToken = "dummy-access-token"
    private val dummyRefreshToken = "dummy-refresh-token"

    /**
     * Login endpoint.
     * Authentication has been disabled, so this always returns dummy tokens.
     *
     * @param loginRequest The login request
     * @return The token response with dummy tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Login (Disabled)", description = "Authentication has been disabled, returns dummy tokens")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<TokenResponse> {
        logger.info("Login request received for user: ${loginRequest.username} (Authentication disabled)")
        return ResponseEntity.ok(TokenResponse(dummyAccessToken, dummyRefreshToken))
    }

    /**
     * Refresh token endpoint.
     * Authentication has been disabled, so this always returns dummy tokens.
     *
     * @param refreshTokenRequest The refresh token request
     * @return The token response with dummy tokens
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token (Disabled)",
        description = "Authentication has been disabled, returns dummy tokens",
    )
    fun refresh(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.info("Token refresh request received (Authentication disabled)")
        return ResponseEntity.ok(TokenResponse(dummyAccessToken, dummyRefreshToken))
    }
}
