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

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class TaskMapperService(maxTaskCount: Int, private val requestDelayFactor: Long) {
    private val semaphore = Semaphore(maxTaskCount)

    suspend fun <T, R> transformList(inputList: List<T>, transform: suspend (T) -> R): List<R> = coroutineScope {

        fun getDelay(index: Int): Long {
            return ((index % inputList.count()) * requestDelayFactor)
        }

        inputList.mapIndexed{index,item ->
            async(Dispatchers.Default) {
                delay(getDelay(index))

                semaphore.withPermit {
                    transform(item)
                }
            }
        }.toList().awaitAll()
    }

}