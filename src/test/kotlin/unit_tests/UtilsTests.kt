package unit_tests

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.DocumentImageResult
import org.tamedai.perceptorclient.InstructionWithResult
import org.tamedai.perceptorclient.groupByInstruction
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilsTests {

    private fun createInstructionWithResult(instructionText: String, pageIndex: Int): InstructionWithResult {
        return InstructionWithResult.success(
            instructionText,
            "resp_${instructionText}_page_${pageIndex}"
        )
    }

    private fun createDocumentImageResult(pageIndex: Int, instructions: List<String>): DocumentImageResult {
        return DocumentImageResult(
            pageIndex = pageIndex,
            results = instructions.map { createInstructionWithResult(it, pageIndex) }.toList()
        )
    }

    @Test
    fun gIVEN_multiple_pages_response_WHEN_grouped_THEN_instructions_match() {

        val instructions = listOf("inst_1", "inst_2", "inst_3")
        val toMap = List(instructions.size) { index -> createDocumentImageResult(index, instructions) }.toList()

        val mapped = groupByInstruction(toMap)
        mapped shouldHaveSize instructions.size
        mapped.map { it.instruction }.toList() shouldBe instructions

    }

    @Test
    fun gIVEN_multiple_pages_response_WHEN_grouped_THEN_responses_match() {

        val instructions = listOf("inst_1", "inst_2", "inst_3")
        val toMap = List(5) { index -> createDocumentImageResult(index, instructions) }.toList()

        val allResponses = toMap.map { it.results }.flatten().toList()

        val mapped = groupByInstruction(toMap)

        mapped.shouldAllSatisfy {
            it.pageResults shouldHaveSize toMap.size

            val responsesToCompare = allResponses.filter { x -> x.instruction == it.instruction }

            it.pageResults.map { x -> x.response } shouldBe responsesToCompare.map { x -> x.response }
            it.pageResults.map { x -> x.isSuccess } shouldBe responsesToCompare.map { x -> x.isSuccess }
            it.pageResults.map { x -> x.errorText } shouldBe responsesToCompare.map { x -> x.errorText }
        }
    }

    @Test
    fun gIVEN_empty_list_WHEN_grouped_THEN_empty_list_returned() {
        val mapped = groupByInstruction(listOf())
        mapped shouldHaveSize 0
    }
}