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

