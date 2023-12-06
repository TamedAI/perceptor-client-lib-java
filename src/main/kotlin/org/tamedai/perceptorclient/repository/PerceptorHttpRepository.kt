package org.tamedai.perceptorclient.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tamedai.perceptorclient.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ExecutionException


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

    private val urlForMethodClassify: URI by lazy{
        URI(concatUrl(clientSettings.url, "classify"))
    }

    override suspend fun sendInstruction(payload: RequestPayload): IPerceptorInstructionResult {
        val request = mapToRequestParameters(payload)

        val response: HttpResponse<String>
        try {
            response = withContext(Dispatchers.IO) {
                underlyingClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
            }
        } catch (e: Exception) {
            return e.toPerceptorError()
        }

        return response.toPerceptorInstructionResult()
    }

    private fun Exception.toPerceptorError(): PerceptorError {
        val exception = this
        if (exception is ExecutionException) {
            return PerceptorError(exception.cause?.message ?: exception.toString(), true)
        }
        return PerceptorError(exception.message ?: exception.toString(), true)
    }

    private fun HttpResponse<String>.toPerceptorInstructionResult(): IPerceptorInstructionResult =
        when (this.statusCode()) {
            HttpURLConnection.HTTP_OK -> this.toSuccessfulResponse()
            HttpURLConnection.HTTP_FORBIDDEN -> ErrorConstants.invalidApiKey
            HttpURLConnection.HTTP_BAD_REQUEST -> this.toBadRequestResponse()
            HttpURLConnection.HTTP_NOT_FOUND -> ErrorConstants.notFound

            else -> ErrorConstants.unknownError
        }

    private fun HttpResponse<String>.toSuccessfulResponse(): IPerceptorInstructionResult {
        val events = parseSseEvents(this.body())
        return when {
            events.isNotEmpty() -> {
                PerceptorSuccessResult(events[0].value)
            }
            else -> {
                ErrorConstants.unknownError
            }
        }
    }

    private fun HttpResponse<String>.toBadRequestResponse(): PerceptorError =
        PerceptorError(mapBadRequestContentString(this.body()), false)

    private fun mapToRequestParameters(payload: RequestPayload): HttpRequest = HttpRequest.newBuilder()
        .uri(getUrlForInstructionMethod(payload.method))
        .header("Accept", "text/event-stream")
        .header("Authorization", authorizationHeader)
        .POST(HttpRequest.BodyPublishers.ofString(mapToBodyText(payload, clientSettings.waitTimeout)))
        .build()

    private fun getUrlForInstructionMethod(method: InstructionMethod): URI =
        when (method) {
            InstructionMethod.Question -> {
                urlForMethodGenerate
            }
            InstructionMethod.Classify ->{
                urlForMethodClassify
            }
            else -> {
                urlForMethodTable
            }
        }

}