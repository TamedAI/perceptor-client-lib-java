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

import io.mockk.coEvery
import org.junit.jupiter.api.TestInstance
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.repository.IPerceptorRepository
import kotlin.test.Test
import io.kotest.matchers.*
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContentSessionContextTests{
    private val repositoryMock: IPerceptorRepository =mockk<IPerceptorRepository>()
    private val taskLimiter: TaskMapperService = TaskMapperService(5, requestDelayFactor = 0)

    private suspend fun setupRepositoryMockResult(response: IPerceptorInstructionResult){
        coEvery { repositoryMock.sendInstruction(any()) } returns response
    }
    private fun createContext(instrContext: InstructionContextData): ContentSessionContext =
        ContentSessionContext(repositoryMock, taskLimiter, instrContext)

    private fun InstructionResponse.getResponseText()=this["text"]

    @Test
    fun successfulResponseIsMapped(){
        val expectedAnswer = "answer value"
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val context = createContext(InstructionContextData("text", "some text"))
            val instruction = Instruction("some instruction")

            val r = context.processInstructionsRequest(
                PerceptorRequest.withFlavor("some_flavor"),
                InstructionMethod.Question,
                listOf(instruction),
                listOf()
            )

            r.shouldNotBeEmpty()
            val firstResponse = r[0]

            firstResponse.isSuccess.shouldBeTrue()
            firstResponse.response?.getResponseText() shouldBe expectedAnswer
        }

    }

    @Test
    fun manyResponsesAreMapped(){
        val expectedAnswer = "answer value"
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val context = createContext(InstructionContextData("text", "some text"))

            val instructions = (1..10).map { i-> Instruction("some instruction $i") }
                .toList()

            val resultList = context.processInstructionsRequest(PerceptorRequest.withFlavor("some_flavor"),
                InstructionMethod.Question,
                instructions,
                listOf()
            )

            instructions.count() shouldBe resultList.count()

            resultList.forEach{
                it.isSuccess.shouldBeTrue()
                it.response?.getResponseText() shouldBe expectedAnswer
            }

        }

    }

     @Test
     fun when_method_classify_and_number_classes_less_than_2_THEN_exception_is_raised() {
         val listOfOneClassOnly = listOf("some_class").map { x -> ClassificationEntry(x) }

         runBlocking {
             setupRepositoryMockResult(PerceptorSuccessResult("not relevant"))
             val context = createContext(InstructionContextData("text", "some text"))

             val toCall: () -> Unit = {
                 runBlocking {
                     context.processInstructionsRequest(
                         PerceptorRequest.withFlavor("some_flavor"),
                         InstructionMethod.Classify,
                         listOf(Instruction("some_instruction")),
                         listOfOneClassOnly
                     )
                 }
             }

             val exc = toCall.shouldThrow()
             exc.shouldBeTypeOf<IllegalArgumentException>()
         }

     }
}