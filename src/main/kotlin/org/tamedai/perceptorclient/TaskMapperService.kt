package org.tamedai.perceptorclient

import kotlinx.coroutines.*
import java.util.concurrent.Semaphore

internal class TaskMapperService(private val maxTaskCount: Int){
    private val semaphore: Semaphore = Semaphore(maxTaskCount)

    suspend fun <T,R> transformList(inputList: List<T>, transform: suspend (T)->R): List<R> = coroutineScope {
        inputList.map { item ->
            async(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    semaphore.acquire()
                }
                try {
                    transform(item)
                } finally {
                    semaphore.release()
                }
            }
        }.toList().awaitAll()

    }
}