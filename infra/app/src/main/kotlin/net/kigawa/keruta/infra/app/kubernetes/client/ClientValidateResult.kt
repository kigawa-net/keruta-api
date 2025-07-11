package net.kigawa.keruta.infra.app.kubernetes.client

sealed interface ClientValidateResult {
    object Success: ClientValidateResult
    data class Error(val message: String): ClientValidateResult
}
