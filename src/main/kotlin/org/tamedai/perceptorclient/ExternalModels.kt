package org.tamedai.perceptorclient



typealias InstructionResponse = Map<String, Any>

data class InstructionWithResult(
    /**
     * Original instruction
     */
    val instruction: String,
    /**
     * True if success
     */
    val isSuccess: Boolean,
    /**
     * Response text (if successful)
     */
    val response: InstructionResponse?,
    /**
     * Error text (if unsuccessful)
     */
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

data class DocumentPageWithResult(
    /**
     * Zero based index of the original document's page
     */
    val pageIndex: Int,
    /**
     * True if success
     */
    val isSuccess: Boolean,
    /**
     * Response text (if successful)
     */
    val response: InstructionResponse?,
    /**
     * Error text (if unsuccessful)
     */
    val errorText: String?
)

data class InstructionWithPageResult(
    /**
     * Original instruction
     */
    val instruction: String,
    /**
     * List of results for the page.
     */
    val pageResults: List<DocumentPageWithResult>
)

/*@dataclass


@dataclass
class InstructionWithPageResult:
    """
    Original instruction text
    """
    instruction: str

    """
    Pages and corresponding results
    """
    page_results: list[DocumentPageWithResult]

 */

