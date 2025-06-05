package com.tgwgroup.zhoupics.utils

import com.tgwgroup.baselib.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

private const val TAG = "FileDownloadUtils"

private val client = OkHttpClient()

interface DownloadCallback {
    fun onProgress(progress: Int)
    fun onSuccess(file: File)
    fun onFailure(e: Exception)
}

suspend fun downloadFile(url: String, outputFile: File, callback: DownloadCallback) {
    withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Download failed: code=${response.code}, message=${response.message}")
            }

            val body = response.body ?: throw IOException("Response body is null")
            val contentLength = body.contentLength()
            val inputStream = body.byteStream()

            val outputDir = outputFile.parentFile
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val outputStream = outputFile.outputStream()

            val buffer = ByteArray(4096)    // 4KB buffer
            var totalBytesRead: Long = 0
            var bytesRead: Int
            var lastProgress = 0

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val currentProgress = (totalBytesRead * 100 / contentLength).toInt()
                            if (currentProgress != lastProgress) {
                                withContext(Dispatchers.Main) {
                                    callback.onProgress(currentProgress)
                                }
                                lastProgress = currentProgress
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                callback.onSuccess(outputFile)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "Download failed", e)
            withContext(Dispatchers.Main) {
                callback.onFailure(e)
            }
        }
    }
}