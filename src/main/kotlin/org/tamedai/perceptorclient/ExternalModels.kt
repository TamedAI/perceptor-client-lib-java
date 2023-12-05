package org.tamedai.perceptorclient



typealias InstructionResponse = Map<String, Any>

data class InstructionWithResult(
    val instruction: String,
    val isSuccess: Boolean,
    val response: InstructionResponse?,
    val errorText: String?
) {
    companion object Factory {
        fun success(instruction: String, response: String) = InstructionWithResult(
            instruction,
            true,
            mapResponseToStructuredContent(response),
            null
        )

        private fun error(instruction: String, errorText: String) = InstructionWithResult(
            instruction,
            false,
            null,
            errorText
        )

        internal fun fromError(instruction: String, err: PerceptorError) = error(instruction, err.errorText)
    }
}

data class PerceptorRequest(
    val flavor: String, val detailedParameters: Map<String, String>,
    val returnScores: Boolean = false
) {
    companion object Factory {
        private val defaultRequest: PerceptorRequest = PerceptorRequest("original", emptyMap(), false)
        fun withFlavor(flavor: String, returnScores: Boolean = false) = defaultRequest.copy(
            flavor = flavor,
            returnScores = returnScores
        )
    }
}

data class DocumentImageResult(
    /**
     * Zero based index of the original document's page
     */
    val pageIndex: Int,
    /**
     * List of results for the page.
     */
    val results: List<InstructionWithResult>
)

