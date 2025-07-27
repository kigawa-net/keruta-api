package net.kigawa.keruta.core.usecase.executor

import net.kigawa.keruta.core.domain.model.CoderTemplate

/**
 * Interface for communicating with keruta-executor service.
 */
interface ExecutorClient {
    /**
     * Fetches Coder templates from the executor service.
     */
    fun getCoderTemplates(): List<CoderTemplate>

    /**
     * Fetches a specific Coder template from the executor service.
     */
    fun getCoderTemplate(id: String): CoderTemplate?
}
