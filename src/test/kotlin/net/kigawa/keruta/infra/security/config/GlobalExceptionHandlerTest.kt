package net.kigawa.keruta.infra.security.config

import net.kigawa.keruta.api.common.dto.ErrorResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest
import java.util.NoSuchElementException

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests the standardized error response format.
 */
class GlobalExceptionHandlerTest {

    private val exceptionHandler = GlobalExceptionHandler()

    @Test
    fun `should create ErrorResponse for NoSuchElementException`() {
        val exception = NoSuchElementException("Resource not found")
        val request = ServletWebRequest(MockHttpServletRequest("GET", "/test"))

        val response = exceptionHandler.handleNoSuchElementException(exception, request)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)

        val errorResponse = response.body!!
        assertEquals("RESOURCE_NOT_FOUND", errorResponse.error.code)
        assertEquals("Resource not found", errorResponse.error.message)
        assertEquals(404, errorResponse.error.status)
        assertEquals("1.0.0", errorResponse.meta.version)
        assertNotNull(errorResponse.meta.timestamp)
    }

    @Test
    fun `should create ErrorResponse for IllegalArgumentException`() {
        val exception = IllegalArgumentException("Invalid input")
        val request = ServletWebRequest(MockHttpServletRequest("POST", "/test"))

        val response = exceptionHandler.handleIllegalArgumentException(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)

        val errorResponse = response.body!!
        assertEquals("INVALID_INPUT", errorResponse.error.code)
        assertEquals("Invalid input", errorResponse.error.message)
        assertEquals(400, errorResponse.error.status)
        assertEquals("1.0.0", errorResponse.meta.version)
        assertNotNull(errorResponse.meta.timestamp)
    }

    @Test
    fun `should create ErrorResponse for generic Exception`() {
        val exception = RuntimeException("Unexpected error")
        val request = ServletWebRequest(MockHttpServletRequest("GET", "/test"))

        val response = exceptionHandler.handleGenericException(exception, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)

        val errorResponse = response.body!!
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.error.code)
        assertEquals(
            "An unexpected error occurred. Our team has been notified and is working to resolve the issue. Please try again later.",
            errorResponse.error.message,
        )
        assertEquals(500, errorResponse.error.status)
        assertEquals("1.0.0", errorResponse.meta.version)
        assertNotNull(errorResponse.meta.timestamp)
    }

    @Test
    fun `ErrorResponse should have correct structure`() {
        val errorResponse = ErrorResponse.create(
            code = "TEST_ERROR",
            message = "Test message",
            status = 400,
            path = "/test/path",
            details = mapOf("field" to "value"),
        )

        assertEquals("TEST_ERROR", errorResponse.error.code)
        assertEquals("Test message", errorResponse.error.message)
        assertEquals(400, errorResponse.error.status)
        assertEquals(mapOf("field" to "value"), errorResponse.error.details)
        assertEquals("1.0.0", errorResponse.meta.version)
        assertEquals("/test/path", errorResponse.meta.path)
        assertNotNull(errorResponse.meta.timestamp)
    }
}
