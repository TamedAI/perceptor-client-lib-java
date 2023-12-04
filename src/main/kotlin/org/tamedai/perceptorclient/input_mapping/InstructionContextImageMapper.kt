package org.tamedai.perceptorclient.input_mapping

import org.tamedai.perceptorclient.InstructionContextData
import java.io.File
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


internal object InstructionContextImageMapper{
    private val validFileTypes: Set<String> =  setOf("png", "jpg", "jpeg")

    fun mapFromFile(filePath: String): InstructionContextData {
        val fileExtension = getFileExtension(filePath)
        return mapFromStream(File(filePath).inputStream(), fileExtension)
    }

    fun mapFromFiles(filePaths: List<String>): List<InstructionContextData> =
        filePaths.map { fp-> mapFromFile(fp) }.toList()

    @OptIn(ExperimentalEncodingApi::class)
    fun mapFromStream(fileStream: InputStream, fileType: String): InstructionContextData {
        val bytes = fileStream.readBytes()
        return mapFromBytes(bytes, fileType)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun mapFromBytes(fileBytes: ByteArray, fileType: String): InstructionContextData {
        assertValidFileType(fileType)
        val encodedString: String = Base64.encode(fileBytes)
        return InstructionContextData("image", "data:image/$fileType;base64,$encodedString")
    }

    private fun getFileExtension(filePath: String): String{
        return File(filePath).extension
    }


    fun isValidFileType(fileExtension: String): Boolean =
        validFileTypes.any { x->x.equals(fileExtension, ignoreCase = true)}

    private fun assertValidFileType(fileExtension: String) {
        if (!validFileTypes.contains(fileExtension))
            throw IllegalArgumentException("invalid file type")
    }

}