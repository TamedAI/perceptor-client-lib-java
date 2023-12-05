package org.tamedai.perceptorclient

import kotlinx.coroutines.*
import java.util.concurrent.Semaphore

internal class TaskMapperService(maxTaskCount: Int, private val requestDelayFactor:Long){
    private val semaphore: Semaphore = Semaphore(maxTaskCount)

    suspend fun <T,R> transformList(inputList: List<T>, transform: suspend (T)->R): List<R> = coroutineScope {

        fun getDelay(index:Int): Long {
            return ((index % inputList.count()) * requestDelayFactor)
        }

        inputList.mapIndexed{index,item ->
            async(Dispatchers.Default) {

                delay(getDelay(index))

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