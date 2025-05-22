package com.tgwgroup.zhoupics.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.pow

const val DEVICE_LEVEL_LOW = 1
const val DEVICE_LEVEL_MID = 2
const val DEVICE_LEVEL_HIGH = 3
const val DEVICE_LEVEL_ULTRA = 4

private val cpuFrequencyBaseKhz = 2.0.pow(24.0) / 10

var deviceLevel = 0
    private set

var isLowRamDevice = false
    private set

var internalStorageMB = 0f
    private set

var totalMemoryMB = 0f
    private set

var cpuCores = 0
    private set

var cpuFrequencyMhz = 0f
    private set

var glEsVersionMajor = 0
    private set

var glEsVersionMinor = 0
    private set

/**
 * Calculates device level based on memory, CPU, internal storage, and OpenGL ES configuration.
 *
 * @param context Application context
 * @return Device level
 */
fun getDeviceLevel(context: Context): Int {
    // Return cached level if available
    if (deviceLevel > 0) {
        return deviceLevel
    }

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    isLowRamDevice = activityManager.isLowRamDevice

    // Directly classify as low level if device has low RAM
    if (activityManager.isLowRamDevice) {
        deviceLevel = DEVICE_LEVEL_LOW
        return deviceLevel
    }

    // Calculate internal storage (size of /data partition in MB)
    val storageIndicator = getInternalStorage()
    internalStorageMB = storageIndicator
    val internalStorageBytes = storageIndicator * 1024 * 1024

    // Get total memory in bytes
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalMemoryBytes = memoryInfo.totalMem
    totalMemoryMB = totalMemoryBytes / 1024f / 1024f

    // Get CPU core count
    val coreCount = getNumberOfCores()
    cpuCores = coreCount

    // Get max CPU frequency per core
    var maxCpuFreqKhz = -1
    for (i in 0 until coreCount) {
        val freqFilePath = "/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq"
        val freqFile = File(freqFilePath)
        if (freqFile.exists()) {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(freqFile)
                val buffer = ByteArray(128)
                val bytesRead = fis.read(buffer)
                var len = 0
                while (len < bytesRead && buffer[len] in '0'.toByte()..'9'.toByte()) {
                    len++
                }
                val freqStr = String(buffer, 0, len)
                val freqVal = freqStr.toInt()
                if (freqVal > maxCpuFreqKhz) {
                    maxCpuFreqKhz = freqVal
                    cpuFrequencyMhz = freqVal / 1000f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Fallback parsing from /proc/cpuinfo if sysfs fails
    if (maxCpuFreqKhz == -1) {
        val freq = parseCpuFreqFromProc()
        if (freq != -1) {
            cpuFrequencyMhz = freq.toFloat()
            maxCpuFreqKhz = freq * 1000
        }
    }

    deviceLevel = if (totalMemoryBytes < gigabytesToBytes(2) ||
        internalStorageBytes <= megabytesToBytes(1296) ||
        isCpuFrequencyLow(maxCpuFreqKhz) ||
        coreCount < 4
    ) {
        DEVICE_LEVEL_LOW
    } else {
        val configInfo: ConfigurationInfo = activityManager.deviceConfigurationInfo
        val reqGlEsVersion = configInfo.reqGlEsVersion
        glEsVersionMajor = reqGlEsVersion and 0xFFFF0000.toInt() shr 16
        glEsVersionMinor = reqGlEsVersion and 0x0000FFFF
        if (glEsVersionMajor < 3) {
            DEVICE_LEVEL_LOW
        } else {
            if (totalMemoryBytes < gigabytesToBytes(6)) {
                DEVICE_LEVEL_MID
            } else if (internalStorageBytes > megabytesToBytes(1304) &&
                isCpuFrequencyMidOrHigh(maxCpuFreqKhz) &&
                coreCount >= 6
            ) {
                if (totalMemoryBytes >= gigabytesToBytes(8) &&
                    internalStorageBytes > megabytesToBytes(1312) &&
                    isCpuFrequencyHigh(maxCpuFreqKhz) &&
                    coreCount >= 8
                ) {
                    DEVICE_LEVEL_ULTRA
                } else {
                    DEVICE_LEVEL_HIGH
                }
            } else {
                DEVICE_LEVEL_MID
            }
        }
    }

    return deviceLevel
}

private fun parseCpuFreqFromProc(): Int {
    return try {
        val content = File("/proc/cpuinfo").readText()
        val match = Regex("cpu MHz\\s+:\\s+(\\d+)").find(content)
        match?.groupValues?.get(1)?.toInt() ?: -1
    } catch (e: Exception) {
        -1
    }
}

private fun getNumberOfCores(): Int {
    val cpuDir = File("/sys/devices/system/cpu/")
    val files = cpuDir.listFiles { _, name -> Regex("cpu[0-9]+").matches(name) }
    return files?.size ?: Runtime.getRuntime().availableProcessors()
}

private fun getInternalStorage(): Float {
    return try {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val blockCount = statFs.blockCountLong
        val sizeInMB = ((blockCount * blockSize) / (1024.0 * 1024.0)).toFloat()
        if (sizeInMB <= 0f) 100f else sizeInMB
    } catch (e: Exception) {
        100f
    }
}

private fun gigabytesToBytes(gigabytes: Int): Long = gigabytes * 1024L * 1024L * 1024L

private fun megabytesToBytes(megabytes: Int): Long = megabytes * 1024L * 1024L

private fun isCpuFrequencyLow(freq: Int) = freq.toDouble() < cpuFrequencyBaseKhz

private fun isCpuFrequencyMidOrHigh(freq: Int) = freq.toDouble() >= cpuFrequencyBaseKhz * 1.125

private fun isCpuFrequencyHigh(freq: Int) = freq.toDouble() >= cpuFrequencyBaseKhz * 1.375
