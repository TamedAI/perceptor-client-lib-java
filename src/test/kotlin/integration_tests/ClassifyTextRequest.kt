package integration_tests

import org.tamedai.perceptorclient.*


fun main() {
    val client = PerceptorClientFactory.createClient(
        clientSettings
    )

    val textToProcess = """
Ich melde einen Schaden für meinen Kunden Hans Helvetia. Er hatte einen Schaden durch eine Überschwemmung. 
Er hat Rechnungen in Höhe von 150000 Euro eingereicht. Der Schaden soll in 2 Chargen bezahlt werden. 
Seine  IBAN ist DE02300606010002474689. Versicherungsbeginn war der 01.10.2022. Er ist abgesichert bis 750.000 EUR. Der Ablauf der Versicherung ist der 01.10.2026. 
Der Kunde hat VIP-Kennzeichen und hatte schonmal einen Leitungswasserschaden in Höhe von 3840 Euro. 
Der Kunde möchte eine Antwort heute oder morgen erhalten. 
Der Schaden ist 2021 aufgetreten. Die Anschrift des Kunden ist: Berliner Straße 56, 60311 Frankfurt am Main.
Für Rückfragen möchte ich per Telefon kontaktiert werden. Es ist eine dringende Angelegenheit.
Meine Vermittlernumer ist die 090.100.
    """;


    val result = client.classifyText(
        textToProcess,
        PerceptorRequest.withFlavor("original").copy(returnScores = true),
        instruction="was ist das für ein Text?",
        classes = listOf(
            "versicherung",
            "Schadenmeldung",
            "letter",
            "brief"
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
