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
package org.tamedai.perceptorclient

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

private val jsonDeserializer = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    prettyPrint = false
}

internal fun mapBadRequestContentString(contentString: String): String {
    fun parseJsonElement(el: JsonElement?) =
        when (el) {
            is JsonPrimitive -> el.content
            else -> el.toString()
        }


    return try {
        val deserializedMap = jsonDeserializer.decodeFromString<Map<String, JsonElement>>(contentString)
        when (val jsonElement: JsonElement? = deserializedMap["detail"]) {
            is JsonPrimitive -> parseJsonElement(jsonElement)
            is JsonArray -> jsonElement.joinToString { x -> parseJsonElement(x) }
            else -> contentString
        }

    } catch (e: SerializationException) {
        contentString
    }
}

private fun mapJsonEntry(entry:Map.Entry<String,JsonElement>): Pair<String, Any>{
    fun mapElement(el: JsonElement): Any{
        return when (el) {
            is JsonPrimitive -> {
                el.content
            }

            is JsonObject -> {
                el.map { mapJsonEntry(it) }.toMap()
            }

            is JsonArray -> {
                el.map { mapElement(it) }.toList()
            }

            else -> el.toString()
        }
    }

    return Pair(entry.key, mapElement(entry.value))
}

private const val KEY_TEXT = "text"
internal fun mapResponseToStructuredContent(contentString: String): Map<String, Any> {
    if (contentString.trim().isEmpty())
        return mapOf(KEY_TEXT to "")


    val mapped = getMapFromStructuredContent(contentString)

    if (!mapped.containsKey(KEY_TEXT)) {
        val mod = mapped.toMutableMap()
        mod[KEY_TEXT] = ""
        return mod
    }
    return mapped

}

private fun getMapFromStructuredContent(contentString: String): Map<String, Any> {
    return try {
        val jsonObject = jsonDeserializer.decodeFromString<JsonObject>(contentString)
        jsonObject
            .map { mapJsonEntry(it) }
            .toMap()
    } catch (e: SerializationException) {
        mapOf(KEY_TEXT to contentString)
    }
}

