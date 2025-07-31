/**
 * Filter for JWT authentication.
 * This filter has been disabled as part of the authentication removal.
 */
package net.kigawa.keruta.infra.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter

// @Component and @Order annotations removed to disable this filter
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Authentication logic removed
        logger.debug("JWT authentication filter disabled")
        filterChain.doFilter(request, response)
    }
}
