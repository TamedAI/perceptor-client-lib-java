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

const val ENV_VAR_BASE_URL:String = "TAI_PERCEPTOR_BASE_URL"
const val ENV_VAR_API_KEY:String = "TAI_PERCEPTOR_API_KEY"

/**
 * Perceptor client settings
 *
 */
@Suppress("unused")
data class ClientSettings(
    /**
     * Api key to authenticate.
     */
    val apiKey: String,
    /**
     * Url of the Api
     */
    val url: String,
    /**
     * Request timeout
     */
    val waitTimeout: java.time.Duration = java.time.Duration.ofSeconds(30),

    /**
     * Maximal number of parallel requests
     */
    val maxNumberOfParallelRequests: Int = 10,

    /**
     * Maximum number of attempts for failed requests
     */
    val maxNumberOfAttempts: Int = 3,

    /**
     * Delay (in milliseconds) between parallel request
     */
    val requestDelayFactor: Long = 5
) {
    constructor(apiKey: String, url: String) : this(apiKey, url, java.time.Duration.ofSeconds(30), 10, 3, 5)

    fun withMaxNumberOfParallelRequests(value: Int): ClientSettings = this.copy(maxNumberOfParallelRequests = value)
    fun withMaxNumberOfAttempts(value: Int): ClientSettings = this.copy(maxNumberOfAttempts = value)
    fun withRequestDelayFactor(value: Long): ClientSettings = this.copy(requestDelayFactor = value)

    companion object Factory {
        fun fromEnv(): ClientSettings {
            fun assertNotNull(input: String?, name: String){
                if (input==null){
                    throw IllegalArgumentException("missing or empty '$name'")
                }
            }

            val apiKey = System.getenv(ENV_VAR_API_KEY)
            assertNotNull(apiKey, ENV_VAR_API_KEY)
            val apiUrl = System.getenv(ENV_VAR_BASE_URL)
            assertNotNull(apiUrl, ENV_VAR_BASE_URL)
            return ClientSettings(apiKey, apiUrl)
        }
    }

}