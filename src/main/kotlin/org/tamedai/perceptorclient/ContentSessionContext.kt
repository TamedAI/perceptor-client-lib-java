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