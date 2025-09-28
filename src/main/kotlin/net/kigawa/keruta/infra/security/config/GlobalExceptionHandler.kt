package net.kigawa.keruta.infra.security.config

import jakarta.validation.ConstraintViolationException
import net.kigawa.keruta.api.common.dto.ErrorResponse
import net.kigawa.keruta.api.generated.ApiException
import net.kigawa.keruta.api.generated.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.NoSuchElementException

/**
 * Global exception handler for the application.
 * Provides standardized error responses for different types of exceptions.
 * Ordered with high precedence to override generated exception handlers.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
open class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handles NoSuchElementException which occurs when a requested resource is not found.
     * Returns a 404 Not Found response with a detailed error message.
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.message ?: "The requested resource could not be found. Please check your input and try again."
        val path = getRequestPath(request)

        logger.error("404 Not Found: {} - Path: {}", errorMessage, path)

        val errorResponse = ErrorResponse.create(
            code = "RESOURCE_NOT_FOUND",
            message = errorMessage,
            status = HttpStatus.NOT_FOUND.value(),
            path = path,
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Handles IllegalArgumentException which occurs when invalid input is provided.
     * Returns a 400 Bad Request response with a detailed error message.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.message ?: "Invalid input provided. Please check your request parameters and try again."
        val path = getRequestPath(request)

        logger.error("400 Bad Request: {} - Path: {}", errorMessage, path)

        val errorResponse = ErrorResponse.create(
            code = "INVALID_INPUT",
            message = errorMessage,
            status = HttpStatus.BAD_REQUEST.value(),
            path = path,
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles validation errors for method arguments.
     * Returns a 400 Bad Request response with validation details.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)
        val errors = ex.bindingResult.fieldErrors.associate { fieldError: FieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Invalid value")
        }

        logger.warn("400 Validation Error: {} - Path: {}", errors, path)

        val errorResponse = ErrorResponse.create(
            code = "VALIDATION_ERROR",
            message = "Validation failed for one or more fields",
            status = HttpStatus.BAD_REQUEST.value(),
            path = path,
            details = mapOf("fieldErrors" to errors),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles constraint violation exceptions from bean validation.
     * Returns a 400 Bad Request response with constraint details.
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)
        val violations = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }

        logger.warn("400 Constraint Violation: {} - Path: {}", violations, path)

        val errorResponse = ErrorResponse.create(
            code = "CONSTRAINT_VIOLATION",
            message = "Constraint validation failed",
            status = HttpStatus.BAD_REQUEST.value(),
            path = path,
            details = mapOf("violations" to violations),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles type mismatch exceptions for method arguments.
     * Returns a 400 Bad Request response with type error details.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)
        val paramName = ex.name
        val expectedType = ex.requiredType?.simpleName ?: "unknown"
        val actualValue = ex.value

        logger.warn(
            "400 Type Mismatch: parameter '{}' expected type '{}' but got '{}' - Path: {}",
            paramName,
            expectedType,
            actualValue,
            path,
        )

        val errorResponse = ErrorResponse.create(
            code = "TYPE_MISMATCH",
            message = "Parameter '$paramName' has invalid type. Expected: $expectedType",
            status = HttpStatus.BAD_REQUEST.value(),
            path = path,
            details = mapOf(
                "parameter" to paramName,
                "expectedType" to expectedType,
                "actualValue" to (actualValue?.toString() ?: "null"),
            ),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles malformed JSON request body.
     * Returns a 400 Bad Request response with parsing error details.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)

        logger.warn("400 Message Not Readable: {} - Path: {}", ex.message, path)

        val errorResponse = ErrorResponse.create(
            code = "MALFORMED_REQUEST",
            message = "Request body could not be parsed. Please check your JSON format.",
            status = HttpStatus.BAD_REQUEST.value(),
            path = path,
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles ApiException from generated code.
     * Returns appropriate HTTP status with standardized error response.
     */
    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        ex: ApiException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)
        val httpStatus = HttpStatus.valueOf(ex.code)

        logger.warn("{} API Exception: {} - Path: {}", ex.code, ex.message, path)

        val errorResponse = ErrorResponse.create(
            code = when (ex) {
                is NotFoundException -> "RESOURCE_NOT_FOUND"
                else -> "API_ERROR"
            },
            message = ex.message ?: "API error occurred",
            status = ex.code,
            path = path,
        )

        return ResponseEntity.status(httpStatus).body(errorResponse)
    }

    /**
     * Handles NotImplementedError for unimplemented functionality.
     * Returns a 501 Not Implemented response.
     */
    @ExceptionHandler(NotImplementedError::class)
    fun handleNotImplementedError(
        ex: NotImplementedError,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = getRequestPath(request)

        logger.warn("501 Not Implemented: {} - Path: {}", ex.message, path)

        val errorResponse = ErrorResponse.create(
            code = "NOT_IMPLEMENTED",
            message = ex.message ?: "This functionality is not yet implemented",
            status = HttpStatus.NOT_IMPLEMENTED.value(),
            path = path,
        )

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse)
    }

    /**
     * Handles all other exceptions that are not specifically handled.
     * Returns a 500 Internal Server Error response with a generic error message.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = "An unexpected error occurred. Our team has been notified and is working to resolve the issue. Please try again later."
        val path = getRequestPath(request)

        // Log the 500 error to the console with the request path
        logger.error("500 Internal Server Error: {} - Path: {}", ex.message, path, ex)

        val errorResponse = ErrorResponse.create(
            code = "INTERNAL_SERVER_ERROR",
            message = errorMessage,
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path = path,
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
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
