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

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.repository.IPerceptorRepository
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PerceptorClientTest {
    private val repositoryMock: IPerceptorRepository = mockk<IPerceptorRepository>()
    private val taskService: TaskMapperService = TaskMapperService(5, requestDelayFactor = 0)

    private val createSessionFunc: (InstructionContextData) -> ContentSessionContext = { ctx ->
        ContentSessionContext(repositoryMock, taskService, ctx)
    }

    private suspend fun setupRepositoryMockResult(response: IPerceptorInstructionResult) {
        coEvery { repositoryMock.sendInstruction(any()) } returns response
    }

    private fun getClient() = PerceptorClient(createSessionFunc)

    private val expectedAnswer = "answer value"

    @Test
    fun given_CustomParameters_WHEN_AskText_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val result = getClient().askText("to process", request, listOf("some question"))

            val firstResponse = result[0]

            firstResponse.isSuccess.shouldBeTrue()
            firstResponse.response?.getResponseText() shouldBe  expectedAnswer

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_ClassifyText_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(""))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val response = getClient().classifyText("to process", request, "some question",
                listOf("class1", "class2")
            )

            response.isSuccess.shouldBeTrue()
            response.response?.getResponseText() shouldBe  ""

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }
    private val imageFilePath = "src/test/kotlin/test-files/binary_file.png"

    private fun InstructionResponse.getResponseText() = this["text"]

    @Test
    fun given_CustomParameters_WHEN_AskImage_FromTemporaryFile_THEN_CopiedFileCanBeDeleted() {
        val tempDir = Files.createTempDirectory("tempDir").toFile()
        val tempFile = File(tempDir, "binary_file.png")
        try {
            // Make a temporary copy of the file
            Files.copy(File(imageFilePath).toPath(), tempFile.toPath())
            runBlocking {
                setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

                val request = PerceptorRequest(
                    "some flavor",
                    mapOf("par1" to "val1")
                )
                val result = getClient().askImage(tempFile.path, request, listOf("some question"))

                val firstResponse = result[0]
                firstResponse.isSuccess shouldBe true
                firstResponse.response?.getResponseText() shouldBe expectedAnswer

                coVerify {
                    repositoryMock.sendInstruction(withArg {
                        assertEquals(request, it.parameters)
                    })
                }
            }
        } finally {
            tempFile.delete() shouldBe true;
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskImage_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val result = getClient().askImage(imageFilePath, request, listOf("some question"))

            val firstResponse = result[0]
            firstResponse.isSuccess shouldBe true
            firstResponse.response?.getResponseText() shouldBe  expectedAnswer

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    assertEquals(request, it.parameters)
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_ClassifyImage_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val result = getClient().classifyImage(imageFilePath, request, "some question",
                listOf("class1", "class2"))

            result.isSuccess shouldBe true
            result.response?.getResponseText() shouldBe  expectedAnswer

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    assertEquals(request, it.parameters)
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskImage_FromStream_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val stream: InputStream = File(imageFilePath).inputStream()

            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val result = getClient().askImage(stream, "png", request, listOf("some question"))

            val firstResponse = result[0]
            firstResponse.isSuccess shouldBe true
            firstResponse.response?.getResponseText() shouldBe  expectedAnswer

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    assertEquals(request, it.parameters)
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskImage_FromBytes_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))

            val bytes = File(imageFilePath).inputStream().readBytes()

            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val result = getClient().askImage(bytes, "png", request, listOf("some question"))

            val firstResponse = result[0]
            firstResponse.isSuccess shouldBe true
            firstResponse.response?.getResponseText() shouldBe  expectedAnswer

            coVerify {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskTableImage_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest("some flavor", mapOf("par1" to "val1"))
            val result = getClient().askTableFromImage(imageFilePath, request, "some question")

            result.isSuccess shouldBe true
            result.response?.getResponseText() shouldBe  expectedAnswer
        }
    }


    @Test
    fun given_CustomParameters_WHEN_AskTableImage_FromStream_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest("some flavor", mapOf("par1" to "val1"))

            val result =
                getClient().askTableFromImage(File(imageFilePath).inputStream(), "png", request, "some question")

            result.isSuccess shouldBe true
            result.response?.getResponseText() shouldBe  expectedAnswer
        }
    }


    @Test
    fun given_CustomParameters_WHEN_AskTableImage_FromBytes_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest("some flavor", mapOf("par1" to "val1"))

            val result = getClient().askTableFromImage(
                File(imageFilePath).inputStream().readBytes(),
                "png",
                request,
                "some question"
            )
            result.isSuccess.shouldBeTrue()
            result.response?.getResponseText() shouldBe expectedAnswer
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskMultipleImages_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val imagePaths = listOf(imageFilePath, imageFilePath)
            val instructions = listOf("some question", "another question", "3rd question", "4th question")

            val result = getClient().askDocumentImagePaths(
                imagePaths,
                request,
                instructions
            )

            assertEquals(imagePaths.count(), result.count())

            assertEquals(result.map { x -> x.pageIndex }.toList(), (0 until imagePaths.count()).toList())

            result.forEach { r ->
                assertEquals(instructions.count(), r.results.count())
            }

            coVerify(exactly = imagePaths.count() * instructions.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskMultipleImages_FromStreams_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val imageStreams = listOf(
                Pair(File(imageFilePath).inputStream(), "png"),
                Pair(File(imageFilePath).inputStream(), "png")
            )
            val instructions = listOf("some question", "another question", "3rd question", "4th question")
            val result = getClient().askDocumentImageStreams(
                imageStreams,
                request,
                instructions
            )

            result shouldHaveSize imageStreams.size

            result.map { x -> x.pageIndex }.toList() shouldBe (0 until imageStreams.count()).toList()

            result shouldAllSatisfy {r->
                r.results shouldHaveSize instructions.size
            }

            coVerify(exactly = imageStreams.count() * instructions.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_AskMultipleImages_FromBytes_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val imageBytes = listOf(
                Pair(File(imageFilePath).inputStream().readBytes(), "png"),
                Pair(File(imageFilePath).inputStream().readBytes(), "png")
            )
            val instructions = listOf("some question", "another question", "3rd question", "4th question")
            val result = getClient().askDocumentImageBytes(
                imageBytes,
                request,
                instructions
            )

            result shouldHaveSize imageBytes.size

            result.map { x -> x.pageIndex }.toList() shouldBe (0 until imageBytes.count()).toList()

            result shouldAllSatisfy { r ->
                r.results shouldHaveSize instructions.size
            }

            coVerify(exactly = imageBytes.count() * instructions.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }
        }
    }

    @Test
    fun given_CustomParameters_WHEN_ClassifyMultipleImages_FromFile_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )
            val imagePaths = listOf(imageFilePath, imageFilePath)
            val instruction = "some question"
            val result = getClient().classifyDocumentImagePaths(
                imagePaths,
                request,
                instruction,
                listOf("class1", "class2")
            )

            result shouldHaveSize imagePaths.size

            result shouldAllSatisfy {r->
                r.results shouldHaveSize 1
                r.results[0].response?.getResponseText() shouldBe expectedAnswer
            }

            coVerify(exactly = imagePaths.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }

        }
    }

    @Test
    fun given_CustomParameters_WHEN_ClassifyMultipleImages_FromStreams_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val imageStreams = listOf(
                Pair(File(imageFilePath).inputStream(), "png"),
                Pair(File(imageFilePath).inputStream(), "png")
            )
            val instruction = "some question"
            val result = getClient().classifyDocumentImageStreams(
                imageStreams,
                request,
                instruction,
                listOf("class1", "class2")
            )

            result shouldHaveSize imageStreams.size

            result shouldAllSatisfy {r->
                r.results shouldHaveSize 1
                r.results[0].response?.getResponseText() shouldBe expectedAnswer
            }

            coVerify(exactly = imageStreams.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }

        }
    }

    @Test
    fun given_CustomParameters_WHEN_ClassifyMultipleImages_FromBytes_THEN_AnswerIsFound() {
        runBlocking {
            setupRepositoryMockResult(PerceptorSuccessResult(expectedAnswer))
            val request = PerceptorRequest(
                "some flavor",
                mapOf("par1" to "val1")
            )

            val imageBytes = listOf(
                Pair(File(imageFilePath).inputStream().readBytes(), "png"),
                Pair(File(imageFilePath).inputStream().readBytes(), "png")
            )
            val instruction = "some question"
            val result = getClient().classifyDocumentImageBytes(
                imageBytes,
                request,
                instruction,
                listOf("class1", "class2")
            )

            result shouldHaveSize imageBytes.size

            result shouldAllSatisfy {r->
                r.results shouldHaveSize 1
                r.results[0].response?.getResponseText() shouldBe expectedAnswer
            }

            coVerify(exactly = imageBytes.count()) {
                repositoryMock.sendInstruction(withArg {
                    request shouldBe it.parameters
                })
            }

        }
    }

}

