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
