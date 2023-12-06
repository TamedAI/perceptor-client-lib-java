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

import org.tamedai.perceptorclient.*
import org.tamedai.perceptorclient.repository.PerceptorHttpRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun main(args: Array<String>) {
    println("Hello World!")

    println("Program arguments: ${args.joinToString()}")

    println("trying to call http service");

    val waitTimeout :Duration = 30.seconds // Duration.seconds(30)
    val clientSettings = HttpClientSettings(apiKey = "api.4zAhccIfthXOWkcSzK9bCf",
        url = "https://perceptor-api.tamed.ai/1/model/",
        waitTimeout = waitTimeout)
    val r = PerceptorHttpRepository(clientSettings);


    val textToProcess: String = """
    Ich melde einen Schaden für meinen Kunden Hans Helvetia. Er hatte einen Schaden durch eine Überschwemmung.
    Er hat Rechnungen in Höhe von 150000 Euro eingereicht. Der Schaden soll in 2 Chargen bezahlt werden.
    Seine  IBAN ist DE02300606010002474689. Versicherungsbeginn war der 01.10.2022. Er ist abgesichert bis 750.000 EUR. Der Ablauf der Versicherung ist der 01.10.2026.
    Der Kunde hat VIP-Kennzeichen und hatte schonmal einen Leitungswasserschaden in Höhe von 3840 Euro.
    Der Schaden ist 2021 aufgetreten. Die Anschrift des Kunden ist: Berliner Straße 56, 60311 Frankfurt am Main.

    Meine Vermittlernumer ist die 090.100.
    """;


    val payload = RequestPayload(
        PerceptorRequest("default", emptyMap()),
        InstructionMethod.Question,
        InstructionContextData("text", textToProcess),
        Instruction("Vorname und Nachname des Kunden?"),
        listOf()
    )

    val fut = r.sendInstruction(payload)

   println(fut);
}
