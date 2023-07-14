package com.orot.stt_demo.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object FileMultipart {

    fun getFileBody(fileKey: String = "file", filePath: String): MultipartBody.Part {
        val file = File(filePath)
        val requestFile =
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            fileKey,
            file.name,
            requestFile
        )
    }

}