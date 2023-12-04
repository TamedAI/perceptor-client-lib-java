package unit_tests

import org.tamedai.perceptorclient.input_mapping.InstructionContextImageMapper
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstructionContextImageMapperTests{
    @Test
    fun gIVEN_FilePath_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png"
        val result = InstructionContextImageMapper.mapFromFile(path)
        assertEquals("image", result.contextType)
        assertEquals("data:image/png;base64,MXg=", result.content)
    }

    @Test
    fun gIVEN_FileStream_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png";
        val stream = File(path).inputStream()
        val result = InstructionContextImageMapper.mapFromStream(stream, "png")
        assertEquals("image", result.contextType)
        assertEquals("data:image/png;base64,MXg=", result.content)
    }

    @Test
    fun gIVEN_FileBytes_WHEN_MappedToInstructionContext_THEN_IsCorrect(){
        val path = "src/test/kotlin/test-files/binary_file.png";
        val stream = File(path).inputStream()
        val bytes = stream.readBytes()
        val result = InstructionContextImageMapper.mapFromBytes(bytes, "png")
        assertEquals("image", result.contextType)
        assertEquals("data:image/png;base64,MXg=", result.content)
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
            assertEquals(expected, InstructionContextImageMapper.isValidFileType(input))
        }
    }
}