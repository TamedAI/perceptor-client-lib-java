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

import kotlinx.coroutines.coroutineScope
import org.tamedai.perceptorclient.repository.IPerceptorRepository

internal class ContentSessionContext(
    private val repository: IPerceptorRepository,
    private val taskMapper: TaskMapperService,
    private val contextData: InstructionContextData
) {

    suspend fun processInstructionsRequest(
        request: PerceptorRequest, method: InstructionMethod,
        instructions: List<Instruction>,
        classifyEntries: List<ClassificationEntry>
    ): List<InstructionWithResult> = coroutineScope {

        if (instructions.isEmpty())
            return@coroutineScope listOf()

        if (method == InstructionMethod.Classify && classifyEntries.count() < 2) {
            throw IllegalArgumentException("number of classes must be > 1")
        }

        suspend fun processSingleInstruction(instruction: Instruction): InstructionWithResult {

            val payload = RequestPayload(
                request,
                method,
                contextData,
                instruction,
                classifyEntries
            )
            return when (val resp = repository.sendInstruction(payload)) {
                is PerceptorSuccessResult -> {
                    InstructionWithResult.success(instruction.text, resp.answer)
                }

                is PerceptorError -> {
                    InstructionWithResult.fromError(instruction.text, resp)
                }

                else -> InstructionWithResult.fromError(instruction.text, ErrorConstants.unknownError)
            }
        }

        taskMapper.transformList(instructions) {
            processSingleInstruction(it)
        }.toList()


    }
}