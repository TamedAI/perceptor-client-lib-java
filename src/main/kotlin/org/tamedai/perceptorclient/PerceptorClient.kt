package org.tamedai.perceptorclient

import kotlinx.coroutines.*
import org.tamedai.perceptorclient.input_mapping.InstructionContextImageMapper
import org.tamedai.perceptorclient.repository.PerceptorHttpRepository
import kotlin.time.toKotlinDuration

typealias ClientResponse = List<InstructionWithResponse>


class PerceptorClient internal constructor(private val createSessionFunc: (InstructionContextData)->ContentSessionContext)  {

    fun askText(textToAsk: String,instructions: List<String>): ClientResponse = runBlocking {
        askInContext(
            InstructionContextData.forText(textToAsk),
            PerceptorRequest.default(),
            InstructionMethod.Question,
            instructions)
    }

    fun askText(textToAsk: String, request:PerceptorRequest, instructions: List<String>): ClientResponse = runBlocking{
        askInContext(
            InstructionContextData.forText(textToAsk),
            request,
            InstructionMethod.Question,
            instructions)
    }

    fun askImage(imagePath: String, instructions: List<String>): ClientResponse = runBlocking{

            val imageContextData = InstructionContextImageMapper.mapFromFile(imagePath)
            askInContext(imageContextData,
                PerceptorRequest.default(),
                InstructionMethod.Question,
                instructions
            )
    }

    fun askImage(imagePath: String, request:PerceptorRequest, instructions: List<String>): ClientResponse = runBlocking{
        val imageContextData = InstructionContextImageMapper.mapFromFile(imagePath)
        askInContext(imageContextData,
            request,
            InstructionMethod.Question,
            instructions
        )
    }

    fun askDocument(imagePaths:List<String>, instructions: List<String>): List<ClientResponse> =
        askDocument(imagePaths, PerceptorRequest.default(), instructions)


    fun askDocument(imagePaths:List<String>, request: PerceptorRequest, instructions: List<String>): List<ClientResponse> = runBlocking{
        val contextData = InstructionContextImageMapper.mapFromFiles(imagePaths)
        askInMultipleContexts(contextData, request, InstructionMethod.Question, instructions)
    }


    private suspend fun askInContext(contextData: InstructionContextData,
                                     request: PerceptorRequest,
                                     method: InstructionMethod,
                                     instructions: List<String>): List<InstructionWithResponse> = coroutineScope{

        val mappedInstructions = instructions.map { s-> Instruction(s) }.toList()
        val res = createSessionFunc(contextData).processInstructionsRequest(request,
            method,
            mappedInstructions)

        return@coroutineScope res
    }

    private suspend fun askInMultipleContexts(contexts: List<InstructionContextData>,
                                              request: PerceptorRequest,
                                              method: InstructionMethod,
                                              instructions: List<String>):List<ClientResponse>{

        suspend fun processSingleContext(contextData: InstructionContextData): List<InstructionWithResponse> {
            return askInContext(contextData,
                request, method, instructions);
        }

        return coroutineScope {
            async {
                contexts.map{c->async { processSingleContext(c)}}
                    .toList().awaitAll()
            }
        }.await()

    }
}

object PerceptorClientFactory{
    fun createClient(settings: ClientSettings): PerceptorClient {
        val waitTimeout = settings.waitTimeout
        val httpSettings = HttpClientSettings(apiKey = settings.apiKey,
            settings.url,
            waitTimeout = waitTimeout.toKotlinDuration())
        val r = PerceptorHttpRepository(httpSettings);

        val taskService = TaskMapperService(5)
        val createSessionFunc: (InstructionContextData)->ContentSessionContext = { ctx->
            ContentSessionContext(r, taskService, ctx)
        }
        return PerceptorClient(createSessionFunc)
    }
}