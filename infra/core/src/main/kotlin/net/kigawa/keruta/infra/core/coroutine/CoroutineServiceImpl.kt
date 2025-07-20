package net.kigawa.keruta.infra.core.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.kigawa.keruta.core.usecase.CoroutineService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of the CoroutineService interface.
 * Uses a SupervisorJob to ensure that failures in one coroutine don't affect others.
 */
@Service
open class CoroutineServiceImpl : CoroutineService {
    private val logger = LoggerFactory.getLogger(CoroutineServiceImpl::class.java)

    // Create a CoroutineScope with a SupervisorJob and Dispatchers.Default
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch {
            block()
        }
    }

    override fun launchWithErrorHandling(
        block: suspend CoroutineScope.() -> Unit,
        onError: (Throwable) -> Unit,
    ): Job {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error("Coroutine failed with exception", throwable)
            onError(throwable)
        }

        return scope.launch(exceptionHandler) {
            block()
        }
    }
}
