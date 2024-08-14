package integration_tests

import io.kotest.matchers.shouldBe
import org.tamedai.perceptorclient.ClientSettings
import org.tamedai.perceptorclient.PerceptorClientFactory
import org.tamedai.perceptorclient.PerceptorRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class AskImageWithManyInstructionsTest {

    /**
     * Tests that proves that 84 instructions will give a result and does not end in a timeout due to a mutlithreading
     * deadlock. The count of 84 (or more) instructions comes from the default Dispatchers.IO max. thread size of 64 and the
     * PerceptorClient property maxNumberOfParallelRequests of 10.
     *
     * When 84 or more instructions are given, the ThreadPool of Dispatcher.IO will have 64 executing Threads
     * + 20 in the queue. 10 (due to maxNumberOfParallelRequests) of the 64 will proceed, using the same thread to
     * execute the http request to the perceptor. When these 10 are done, the next 10 of the 74 acquire the semaphore
     * and can start their processing, but they will never get to execute their http requests, because another 64 are in
     * front of the ThreadPool queue, which are blocked because they are waiting to acquire the semaphore.
     */
    @Test
    fun given_maxNumberOfParallelRequests_is10_when84Instructions_givesResults() {
        val client = PerceptorClientFactory.createClient(
            ClientSettings(
                apiKey = "api.4zAhccIfthXOWkcSzK9bCf",
                url = "https://perceptor-api.tamed.ai/1/model/",
                maxNumberOfParallelRequests = 10
            )
        )

        val imagePath = "src/test/kotlin/test-files/invoice.jpg"

        val instructions = ArrayList<String>()
        for (i in 1 until 85) {
            instructions.add("Prompt" + i)
        }

        val result = CompletableFuture.supplyAsync {
            client.askImage(
                imagePath, PerceptorRequest.withFlavor("original"), instructions
            )
        }.get(100, TimeUnit.SECONDS)

        result.size shouldBe 84
        val firstResponse = result[0]
        firstResponse.isSuccess shouldBe true
    }
}
