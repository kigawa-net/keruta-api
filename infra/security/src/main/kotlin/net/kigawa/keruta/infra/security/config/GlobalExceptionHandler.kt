package net.kigawa.keruta.infra.security.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException

/**
 * Global exception handler for the application.
 * Provides standardized error responses for different types of exceptions.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handles NoSuchElementException which occurs when a requested resource is not found.
     * Returns a 404 Not Found response with a detailed error message.
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorMessage = ex.message ?: "The requested resource could not be found. Please check your input and try again."
        val path = getRequestPath(request)

        logger.error("404 Not Found: {} - Path: {}", errorMessage, path)

        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            errorMessage
        )
    }

    /**
     * Handles IllegalArgumentException which occurs when invalid input is provided.
     * Returns a 400 Bad Request response with a detailed error message.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorMessage = ex.message ?: "Invalid input provided. Please check your request parameters and try again."
        val path = getRequestPath(request)

        logger.error("400 Bad Request: {} - Path: {}", errorMessage, path)

        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_INPUT",
            errorMessage
        )
    }

    /**
     * Handles all other exceptions that are not specifically handled.
     * Returns a 500 Internal Server Error response with a generic error message.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorMessage = "An unexpected error occurred. Our team has been notified and is working to resolve the issue. Please try again later."
        val path = getRequestPath(request)

        // Log the 500 error to the console with the request path
        logger.error("500 Internal Server Error: {} - Path: {}", ex.message, path, ex)

        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            errorMessage
        )
    }

    /**
     * Creates a standardized error response with the given status, code, and message.
     */
    private fun createErrorResponse(
        status: HttpStatus,
        code: String,
        message: String
    ): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "error" to mapOf(
                "code" to code,
                "message" to message,
                "status" to status.value()
            ),
            "meta" to mapOf(
                "timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "version" to "1.0.0"
            )
        )

        return ResponseEntity.status(status).body(errorResponse)
    }

    /**
     * Extracts the request path from the WebRequest object.
     * If the WebRequest is a ServletWebRequest, it gets the path from the HttpServletRequest.
     * Otherwise, it returns a placeholder.
     */
    private fun getRequestPath(request: WebRequest): String {
        return try {
            if (request is ServletWebRequest) {
                val servletRequest = request.request
                val queryString = servletRequest.queryString
                val path = servletRequest.requestURI

                if (queryString != null && queryString.isNotEmpty()) {
                    "$path?$queryString"
                } else {
                    path
                }
            } else {
                "Unknown path"
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract request path", e)
            "Unknown path"
        }
    }
}
