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
        File(filePath).inputStream().use {
            inputStream -> return mapFromStream(inputStream, fileExtension);
        }
    }

    fun mapFromFiles(filePaths: List<String>): List<InstructionContextData> =
        filePaths.map { fp-> mapFromFile(fp) }.toList()

    fun mapFromStreams(inputStreams: List<Pair<InputStream, String>>): List<InstructionContextData> =
        inputStreams.map { mapFromStream(it.first, it.second) }.toList()

    fun mapFromStream(fileStream: InputStream, fileType: String): InstructionContextData {
        val bytes = fileStream.readBytes()
        return mapFromBytes(bytes, fileType)
    }

    fun mapFromBytes(inputBytes: List<Pair<ByteArray, String>>): List<InstructionContextData> =
        inputBytes.map { mapFromBytes(it.first, it.second) }

    @OptIn(ExperimentalEncodingApi::class)
    fun mapFromBytes(fileBytes: ByteArray, fileType: String): InstructionContextData {
        assertValidFileType(fileType)
        val encodedString: String = Base64.encode(fileBytes)
        return InstructionContextData("image", "data:image/$fileType;base64,$encodedString")
    }

    private fun getFileExtension(filePath: String): String{
        return File(filePath).extension
    }


    internal fun String.isValidFileType(): Boolean =
        validFileTypes.any { it.equals(this, ignoreCase = true)}

    private fun assertValidFileType(fileExtension: String) {
        if (!fileExtension.isValidFileType())
            throw IllegalArgumentException("invalid file type")
    }

}