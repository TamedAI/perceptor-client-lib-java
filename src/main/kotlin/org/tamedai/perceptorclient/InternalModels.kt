package org.tamedai.perceptorclient

import kotlin.time.Duration

internal data class InstructionContextData(val contextType: String, val content: String){
    companion object Factory {
        fun forText(textContent: String) = InstructionContextData("text", textContent)
    }
}

internal enum class InstructionMethod{
    Question,
    Table
}

internal data class RequestPayload(val parameters: PerceptorRequest,
                          val method: InstructionMethod,
                          val contextData: InstructionContextData,
                          val instruction: Instruction
)

@JvmInline
internal value class Instruction(val text: String)

internal data class HttpClientSettings(val apiKey: String, val url: String, val waitTimeout: Duration)


@JvmInline
internal value class SseEvent(val value:String)

object ErrorConstants{ // TODO - perhaps move to another class / file
    val invalidApiKey = PerceptorError("invalid api key")
    val unknownError = PerceptorError("unknown error")
}
