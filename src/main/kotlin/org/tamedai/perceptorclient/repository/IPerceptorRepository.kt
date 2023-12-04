package org.tamedai.perceptorclient.repository

import org.tamedai.perceptorclient.RequestPayload
import org.tamedai.perceptorclient.IPerceptorInstructionResult

internal interface IPerceptorRepository {
    suspend fun sendInstruction(payload: RequestPayload): IPerceptorInstructionResult
}