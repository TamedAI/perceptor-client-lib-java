package unit_tests

import io.mockk.coEvery
import org.junit.jupiter.api.TestInstance
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.repository.IPerceptorRepository
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContentSessionContextTests{
    private val repositoryMock: IPerceptorRepository =mockk<IPerceptorRepository>()
    private val taskLimiter: TaskMapperService = TaskMapperService(5)
//    init {
//        val repositoryMock = mockk<IPerceptorRepository>()
//
//    }

    private suspend fun setupRepositoryMockResult(response: IPerceptorInstructionResult){
        coEvery { repositoryMock.sendInstruction(any()) } returns response
    }
    private fun createContext(instrContext: InstructionContextData): ContentSessionContext =
        ContentSessionContext(repositoryMock, taskLimiter, instrContext)

    @Test
    fun successfulResponseIsMapped(){
        val expectedAnswer = "answer value";
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val context = createContext(InstructionContextData("text", "some text"))
            val instruction = Instruction("some instruction")

            val r = context.processInstructionsRequest(
                PerceptorRequest.default(),
                InstructionMethod.Question,
                listOf(instruction)
            )

            assertTrue { r.isNotEmpty() }
            val firstResponse = r[0].response
            assertTrue { firstResponse is PerceptorSuccessResult && firstResponse.answer == expectedAnswer }
        }

    }

    @Test
    fun manyResponsesAreMapped(){
        val expectedAnswer = "answer value";
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val context = createContext(InstructionContextData("text", "some text"))

            val instructions = (1..10).map { i-> Instruction("some instruction ${i}") }
                .toList()

            val resultList = context.processInstructionsRequest(PerceptorRequest.default(),
                InstructionMethod.Question,
                instructions
            )

            assertTrue { instructions.count() == resultList.count() }

            resultList.forEach{
                val resp = it.response
                assertTrue { resp is PerceptorSuccessResult && resp.answer == expectedAnswer }
            }

        }

    }
}