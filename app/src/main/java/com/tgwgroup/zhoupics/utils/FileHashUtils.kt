package com.tgwgroup.zhoupics.utils

import com.tgwgroup.baselib.utils.LogUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private const val TAG = "FileHashUtils"

fun calculateMD5(file: File): String? {
    if (!file.isFile) {
        println("Error: Not a valid file.")
        return null
    }

    try {
        val digest = MessageDigest.getInstance("MD5")
        val fis = FileInputStream(file)
        val buffer = ByteArray(8192) // 8KB buffer
        var bytesRead: Int

        fis.use { input -> // Make sure to close the stream
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        val md5Bytes = digest.digest()
        return bytesToHex(md5Bytes)
    } catch (e: NoSuchAlgorithmException) {
        LogUtil.e(TAG, "MD5 algorithm not found", e)
        e.printStackTrace()
    } catch (e: IOException) {
        LogUtil.e(TAG, "Error reading file: ${file.absolutePath}", e)
        e.printStackTrace()
    }
    return null
}

private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    for (i in bytes.indices) {
        val v = bytes[i].toInt() and 0xFF
        hexChars[i * 2] = HEX_ARRAY[v ushr 4]
        hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

private val HEX_ARRAY = "0123456789abcdef".toCharArray()