package net.kigawa.keruta.infra.security.dto

/**
 * Request for login.
 */
data class LoginRequest(
    val username: String,
    val password: String,
)

/**
 * Request for token refresh.
 */
data class RefreshTokenRequest(
    val refreshToken: String,
)

/**
 * Response with authentication tokens.
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)
