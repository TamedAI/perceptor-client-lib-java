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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.tamedai.perceptorclient.ClientSettings
import org.tamedai.perceptorclient.ENV_VAR_API_KEY
import org.tamedai.perceptorclient.ENV_VAR_BASE_URL
import java.io.File

private val jsonDeserializer = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    prettyPrint = false
}

val curDir = System.getProperty("user.dir")
val filePath = "$curDir/src/test/kotlin/integration_tests/config.json"

data class Config(val apiKey: String, val apiUrl: String)

@OptIn(ExperimentalSerializationApi::class)
fun getFromConfigFile(): Config {
    val map = jsonDeserializer.decodeFromStream<Map<String, String>>(File(filePath).inputStream())
    val apiUrl = map[ENV_VAR_BASE_URL]!!
    val apiKey = map[ENV_VAR_API_KEY]!!
    return Config(apiKey, apiUrl)
}

val config = getFromConfigFile()
val clientSettings = ClientSettings(config.apiKey, config.apiUrl)