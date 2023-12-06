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

internal interface IPerceptorInstructionResult {}

internal data class PerceptorError(val errorText: String, val isRetryable: Boolean) : IPerceptorInstructionResult

internal data class PerceptorSuccessResult(val answer: String) : IPerceptorInstructionResult

internal object ErrorConstants {
    val invalidApiKey = PerceptorError("invalid api key", false)
    val notFound = PerceptorError("not found", false)
    val unknownError = PerceptorError("unknown error", true)
}
