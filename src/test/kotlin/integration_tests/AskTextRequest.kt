package integration_tests

import org.tamedai.perceptorclient.ClientSettings
import org.tamedai.perceptorclient.PerceptorClientFactory
import org.tamedai.perceptorclient.PerceptorError
import org.tamedai.perceptorclient.PerceptorSuccessResult

fun main(args: Array<String>) {
    println("trying to call http service");

    val client = PerceptorClientFactory.createClient(ClientSettings("api.4zAhccIfthXOWkcSzK9bCf",
        "https://perceptor-api.tamed.ai/1/model/",
        java.time.Duration.ofSeconds(30)))

    val textToProcess: String = """
    Ich melde einen Schaden für meinen Kunden Hans. Er hatte einen Schaden durch eine Überschwemmung.
    Er hat Rechnungen in Höhe von 150000 Euro eingereicht. Der Schaden soll in 2 Chargen bezahlt werden.
    Seine  IBAN ist DE02300606010002474689. Versicherungsbeginn war der 01.10.2022. Er ist abgesichert bis 750.000 EUR. Der Ablauf der Versicherung ist der 01.10.2026.
    Der Kunde hat VIP-Kennzeichen und hatte schonmal einen Leitungswasserschaden in Höhe von 3840 Euro.
    Der Schaden ist 2021 aufgetreten. Die Anschrift des Kunden ist: Berliner Straße 56, 60311 Frankfurt am Main.

    Meine Vermittlernumer ist die 090.100.
    """;


//    val payload = RequestPayload(PerceptorRequest("default", emptyMap()),
//        InstructionMethod.Question,
//        InstructionContextData("text", textToProcess),
//        Instruction("Vorname und Nachname des Kunden?"))

    val fut = client.askText(textToProcess, listOf("Vorname und Nachname des Kunden?") )//  r.sendInstruction(payload)
    if (fut.isEmpty()){
        println("No response received")
    }else{
        val firstResp = fut[0].response
        when (firstResp) {
            is PerceptorSuccessResult -> {
                println(firstResp.answer)
            }

            is PerceptorError -> {
                println(firstResp.errorText)
            }

            else -> {
                println("undefined result");
                println(fut);
            }
        }
    }

}
