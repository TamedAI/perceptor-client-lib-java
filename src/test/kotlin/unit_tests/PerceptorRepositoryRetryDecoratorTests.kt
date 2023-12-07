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
package unit_tests

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.repository.IPerceptorRepository
import org.tamedai.perceptorclient.repository.PerceptorRepositoryRetryDecorator
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PerceptorRepositoryRetryDecoratorTests{
    private val numberOfAttempts = 4

    private val repositoryMock: IPerceptorRepository = mockk<IPerceptorRepository>()
    private val repositoryDecorator = PerceptorRepositoryRetryDecorator(repositoryMock, numberOfAttempts)

    private suspend fun setupRepositoryMockResult(response: IPerceptorInstructionResult) {
        coEvery { repositoryMock.sendInstruction(any()) } returns response
    }

    private val requestPayload = RequestPayload(
        PerceptorRequest(
            "some flavor",
            mapOf("par1" to "val1")
        ),
        method = InstructionMethod.Question,
        contextData = InstructionContextData("text", "some_content"),
        instruction = Instruction("some instruction"),
        classifyEntries = listOf()
    )

    @Test
    fun when_RetryableError_THEN_ErrorIsMapped(){
        runBlocking {
            val expectedError = PerceptorError("some_error", true)
            setupRepositoryMockResult(expectedError)

            val result = repositoryDecorator.sendInstruction(requestPayload)
            result shouldBe expectedError

            coVerify(exactly = numberOfAttempts) {
                repositoryMock.sendInstruction(withArg {
                    assertEquals(requestPayload, it)
                })
            }
        }
    }

    @Test
    fun when_NonRetryableError_THEN_ErrorIsMapped(){
        runBlocking {
            val expectedError = PerceptorError("some_error", false)
            setupRepositoryMockResult(expectedError)

            val result = repositoryDecorator.sendInstruction(requestPayload)
            result shouldBe expectedError
        }
    }
}