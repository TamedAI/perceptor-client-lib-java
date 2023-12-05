package unit_tests

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.TestInstance
import org.tamedai.perceptorclient.mapResponseToStructuredContent
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StructuredResponseParserTests {

    @Test
    fun given_empty_structuredResponse_THEN_text_should_be_empty(){
        val toParse = " "
        val parsed = mapResponseToStructuredContent(toParse)
        parsed["text"] shouldBe ""
    }

    @Test
    fun given_plainTextResponse_THEN_text_should_be_response(){
        val toParse = " some unstructured text "
        val parsed = mapResponseToStructuredContent(toParse)
        parsed["text"] shouldBe toParse
    }

    @Test
    fun given_structuredResponse_with_text_THEN_text_should_be_found(){
        val expectedAnswerText = "some_answer_text"
        val toParse = """{"text":"$expectedAnswerText","scores":{"x":100}}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed.shouldNotBeEmpty()
        parsed shouldHaveKey "text"
        parsed["text"] shouldBe expectedAnswerText
    }

    @Test
    fun given_structuredResponse_with_scores_only_THEN_text_should_be_empty(){
        val toParse = """{"scores": {"score": 0.19778291881084442, "n_tokens": 5}}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed.shouldNotBeEmpty()
        parsed["text"] shouldBe ""
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun given_structuredResponse_with_scores_THEN_scores_should_be_found(){
        val toParse = """{"text":"", "scores": {"score": 0.19, "n_tokens": 5}}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed.shouldNotBeEmpty()
        parsed shouldHaveKey "scores"

        (parsed["scores"] is Map<*, *>) shouldBe true
        val scoresMap: Map<String, Any>  = parsed["scores"] as Map<String, Any>
        scoresMap shouldNotBe null
        scoresMap shouldHaveKey "score"
        scoresMap shouldHaveKey "n_tokens"
    }

    @Test
    fun given_structuredResponse_with_3_entries_THEN_all_should_be_found(){
        val toParse = """{"text":"", "scores": {}, "other":[]}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed.keys.size shouldBe 3
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun given_structuredResponse_with_array_THEN_all_should_be_found(){
        val toParse = """{"other":[{"array_element":"el_value"}]}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed shouldHaveKey "other"
        val arrayMap  = parsed["other"] as List<Any>
        arrayMap shouldNotBe null
        arrayMap.shouldNotBeEmpty()
        val firstArrayEntry = arrayMap[0] as Map<String, Any>
        firstArrayEntry shouldHaveKey "array_element"
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun given_structuredResponse_with_values_array_THEN_all_should_be_found(){
        val toParse = """{"scores": [3.1, 4.3, 5.6]}"""
        val parsed = mapResponseToStructuredContent(toParse)
        parsed shouldHaveKey "scores"
        val arrayMap  = parsed["scores"] as List<Any>
        arrayMap shouldNotBe null
        arrayMap.size shouldBe 3
        arrayMap[0] shouldBe "3.1"
        arrayMap[1] shouldBe "4.3"
        arrayMap[2] shouldBe "5.6"
    }

}