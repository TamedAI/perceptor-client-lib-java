/*
Copyright 2023 TamedAI GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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