package org.tamedai.perceptorclient

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration


@Serializable
private data class RequestBodyModel(
    val flavor: String,
    val contextType: String,
    val context: String,
    val instruction: String,
    val waitTimeout: Long,
    val params: Map<String, String>
)

@OptIn(ExperimentalSerializationApi::class)
internal fun mapToBodyText(payload: RequestPayload, waitTimeout: Duration): String {

    val toSerialize = RequestBodyModel(
        payload.parameters.flavour,
        payload.contextData.contextType,
        payload.contextData.content,
        payload.instruction.text,
        waitTimeout.inWholeSeconds,
        payload.parameters.detailedParameters
    );

    return Json.encodeToString(toSerialize)
}

internal fun concatUrl(url1: String, url2: String): String {
    // TODO - Perhaps we can replace it with some library function
    val firstTrimmed = url1.trimEnd('/')
    val secondTrimmed = url2.trimStart('/')
    if (firstTrimmed.isEmpty()) {
        return url2
    }
    if (secondTrimmed.isEmpty()) {
        return url1
    }
    return "$firstTrimmed/$secondTrimmed"
}

private const val eventFinishedLine: String = "event: finished"
private const val eventDataLinePrefix: String = "data: "

internal fun parseSseEvents(contentString: String): List<SseEvent> {
    return contentString.lines()
        .filter { s -> s.isNotEmpty() }
        .dropWhile { s -> !s.isEventFinishedLine() }
        .selectDataLines()
        .mapNotNull { s -> s.getSseEventFromLine() }
        .map { x -> x }
}

private fun String.isEventFinishedLine(): Boolean {
    return this.startsWith(eventFinishedLine)
}

@OptIn(ExperimentalStdlibApi::class)
private fun List<String>.selectDataLines(): List<String> {
    if (this.isEmpty())
        return listOf()
    return (0..<this.count() step 2)
        .map { index ->
            if (this[index].isEventFinishedLine() && index < this.count()) {
                listOf(this[index + 1])
            } else {
                emptyList()
            }
        }
        .flatMap { x -> x.toList() }

}

private fun String.getSseEventFromLine(): SseEvent? {
    if (!this.startsWith(eventDataLinePrefix))
        return null
    val data = this.substring(eventDataLinePrefix.length)
    return SseEvent(data)
}
