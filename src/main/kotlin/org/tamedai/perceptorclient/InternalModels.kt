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
package org.tamedai.perceptorclient

import kotlin.time.Duration

internal data class InstructionContextData(val contextType: String, val content: String){
    companion object Factory {
        fun forText(textContent: String) = InstructionContextData("text", textContent)
    }
}

internal enum class InstructionMethod{
    Question,
    Table,
    Classify
}

internal data class RequestPayload(val parameters: PerceptorRequest,
                          val method: InstructionMethod,
                          val contextData: InstructionContextData,
                          val instruction: Instruction,
                          val classifyEntries: List<ClassificationEntry>
)

@JvmInline
internal value class Instruction(val text: String)

@JvmInline
internal value class ClassificationEntry(val value: String)

internal data class HttpClientSettings(val apiKey: String, val url: String, val waitTimeout: Duration)


@JvmInline
internal value class SseEvent(val value:String)

internal interface IPerceptorInstructionResult

internal data class PerceptorError(val errorText: String, val isRetryable: Boolean) : IPerceptorInstructionResult

internal data class PerceptorSuccessResult(val answer: String) : IPerceptorInstructionResult

internal object ErrorConstants {
    val invalidApiKey = PerceptorError("invalid api key", false)
    val notFound = PerceptorError("not found", false)
    val unknownError = PerceptorError("unknown error", true)
}
