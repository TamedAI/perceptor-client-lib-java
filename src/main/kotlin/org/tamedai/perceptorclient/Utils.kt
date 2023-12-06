package org.tamedai.perceptorclient

class Utils

fun groupByInstruction(inputList: List<DocumentImageResult>): List<InstructionWithPageResult> {
    if (inputList.isEmpty()) {
        return listOf()
    }

    fun toDocPageWithResult(inst: String, docImageResult: DocumentImageResult): List<DocumentPageWithResult> =
        docImageResult.results.filter { it.instruction == inst }
            .take(1)
            .map { DocumentPageWithResult(docImageResult.pageIndex, it.isSuccess, it.response, it.errorText) }


    val instructions = inputList[0].results.map { it.instruction }

    return instructions
        .map {
            InstructionWithPageResult(
                it,
                inputList.map { x -> toDocPageWithResult(it, x) }.flatten()
            )
        }

}