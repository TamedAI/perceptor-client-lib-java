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

@Suppress("unused")
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