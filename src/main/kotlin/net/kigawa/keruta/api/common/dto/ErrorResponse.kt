package net.kigawa.keruta.api.common.dto

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Standardized error response structure for all API endpoints.
 * Provides consistent error information to clients.
 */
data class ErrorResponse(
    val error: ErrorDetail,
    val meta: ErrorMeta,
) {
    data class ErrorDetail(
        val code: String,
        val message: String,
        val status: Int,
        val details: Map<String, Any>? = null,
    )

    data class ErrorMeta(
        val timestamp: String,
        val version: String,
        val path: String? = null,
    )

    companion object {
        fun create(
            code: String,
            message: String,
            status: Int,
            path: String? = null,
            details: Map<String, Any>? = null,
        ): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = code,
                    message = message,
                    status = status,
                    details = details,
                ),
                meta = ErrorMeta(
                    timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    version = "1.0.0",
                    path = path,
                ),
            )
        }
    }
}
