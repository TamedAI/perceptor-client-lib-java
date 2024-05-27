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
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.input_mapping.InstructionContextImageMapper
import org.tamedai.perceptorclient.input_mapping.InstructionContextImageMapper.isValidFileType
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstructionContextImageMapperTests{

    // Test to ensure, that the file of the path can be edited (here: deleted) after the mapFromFile call.
    @Test
    fun given_FilePath_toCopyOfFile_WHEN_MappedToInstructionContext_THEN_copyCanBeDeleted(){
        val path = "src/test/kotlin/test-files/binary_file.png"
        val tempDir = Files.createTempDirectory("tempDir").toFile()
        val tempFile = File(tempDir, "binary_file.png")
        try {
            // Make a temporary copy of the file
            Files.copy(File(path).toPath(), tempFile.toPath())

            val result = InstructionContextImageMapper.mapFromFile(tempFile.path)
            result shouldNotBe null
        } finally {
            tempFile.delete() shouldBe true;
        }
    }

    @Test
    fun gIVEN_FilePath_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png"
        val result = InstructionContextImageMapper.mapFromFile(path)
        result.contextType shouldBe "image"
        result.content shouldBe "data:image/png;base64,MXg="
    }

    @Test
    fun gIVEN_FileStream_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png"
        val stream = File(path).inputStream()
        val result = InstructionContextImageMapper.mapFromStream(stream, "png")

        result.contextType shouldBe "image"
        result.content shouldBe "data:image/png;base64,MXg="
    }

    @Test
    fun gIVEN_FileBytes_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png"
        val stream = File(path).inputStream()
        val bytes = stream.readBytes()
        val result = InstructionContextImageMapper.mapFromBytes(bytes, "png")

        result.contextType shouldBe "image"
        result.content shouldBe "data:image/png;base64,MXg="
    }

    @TestFactory
    fun testFileExtension() = listOf(
        "jpg" to true,
        "jpeg" to true,
        "png" to true,
        "JPG" to true,
        "JPEG" to true,
        "PNg" to true,
        "" to false,
        "other" to false
    ).map{(input, expected) ->
        DynamicTest.dynamicTest("GIVEN '${input}' WHEN isValidFileType THEN Should Be '$expected'") {
            input.isValidFileType() shouldBe expected
        }
    }
}