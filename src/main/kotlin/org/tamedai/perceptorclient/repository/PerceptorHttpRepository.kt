package org.tamedai.perceptorclient.repository


import org.tamedai.perceptorclient.HttpClientSettings
import org.tamedai.perceptorclient.InstructionMethod
import org.tamedai.perceptorclient.RequestPayload
import org.tamedai.perceptorclient.concatUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tamedai.perceptorclient.mapToBodyText
import org.tamedai.perceptorclient.ErrorConstants
import org.tamedai.perceptorclient.IPerceptorInstructionResult
import org.tamedai.perceptorclient.PerceptorSuccessResult
import org.tamedai.perceptorclient.parseSseEvents
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


internal class PerceptorHttpRepository(private val clientSettings: HttpClientSettings) : IPerceptorRepository {

    private val underlyingClient: HttpClient = HttpClient.newHttpClient()
    private val authorizationHeader: String by lazy {
        "Bearer ${clientSettings.apiKey}"
    }

    private val urlForMethodGenerate: URI by lazy {
        URI(concatUrl(clientSettings.url, "generate"))
    }

    private val urlForMethodTable: URI by lazy {
        URI(concatUrl(clientSettings.url, "generate_table"))
    }

    override suspend fun sendInstruction(payload: RequestPayload): IPerceptorInstructionResult {
        val request = mapToRequestParameters(payload)

        val response = withContext(Dispatchers.IO) {
            underlyingClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
        }

        return mapResponse(response)
    }

    private fun mapResponse(httpResponse: HttpResponse<String>): IPerceptorInstructionResult =
        when(httpResponse.statusCode()){
            HttpURLConnection.HTTP_OK  -> mapSuccessfulResponse(httpResponse)
            HttpURLConnection.HTTP_FORBIDDEN -> ErrorConstants.invalidApiKey

            else -> ErrorConstants.unknownError
        }

    private fun mapSuccessfulResponse(httpResponse: HttpResponse<String>): IPerceptorInstructionResult {
        val events = parseSseEvents(httpResponse.body())
        return if (events.isNotEmpty()){
            PerceptorSuccessResult(events[0].value)
        }else{
            ErrorConstants.unknownError // TODO - Perhaps 'invalid content response' ?
        }
    }

    private fun mapToRequestParameters(payload: RequestPayload): HttpRequest = HttpRequest.newBuilder()
        .uri(getUrlForInstructionMethod(payload.method))
        .header("Accept", "text/event-stream")
        .header("Authorization", authorizationHeader)
        .POST( HttpRequest.BodyPublishers.ofString(mapToBodyText(payload, clientSettings.waitTimeout)))
        .build()

    private fun getUrlForInstructionMethod(method: InstructionMethod): URI =
        if (method == InstructionMethod.Question) {
            urlForMethodGenerate
        } else {
            urlForMethodTable
        }


//    fun sendRequest(): CompletableFuture<HttpResponse<String>>? {
//
////        val client = HttpClient()
////
////        runBlocking {
//////            val client = HttpClient(CIO) {
//////                install(HttpCookies)
//////            }
//////            val loginResponse: HttpResponse = client.get("http://0.0.0.0:8080/login")
//////            repeat(3) {
//////                val response: HttpResponse = client.get("http://0.0.0.0:8080/user")
//////                println(response.bodyAsText())
//////            }
//////            client.close()
////
////        }
//
//        val request = HttpRequest.newBuilder()
//            .uri(URI("https://postman-echo.com/get"))
//            .GET()
//            .build()
//
//        val client = HttpClient.newHttpClient();
//        val bodyHandler = null
//        //HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//        val res = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//
//        return res;
//    }

}