package org.tamedai.perceptorclient

import org.tamedai.perceptorclient.Instruction
import org.tamedai.perceptorclient.InstructionContextData
import org.tamedai.perceptorclient.InstructionMethod
import org.tamedai.perceptorclient.RequestPayload
import org.tamedai.perceptorclient.TaskMapperService
import kotlinx.coroutines.coroutineScope
import org.tamedai.perceptorclient.repository.IPerceptorRepository

internal class ContentSessionContext(
    private val repository: IPerceptorRepository,
    private val taskMapper: TaskMapperService,
    private val contextData: InstructionContextData
) {

    suspend fun processInstructionsRequest(
        request: PerceptorRequest, method: InstructionMethod,
        instructions: List<Instruction>
    ): List<InstructionWithResponse> = coroutineScope {

        if (instructions.isEmpty())
            return@coroutineScope listOf()

        suspend fun processSingleInstruction(instruction: Instruction): Pair<Instruction, IPerceptorInstructionResult> {

            val payload = RequestPayload(
                request,
                method,
                contextData,
                instruction
            )
            val resp = repository.sendInstruction(payload)
            return Pair(instruction, resp);
        }

        taskMapper.transformList(instructions) { i ->
            val res = processSingleInstruction(i)
            InstructionWithResponse(res.first.text, res.second)
        }.toList()


    }
}