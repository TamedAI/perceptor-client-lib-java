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
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.SseEvent
import org.tamedai.perceptorclient.concatUrl
import org.tamedai.perceptorclient.mapBadRequestContentString
import org.tamedai.perceptorclient.parseSseEvents

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryHelpersTests {

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
        .map { (input, expected) ->
            DynamicTest.dynamicTest("GIVEN (${input.first},${input.second}) WHEN Concatenated THEN Should Be '$expected'") {
                concatUrl(input.first, input.second) shouldBe expected
            }
        }

    @TestFactory
    fun testSseEventParser() = listOf(
        "" to emptyList(),
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
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("GIVEN (${input} WHEN MappedToSseEvents THEN Should Be '$expected'") {
            parseSseEvents(input) shouldBe expected
        }
    }

    @TestFactory
    fun testParseBadResponse() = listOf(
        """{"detail":"detail_text"}""" to "detail_text",
        """   {"detail" : "detail_text"}""" to "detail_text",
        """{"detail":["detail_text"]}""" to "detail_text",
        """ {"detail":  ["text1","text2"]}""" to "text1, text2",
        """ {"other":  ["text1"]}""" to """ {"other":  ["text1"]}""",
        "other response" to "other response",
        "" to ""
    ).map { (input, expected)->
        DynamicTest.dynamicTest("GIVEN (${input} WHEN Parsing THEN Should Be '$expected'") {
            mapBadRequestContentString(input) shouldBe expected
        }
    }

}

