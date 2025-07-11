package net.kigawa.keruta.core.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Service for managing coroutines in the application.
 */
interface CoroutineService {
    /**
     * Launches a coroutine that executes the given block.
     *
     * @param block The suspend function to execute.
     * @return A Job that can be used to cancel the coroutine.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job

    /**
     * Launches a coroutine that executes the given block and handles any exceptions.
     *
     * @param block The suspend function to execute.
     * @param onError The function to call if an exception is thrown.
     * @return A Job that can be used to cancel the coroutine.
     */
    fun launchWithErrorHandling(
        block: suspend CoroutineScope.() -> Unit,
        onError: (Throwable) -> Unit,
    ): Job
}
