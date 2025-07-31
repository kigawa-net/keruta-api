package net.kigawa.keruta.infra.security.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest
import java.util.NoSuchElementException

@ExtendWith(MockitoExtension::class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private lateinit var exceptionHandler: GlobalExceptionHandler

    @Mock
    private lateinit var webRequest: WebRequest

    @Test
    fun `handleNoSuchElementException should return 404 with polite error message`() {
        // Given
        val exception = NoSuchElementException("Task not found with id: 123")

        // When
        val response = exceptionHandler.handleNoSuchElementException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode.value())

        val body = response.body
        assertNotNull(body)
        assertTrue(body!!.containsKey("error"))
        assertTrue(body.containsKey("meta"))

        val error = body["error"] as Map<*, *>
        assertEquals("RESOURCE_NOT_FOUND", error["code"])
        assertEquals("Task not found with id: 123", error["message"])
        assertEquals(HttpStatus.NOT_FOUND.value(), error["status"])

        val meta = body["meta"] as Map<*, *>
        assertTrue(meta.containsKey("timestamp"))
        assertEquals("1.0.0", meta["version"])
    }

    @Test
    fun `handleIllegalArgumentException should return 400 with polite error message`() {
        // Given
        val exception = IllegalArgumentException("Invalid task status: UNKNOWN")

        // When
        val response = exceptionHandler.handleIllegalArgumentException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())

        val body = response.body
        assertNotNull(body)
        assertTrue(body!!.containsKey("error"))
        assertTrue(body.containsKey("meta"))

        val error = body["error"] as Map<*, *>
        assertEquals("INVALID_INPUT", error["code"])
        assertEquals("Invalid task status: UNKNOWN", error["message"])
        assertEquals(HttpStatus.BAD_REQUEST.value(), error["status"])

        val meta = body["meta"] as Map<*, *>
        assertTrue(meta.containsKey("timestamp"))
        assertEquals("1.0.0", meta["version"])
    }

    @Test
    fun `handleGenericException should return 500 with polite error message`() {
        // Given
        val exception = RuntimeException("Something went wrong")

        // When
        val response = exceptionHandler.handleGenericException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode.value())

        val body = response.body
        assertNotNull(body)
        assertTrue(body!!.containsKey("error"))
        assertTrue(body.containsKey("meta"))

        val error = body["error"] as Map<*, *>
        assertEquals("INTERNAL_SERVER_ERROR", error["code"])
        assertEquals(
            "An unexpected error occurred. Our team has been notified and is working to resolve the issue. Please try again later.",
            error["message"],
        )
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), error["status"])

        val meta = body["meta"] as Map<*, *>
        assertTrue(meta.containsKey("timestamp"))
        assertEquals("1.0.0", meta["version"])
    }
}
