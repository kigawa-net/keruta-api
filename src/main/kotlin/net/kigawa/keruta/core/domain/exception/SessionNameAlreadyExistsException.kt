package net.kigawa.keruta.core.domain.exception

/**
 * Exception thrown when attempting to create a session with a name that already exists.
 */
class SessionNameAlreadyExistsException(
    sessionName: String,
) : RuntimeException("Session with name '$sessionName' already exists")
