package com.tgwgroup.zhoupics.utils

import java.util.Locale

/**
 * Converts a Long timestamp to a formatted time string like "MM:ss.SSS".
 */
fun Long.toTimeString(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = this % 1000

    return String.format(Locale.ROOT, "%02d:%02d.%03d", minutes, seconds, milliseconds)
}