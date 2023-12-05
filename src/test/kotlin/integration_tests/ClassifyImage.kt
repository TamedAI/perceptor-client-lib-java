package integration_tests

import org.tamedai.perceptorclient.PerceptorClientFactory
import org.tamedai.perceptorclient.PerceptorRequest


fun main() {
    val client = PerceptorClientFactory.createClient(
        clientSettings
    )

    val imagePath = "src/test/kotlin/test-files/invoice.jpg"


    val result = client.classifyImage(
        imagePath,
        PerceptorRequest.withFlavor("original").copy(returnScores = true),
        instruction="was ist das für ein Document?",
        classes = listOf(
            "Rechnung", "Antrag", "Rezept"
        )
    )

    print(result.instruction)
    print(":\t")
    if (result.isSuccess) {
        print("response: ")
        println(result.response)
    } else {
        print("error:")
        println(result.errorText)
    }

}
