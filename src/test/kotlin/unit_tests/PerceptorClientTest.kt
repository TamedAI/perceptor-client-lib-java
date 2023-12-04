package unit_tests
import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.InstructionContextData
import org.tamedai.perceptorclient.TaskMapperService

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.repository.IPerceptorRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PerceptorClientTest {
    private val repositoryMock: IPerceptorRepository = mockk<IPerceptorRepository>()
    private val taskService: TaskMapperService = TaskMapperService(5)

    private val createSessionFunc: (InstructionContextData)->ContentSessionContext = { ctx->
        ContentSessionContext(repositoryMock, taskService, ctx)
    }

    private suspend fun setupRepositoryMockResult(response: IPerceptorInstructionResult){
        coEvery { repositoryMock.sendInstruction(any()) } returns response
    }

    private fun getClient() = PerceptorClient(createSessionFunc)

    private val expectedAnswer = "answer value"
    @Test
    fun given_DefaultParameters_WHEN_AskText_THEN_AnswerIsFound(){
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val result = getClient().askText("to process", listOf("some question"))

            val firstResponse = result[0].response
            assertTrue { firstResponse is PerceptorSuccessResult && firstResponse.answer == expectedAnswer }

            coVerify {repositoryMock.sendInstruction(withArg {
                assertTrue(PerceptorRequest.default() == it.parameters)
            })}
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskText_THEN_AnswerIsFound(){
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request: PerceptorRequest = PerceptorRequest("some flavor",
                mapOf("par1" to "val1")
            )

            val result = getClient().askText("to process", request, listOf("some question"))

            val firstResponse = result[0].response
            assertTrue { firstResponse is PerceptorSuccessResult && firstResponse.answer == expectedAnswer }

            coVerify {repositoryMock.sendInstruction(withArg {
                assertTrue(request == it.parameters)
            })}
        }
    }

    private val imageFilePath = "src/test/kotlin/test-files/binary_file.png"

    @Test
    fun given_DefaultParameters_WHEN_AskImage_FromFile_THEN_AnswerIsFound(){
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val result = getClient().askImage(imageFilePath, listOf("some question"))

            val firstResponse = result[0].response
            assertTrue { firstResponse is PerceptorSuccessResult && firstResponse.answer == expectedAnswer }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskImage_FromFile_THEN_AnswerIsFound(){
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val request: PerceptorRequest = PerceptorRequest("some flavor",
                mapOf("par1" to "val1")
            )
            val result = getClient().askImage(imageFilePath, request, listOf("some question"))

            val firstResponse = result[0].response
            assertTrue { firstResponse is PerceptorSuccessResult && firstResponse.answer == expectedAnswer }

            coVerify {repositoryMock.sendInstruction(withArg {
                assertTrue(request == it.parameters)
            })}
        }
    }

    @Test
    fun given_DefaultParameters_WHEN_AskMultipleImages_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val imagePaths = listOf(imageFilePath, imageFilePath)
            val instructions = listOf("some question", "another question")
            val result = getClient().askDocument(
                imagePaths,
                instructions
            )

            assertEquals(imagePaths.count(), result.count())

            coVerify(exactly = imagePaths.count() * instructions.count()) {
                repositoryMock.sendInstruction(withArg {
                    assertEquals(PerceptorRequest.default(), it.parameters)
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskMultipleImages_FromFile_THEN_AnswerIsFound(){
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request: PerceptorRequest = PerceptorRequest("some flavor",
                mapOf("par1" to "val1")
            )
            val imagePaths = listOf(imageFilePath, imageFilePath)
            val instructions = listOf("some question", "another question")
            val result = getClient().askDocument(
                imagePaths,
                request,
                instructions                )

            assertEquals(imagePaths.count(), result.count())

            coVerify(exactly = imagePaths.count()*instructions.count()) {repositoryMock.sendInstruction(withArg {
                assertTrue(request == it.parameters)
            })}
        }
    }

}

