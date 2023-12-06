package unit_tests

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.tamedai.perceptorclient.*
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestBodyMappingTests{
    private val someContextData: InstructionContextData =
        InstructionContextData("text", "some-text")
    @Test
    fun flavorShouldBeMapped(){
        val someFlavor = "some-value"
        val req =
            RequestPayload(
                PerceptorRequest(someFlavor, emptyMap()),
                InstructionMethod.Question,
                someContextData,
                Instruction("not relevant"),
                listOf()
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        mapped shouldContain "\"flavor\":\"$someFlavor\""
    }

    @Test
    fun instructionShouldBeMapped(){
        val someInstruction = "some-instruction-text"
        val req =
            RequestPayload(
                PerceptorRequest("not relevant", emptyMap()),
                InstructionMethod.Question,
                someContextData,
                Instruction(someInstruction),
                listOf()
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        mapped shouldContain "\"instruction\":\"$someInstruction\""
    }

    @Test
    fun waitTimeoutShouldBeMapped(){
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            someContextData, Instruction("not relevant"),
            listOf()
        )

        val numberOfSeconds = 47
        val duration: Duration = numberOfSeconds.seconds
        val mapped = mapToBodyText(req, duration)
        mapped shouldContain "\"waitTimeout\":$numberOfSeconds"
    }

    @Test
    fun detailedParametersShouldBeMapped(){
        val detailedParmetersMap = mapOf("first" to "val1",
            "second" to "val2")
        val req =
            RequestPayload(
                PerceptorRequest("not relevant", detailedParmetersMap),
                InstructionMethod.Question,
                someContextData,
                Instruction("not relevant"),
                listOf()
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        mapped shouldContain "\"first\":\"val1\""
        mapped shouldContain "\"second\":\"val2\""
    }

    @TestFactory
    fun returnScoresShouldBeMapped() = listOf(
        true to "true",
        false to "false"
    ).map { (input, expected)->
        DynamicTest.dynamicTest("GIVEN (${input} WHEN mappingToBody THEN body should contain '$expected'") {
            val req =
                RequestPayload(
                    PerceptorRequest("not relevant", mapOf("first" to "val1"), returnScores = input),
                    InstructionMethod.Question,
                    someContextData,
                    Instruction("not relevant"),
                    listOf()
                )
            val notRelevantDuration: Duration = 1.minutes
            val mapped = mapToBodyText(req, notRelevantDuration)
            mapped shouldContain "\"returnScores\":\"$expected\""
        }
    }


    @Test
    fun contextDataShouldBeMapped(){
        val contextData =
            InstructionContextData("some-type", "some-content-text")
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            contextData, Instruction("not relevant"),
            listOf()
        )

        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        mapped shouldContain "\"contextType\":\"${contextData.contextType}\""
        mapped shouldContain "\"context\":\"${contextData.content}\""
    }

    @Test
    fun when_method_is_classify_THEN_classesShouldBeMapped(){
        val classEntry1 = "class_entry_1"
        val classEntry2 = "class_entry_2"
        val classifyEntries = listOf(ClassificationEntry(classEntry1),
            ClassificationEntry(classEntry2)
        )
        val contextData = InstructionContextData("some-type", "some-content-text")
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Classify,
            contextData,
            Instruction("not relevant"),
            classifyEntries
        )

        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)

        mapped shouldContain "\"classes\":[\"${classEntry1}\",\"${classEntry2}\"]"
    }

    @Test
    fun when_method_is_not_classify_THEN_classesShouldNotBeMapped(){
        val classEntry1 = "class_entry_1"
        val classEntry2 = "class_entry_2"
        val classifyEntries = listOf(ClassificationEntry(classEntry1),
            ClassificationEntry(classEntry2)
        )
        val contextData = InstructionContextData("some-type", "some-content-text")
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            contextData,
            Instruction("not relevant"),
            classifyEntries
        )

        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)

        mapped shouldNotContain "\"classes\""
    }

    @Test
    fun longContextTextShouldBeMapped(){
        val textToProcess = """
Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,
 sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, 
 no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
 At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
    """;
        val contextData = InstructionContextData("text", textToProcess)
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            contextData,
            Instruction("Lorem ipsum dolor sit amet, consetetur sadipscing elitr"),
            listOf()
        )

        val notRelevantDuration: Duration = 1.minutes
        { mapToBodyText(req, notRelevantDuration)}.shouldNotThrow()

    }

}