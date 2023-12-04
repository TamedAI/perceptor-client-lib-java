package unit_tests

import org.tamedai.perceptorclient.Instruction
import org.tamedai.perceptorclient.InstructionContextData
import org.tamedai.perceptorclient.InstructionMethod
import org.tamedai.perceptorclient.RequestPayload
import org.tamedai.perceptorclient.mapToBodyText
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.tamedai.perceptorclient.PerceptorRequest
import kotlin.test.Test
import kotlin.test.assertTrue
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
        val req: RequestPayload =
            RequestPayload(
                PerceptorRequest(someFlavor, emptyMap()),
                InstructionMethod.Question,
                someContextData,
                Instruction("not relevant")
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        assertTrue(mapped.contains("\"flavor\":\"$someFlavor\""), "$someFlavor should be present in: '$mapped'")
    }

    @Test
    fun instructionShouldBeMapped(){
        val someInstruction = "some-instruction-text"
        val req: RequestPayload =
            RequestPayload(
                PerceptorRequest("not relevant", emptyMap()),
                InstructionMethod.Question,
                someContextData,
                Instruction(someInstruction)
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        assertTrue(mapped.contains("\"instruction\":\"$someInstruction\""), "$someInstruction should be present in: '$mapped'")
    }

    @Test
    fun waitTimeoutShouldBeMapped(){
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            someContextData, Instruction("not relevant")
        )

        val numberOfSeconds = 47
        val duration: Duration = numberOfSeconds.seconds
        val mapped = mapToBodyText(req, duration)
        assertTrue(mapped.contains("\"waitTimeout\":$numberOfSeconds"), "$numberOfSeconds should be present in: '$mapped'")
    }

    @Test
    fun detailedParametersShouldBeMapped(){
        val detailedParmetersMap = mapOf("first" to "val1",
            "second" to "val2")
        val req: RequestPayload =
            RequestPayload(
                PerceptorRequest("not relevant", detailedParmetersMap),
                InstructionMethod.Question,
                someContextData,
                Instruction("not relevant")
            )
        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        assertTrue(mapped.contains("\"first\":\"val1\""))
        assertTrue(mapped.contains("\"second\":\"val2\""))
    }

    @Test
    fun contextDataShouldBeMapped(){
        val contextData =
            InstructionContextData("some-type", "some-content-text")
        val req = RequestPayload(
            PerceptorRequest("not relevant", emptyMap()),
            InstructionMethod.Question,
            contextData, Instruction("not relevant")
        )

        val notRelevantDuration: Duration = 1.minutes
        val mapped = mapToBodyText(req, notRelevantDuration)
        assertTrue(mapped.contains("\"contextType\":\"${contextData.contextType}\""), "${contextData.contextType} should be present in: '$mapped'")
        assertTrue(mapped.contains("\"context\":\"${contextData.content}\""), "${contextData.content} should be present in: '$mapped'")
    }

    @Test
    fun longContextTextShouldBeMapped(){
        val textToProcess: String = """
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
            Instruction("Lorem ipsum dolor sit amet, consetetur sadipscing elitr")
        )

        val notRelevantDuration: Duration = 1.minutes

        assertDoesNotThrow { mapToBodyText(req, notRelevantDuration) }
    }
}