package org.tamedai.perceptorclient.repository

import kotlinx.coroutines.delay
import org.tamedai.perceptorclient.IPerceptorInstructionResult
import org.tamedai.perceptorclient.PerceptorError
import org.tamedai.perceptorclient.RequestPayload


internal class PerceptorRepositoryRetryDecorator(
    private val decoree: IPerceptorRepository,
    private val maxAttempts: Int
) : IPerceptorRepository {

    private val waitDuration: Long = 100

    private fun shouldRetry(r: IPerceptorInstructionResult): Boolean {
        if (r is PerceptorError) {
            return r.isRetryable
        }
        return false
    }

    override suspend fun sendInstruction(payload: RequestPayload): IPerceptorInstructionResult {
        return retryIO({ r -> shouldRetry(r) }, maxAttempts, waitDuration) {
            decoree.sendInstruction(payload)
        }

    }
}

private suspend fun <T> retryIO(
    checkShouldRepeat: (T) -> Boolean,
    times: Int,
    initialDelay: Long = 100, // 0.1 seconds
    maxDelay: Long = 1000,    // 1 second
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) { attemptNumber ->
        val result = block()
        if (checkShouldRepeat(result)) {
            delay(currentDelay)
            currentDelay = (currentDelay * attemptNumber).coerceAtMost(maxDelay)
        } else {
            return result
        }
    }
    return block() // last attempt
}