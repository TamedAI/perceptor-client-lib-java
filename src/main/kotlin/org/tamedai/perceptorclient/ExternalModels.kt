package org.tamedai.perceptorclient

interface IPerceptorInstructionResult{}


data class PerceptorError(val errorText: String): IPerceptorInstructionResult
data class PerceptorSuccessResult(val answer: String): IPerceptorInstructionResult

data class PerceptorRequest(val flavour: String, val detailedParameters: Map<String, String>){
    companion object Factory {
        private val defaultRequest: PerceptorRequest = PerceptorRequest("original", emptyMap())
        fun default() = defaultRequest
    }
}




data class InstructionWithResponse(val instruction: String, val response: IPerceptorInstructionResult)

data class ClientSettings(var apiKey:String, var url: String = "https://perceptor-api.tamed.ai/1/model/", var waitTimeout: java.time.Duration = java.time.Duration.ofSeconds(30))

