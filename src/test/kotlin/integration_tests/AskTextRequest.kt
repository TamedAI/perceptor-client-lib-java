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

import org.tamedai.perceptorclient.*

fun main() {
    val client = PerceptorClientFactory.createClient(
        clientSettings
    )

    val textToProcess = """
Ich melde einen Schaden für meinen Kunden Hans Mustermann. Er hatte einen Schaden durch eine Überschwemmung. 
Er hat Rechnungen in Höhe von 150000 Euro eingereicht. Der Schaden soll in 2 Chargen bezahlt werden. 
Seine  IBAN ist DE02300606010002474689. Versicherungsbeginn war der 01.10.2022. Er ist abgesichert bis 750.000 EUR. Der Ablauf der Versicherung ist der 01.10.2026. 
Der Kunde hat VIP-Kennzeichen und hatte schonmal einen Leitungswasserschaden in Höhe von 3840 Euro. 
Der Kunde möchte eine Antwort heute oder morgen erhalten. 
Der Schaden ist 2021 aufgetreten. Die Anschrift des Kunden ist: Leipzigerstr. 12, 21390 Bonn.
Für Rückfragen möchte ich per Telefon kontaktiert werden. Es ist eine dringende Angelegenheit.
Meine Vermittlernumer ist die 090.100.
    """


    val fut = client.askText(
        textToProcess,
        PerceptorRequest.withFlavor("original").copy(returnScores = true),
        listOf(
            "Vorname und Nachname des Kunden?",
            "Ist der Kunde ein VIP? (Ja oder nein)",
            "was ist die IBAN?",
            "Wie hoch sind seine Rechnungen?",
            "Ist er abgesichert?",
            "wann läuft die Versicherung ab?",
            "wie wiele Chargen?",
            "wie ist der Schaden entstanden?",
            "wie lautet die Anschrift?",
            "die Vermittlernummer?",
            "hatte er schon mal Schaden?",
            "wann will der Kunde Antwort?",
            "wie soll ich kontaktiert werden?",
            "ist es dringend?"
        )
    )

    if (fut.isEmpty()) {
        println("No response received")
    } else {

        fut.forEach { res->
            print(res.instruction)
            print(":\t")

            when (res.isSuccess) {
                true -> {
                    println(res.response)
                }
                else -> {
                    print("error:")
                    println(res.errorText)
                }
            }
        }
    }

}
