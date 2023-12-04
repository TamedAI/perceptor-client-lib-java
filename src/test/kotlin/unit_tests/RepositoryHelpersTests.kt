package unit_tests

import org.tamedai.perceptorclient.SseEvent
import org.tamedai.perceptorclient.concatUrl
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.parseSseEvents
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryHelpersTests{

    @TestFactory
    fun testConcatUrl() = listOf(
        Pair("first", "second") to "first/second",
        Pair("first/", "second") to "first/second",
        Pair("first", "/second") to "first/second",
        Pair("", "/second") to "/second",
        Pair("first/", "") to "first/",
        Pair("first", "") to "first",
        Pair("", "") to "",
    )
        .map{(input, expected) ->
            DynamicTest.dynamicTest("GIVEN (${input.first},${input.second}) WHEN Concatenated THEN Should Be '$expected'") {
                assertEquals(expected, concatUrl(input.first, input.second))
            }
        }

    @TestFactory
    fun testSseEventParser()= listOf(
        "" to emptyList<SseEvent>(),
        """
event: waiting
data: 

event: waiting
data: 

event: generate
data: Hans

event: generate
data: Hans Helvetia

event: finished
data: Hans Helvetia
        """.trimIndent() to listOf(SseEvent("Hans Helvetia")),

       """
           event: finished
           data: First Answer

           event: finished
           data: Another answer
       """.trimIndent() to listOf(
           SseEvent("First Answer"),
           SseEvent("Another answer")
       )
    ).map{(input, expected) ->
        DynamicTest.dynamicTest("GIVEN (${input} WHEN MappedToSseEvents THEN Should Be '$expected'") {
            assertEquals(expected, parseSseEvents(input))
        }
}
}