package org.tamedai.perceptorclient

import kotlinx.coroutines.*
import org.tamedai.perceptorclient.input_mapping.InstructionContextImageMapper
import org.tamedai.perceptorclient.repository.PerceptorHttpRepository
import org.tamedai.perceptorclient.repository.PerceptorRepositoryRetryDecorator
import java.io.InputStream
import kotlin.time.toKotlinDuration

typealias ClientResponse = List<InstructionWithResult>

class PerceptorClient internal constructor(private val createSessionFunc: (InstructionContextData) -> ContentSessionContext) {

    /**
     * Sends instructions for the specified [textToAsk].
     *
     * @param textToAsk text to ask
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of [InstructionWithResult] containing instructions and their answers.
     */
    fun askText(textToAsk: String, request: PerceptorRequest, instructions: List<String>): List<InstructionWithResult> =
        askInContextBlocking(
            { InstructionContextData.forText(textToAsk) },
            request,
            InstructionMethod.Question,
            instructions,
            listOf()
        )

    /**
     * Sends classify instruction for the specified [textToAsk]
     *
     * @param textToAsk text to ask
     * @param request Request parameters
     * @param instruction text to ask
     * @param classes list of classes ("document", "invoice" etc.)
     * @return [InstructionWithResult] containing instruction response's scores.
     */
    fun classifyText(textToAsk: String, request: PerceptorRequest, instruction: String,
                     classes: List<String>): InstructionWithResult =
        classifyInContextBlocking(
            { InstructionContextData.forText(textToAsk) },
            request,
            instruction,
            classes
        )

    /**
     * Sends instructions for the specified [imagePath].
     *
     * @param imagePath image path
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of lists of [InstructionWithResult] containing instructions and their answers.
     */
    fun askImage(imagePath: String, request: PerceptorRequest, instructions: List<String>): ClientResponse {
        return askInContextBlocking(
            { InstructionContextImageMapper.mapFromFile(imagePath) },
            request,
            InstructionMethod.Question,
            instructions,
            listOf()
        )
    }

    /**
     * Sends classify instruction for the specified [imagePath]
     *
     * @param imagePath image path
     * @param request Request parameters
     * @param instruction instruction to perform
     * @param classes list of classes ("document", "invoice" etc.)
     * @return [InstructionWithResult] containing instruction response's scores.
     */
    fun classifyImage(imagePath: String, request: PerceptorRequest, instruction: String,
        classes: List<String>): InstructionWithResult =
        classifyInContextBlocking(
            { InstructionContextImageMapper.mapFromFile(imagePath) },
            request,
            instruction,
            classes
        )

    /**
     * Sends a table instruction for the specified [imagePath].
     *
     * @param imagePath image path
     * @param request Request parameters
     * @param instruction Instruction to perform
     * @return [InstructionWithResult] containing and answer/error message.
     */
    fun askTableFromImage(imagePath: String, request: PerceptorRequest, instruction: String): InstructionWithResult =
        askTableInContextBlocking(
            { InstructionContextImageMapper.mapFromFile(imagePath) },
            request,
            instruction
        )

    /**
     * Sends instructions for the specified [imageStream].
     *
     * @param imageStream image stream
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of lists of [InstructionWithResult] containing instructions and their answers.
     */
    fun askImage(imageStream: InputStream, fileType: String, request: PerceptorRequest, instructions: List<String>) =
        askInContextBlocking(
            { InstructionContextImageMapper.mapFromStream(imageStream, fileType) },
            request,
            InstructionMethod.Question,
            instructions,
            listOf()
        )

    /**
     * Sends a table instruction for the specified [imageStream].
     *
     * @param imageStream image stream
     * @param request Request parameters
     * @param instruction Instruction to perform
     * @return [InstructionWithResult] containing and answer/error message.
     */
    fun askTableFromImage(
        imageStream: InputStream,
        fileType: String,
        request: PerceptorRequest,
        instruction: String
    ): InstructionWithResult =
        askTableInContextBlocking(
            { InstructionContextImageMapper.mapFromStream(imageStream, fileType) },
            request,
            instruction
        )

    /**
     * Sends a table instruction for the specified [imageBytes].
     *
     * @param imageBytes image bytes array
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return [InstructionWithResult] containing and answer/error message.
     */
    fun askImage(imageBytes: ByteArray, fileType: String, request: PerceptorRequest, instructions: List<String>) =
        askInContextBlocking(
            { InstructionContextImageMapper.mapFromBytes(imageBytes, fileType) },
            request,
            InstructionMethod.Question,
            instructions,
            listOf()
        )

    /**
     * Sends a table instruction for the specified [imageBytes].
     *
     * @param imageBytes image bytes array
     * @param request Request parameters
     * @param instruction Instruction to perform
     * @return [InstructionWithResult] containing and answer/error message.
     */
    fun askTableFromImage(
        imageBytes: ByteArray,
        fileType: String,
        request: PerceptorRequest,
        instruction: String
    ): InstructionWithResult =
        askTableInContextBlocking(
            { InstructionContextImageMapper.mapFromBytes(imageBytes, fileType) },
            request,
            instruction
        )

    /**
     * Sends instructions for multiple images specified by [imagePaths].
     *
     * @param imagePaths List of file image paths
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of [DocumentImageResult] containing instructions and their answers.
     */
    fun askDocumentImagePaths(
        imagePaths: List<String>,
        request: PerceptorRequest,
        instructions: List<String>
    ): List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromFiles(imagePaths)
        askInMultipleContexts(contextData, request, InstructionMethod.Question, instructions, listOf())
    }

    /**
     * Sends instructions for multiple image streams specified by [inputStreams].
     *
     * @param inputStreams List of pairs (Stream, String) containing image stream/file type.
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of lists of [DocumentImageResult] containing instructions and their answers.
     */
    fun askDocumentImageStreams(
        inputStreams: List<Pair<InputStream, String>>,
        request: PerceptorRequest,
        instructions: List<String>
    ): List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromStreams(inputStreams)
        askInMultipleContexts(contextData, request, InstructionMethod.Question, instructions, listOf())
    }

    /**
     * Sends instructions for multiple image bytes arrays specified by [imageBytes].
     *
     * @param imageBytes List of pairs (bytearray, String) containing image file type.
     * @param request Request parameters
     * @param instructions List of instructions to perform
     * @return List of lists of [DocumentImageResult] containing instructions and their answers.
     */
    fun askDocumentImageBytes(
        imageBytes: List<Pair<ByteArray, String>>,
        request: PerceptorRequest,
        instructions: List<String>
    ): List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromBytes(imageBytes)
        askInMultipleContexts(contextData, request, InstructionMethod.Question, instructions, listOf())
    }

    /**
     * Sends classify instruction for multiple images specified by [imagePaths].
     *
     * @param imagePaths image path
     * @param request Request parameters
     * @param instruction instruction to perform
     * @param classes list of classes ("document", "invoice" etc.)
     * @return [InstructionWithResult] containing instruction and response's scores.
     */
    fun classifyDocumentImagePaths(
        imagePaths: List<String>,
        request: PerceptorRequest,
        instruction: String,
        classes: List<String>
    ):List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromFiles(imagePaths)
        askInMultipleContexts(contextData, request, InstructionMethod.Classify, instruction, classes)
    }

    /**
     * Sends classify instruction for multiple image streams specified by [inputStreams].
     *
     * @param inputStreams List of pairs (Stream, String) containing image stream/file type.
     * @param request Request parameters
     * @param instruction instruction to perform
     * @param classes list of classes ("document", "invoice" etc.)
     * @return [InstructionWithResult] containing instruction and response's scores.
     */
    fun classifyDocumentImageStreams(
        inputStreams: List<Pair<InputStream, String>>,
        request: PerceptorRequest,
        instruction: String,
        classes: List<String>
    ):List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromStreams(inputStreams)
        askInMultipleContexts(contextData, request, InstructionMethod.Classify, instruction, classes)
    }

    /**
     * Sends classify instruction for multiple image byte array specified by [inputBytes].
     *
     * @param inputBytes List of pairs (byte array, String) containing image file type.
     * @param request Request parameters
     * @param instruction instruction to perform
     * @param classes list of classes ("document", "invoice" etc.)
     * @return [InstructionWithResult] containing instruction and response's scores.
     */
    fun classifyDocumentImageBytes(
        inputBytes: List<Pair<ByteArray, String>>,
        request: PerceptorRequest,
        instruction: String,
        classes: List<String>
    ): List<DocumentImageResult> = runBlocking {
        val contextData = InstructionContextImageMapper.mapFromBytes(inputBytes)
        askInMultipleContexts(contextData, request, InstructionMethod.Classify, instruction, classes)
    }

    private fun askInContextBlocking(
        getContextData: () -> InstructionContextData,
        request: PerceptorRequest,
        method: InstructionMethod,
        instructions: List<String>,
        classes: List<String>
    ): List<InstructionWithResult> = runBlocking {
        askInContext(getContextData(), request, method, instructions, classes)
    }

    private fun askTableInContextBlocking(
        getContextData: () -> InstructionContextData,
        request: PerceptorRequest,
        instruction: String
    ): InstructionWithResult {
        val result = askInContextBlocking(getContextData, request, InstructionMethod.Table, listOf(instruction),listOf())
        return when {
            result.isNotEmpty() -> result[0]
            else -> InstructionWithResult.fromError(instruction, ErrorConstants.unknownError)
        }
    }

    private fun classifyInContextBlocking(
        getContextData: () -> InstructionContextData,
        request: PerceptorRequest,
        instruction: String,
        classes: List<String>
    ): InstructionWithResult {
        val result = askInContextBlocking(getContextData, request, InstructionMethod.Classify,
            listOf(instruction),
            classes)
        return when {
            result.isNotEmpty() -> result[0]
            else -> InstructionWithResult.fromError(instruction, ErrorConstants.unknownError)
        }
    }

    private suspend fun askInContext(
        contextData: InstructionContextData,
        request: PerceptorRequest,
        method: InstructionMethod,
        instructions: List<String>,
        classes: List<String>
    ): List<InstructionWithResult> = coroutineScope {

        val mappedInstructions = instructions.map { s -> Instruction(s) }.toList()
        val result = createSessionFunc(contextData).processInstructionsRequest(
            request,
            method,
            mappedInstructions,
            classes.map { ClassificationEntry(it)}
        )

        return@coroutineScope result
    }

    private suspend fun askInMultipleContexts(
        contexts: List<InstructionContextData>,
        request: PerceptorRequest,
        method: InstructionMethod,
        instruction: String,
        classes: List<String>
    ): List<DocumentImageResult>  =
        askInMultipleContexts(contexts,request,method, listOf(instruction),classes)


    private suspend fun askInMultipleContexts(
        contexts: List<InstructionContextData>,
        request: PerceptorRequest,
        method: InstructionMethod,
        instructions: List<String>,
        classes: List<String>
    ): List<DocumentImageResult> {

        suspend fun processSingleContext(pageIndex: Int, contextData: InstructionContextData): DocumentImageResult {
            val instructionResult = askInContext(
                contextData,
                request, method, instructions,classes
            );
            return DocumentImageResult(pageIndex, instructionResult)
        }

        return coroutineScope {
            async {
                contexts.mapIndexed { pageIndex,ctx -> async { processSingleContext(pageIndex, ctx) } }
                    .toList().awaitAll()
            }
        }.await()

    }
}

object PerceptorClientFactory {
    fun createClient(settings: ClientSettings): PerceptorClient {
        val waitTimeout = settings.waitTimeout
        val httpSettings = HttpClientSettings(
            apiKey = settings.apiKey,
            settings.url,
            waitTimeout = waitTimeout.toKotlinDuration()
        )
        val httpRepository = PerceptorHttpRepository(httpSettings);
        val repositoryRetryDecorator = PerceptorRepositoryRetryDecorator(httpRepository,
            settings.maxNumberOfAttempts)

        val taskService = TaskMapperService(settings.maxNumberOfParallelRequests, settings.requestDelayFactor)
        val createSessionFunc: (InstructionContextData) -> ContentSessionContext = { ctx ->
            ContentSessionContext(repositoryRetryDecorator, taskService, ctx)
        }
        return PerceptorClient(createSessionFunc)
    }
}