/**
 * Provider for JWT token generation and validation.
 * This component handles all JWT token operations, including creation,
 * validation, and extraction of authentication information.
 */
package net.kigawa.keruta.infra.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService,
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    @Value("\${jwt.secret:your-secret-key-here-should-be-very-long-and-secure}")
    private lateinit var secretString: String

    @Value("\${jwt.expiration:86400000}")
    private var validityInMilliseconds: Long = 0 // 24h by default

    @Value("\${jwt.refresh-expiration:604800000}")
    private var refreshValidityInMilliseconds: Long = 0 // 7 days by default

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretString.toByteArray())
    }

    /**
     * Creates a JWT token for the given authentication.
     *
     * @param authentication The authentication object
     * @return The JWT token
     */
    fun createToken(authentication: Authentication): String {
        val username = authentication.name
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        logger.debug("Creating JWT token for user: $username, valid until: $validity")

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact().also {
                logger.debug("JWT token created successfully for user: $username")
            }
    }

    /**
     * Creates a refresh token for the given authentication.
     *
     * @param authentication The authentication object
     * @return The refresh token
     */
    fun createRefreshToken(authentication: Authentication): String {
        val username = authentication.name
        val now = Date()
        val validity = Date(now.time + refreshValidityInMilliseconds)

        logger.debug("Creating refresh token for user: $username, valid until: $validity")

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .claim("refresh", true)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact().also {
                logger.debug("Refresh token created successfully for user: $username")
            }
    }

    /**
     * Gets the authentication from a JWT token.
     *
     * @param token The JWT token
     * @return The authentication object
     */
    fun getAuthentication(token: String): Authentication {
        logger.debug("Getting authentication from JWT token")

        val claims = getClaims(token)
        val username = claims.subject
        logger.debug("JWT token subject: $username")

        val userDetails = userDetailsService.loadUserByUsername(username)
        logger.debug("User details loaded for user: $username, authorities: ${userDetails.authorities}")

        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities).also {
            logger.debug("Authentication created for user: $username with authorities: ${it.authorities}")
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token
     * @return true if the token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        logger.debug("Validating JWT token")

        return try {
            val claims = getClaims(token)
            val isValid = !claims.expiration.before(Date())

            if (isValid) {
                logger.debug("JWT token is valid, subject: ${claims.subject}, expiration: ${claims.expiration}")
            } else {
                logger.warn("JWT token is expired, subject: ${claims.subject}, expiration: ${claims.expiration}")
            }

            isValid
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT token is expired: ${e.message}")
            false
        } catch (e: UnsupportedJwtException) {
            logger.warn("JWT token is unsupported: ${e.message}")
            false
        } catch (e: MalformedJwtException) {
            logger.warn("JWT token is malformed: ${e.message}")
            false
        } catch (e: SignatureException) {
            logger.warn("JWT token has invalid signature: ${e.message}")
            false
        } catch (e: Exception) {
            logger.error("JWT token validation error", e)
            false
        }
    }

    /**
     * Gets the claims from a JWT token.
     *
     * @param token The JWT token
     * @return The claims
     */
    private fun getClaims(token: String): Claims {
        logger.debug("Parsing JWT token to extract claims")

        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body.also {
                    logger.debug("JWT claims extracted successfully: subject=${it.subject}, expiration=${it.expiration}")
                }
        } catch (e: Exception) {
            logger.error("Failed to parse JWT token", e)
            throw e
        }
    }

    /**
     * Creates a JWT token for API access.
     * This method doesn't require an Authentication object and is useful for system-generated tokens.
     *
     * @param subject The subject of the token (usually a username or identifier)
     * @return The JWT token
     */
    fun createApiToken(subject: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        logger.debug("Creating API token for subject: $subject, valid until: $validity")

        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(validity)
            .claim("type", "api")
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact().also {
                logger.debug("API token created successfully for subject: $subject")
            }
    }
}
