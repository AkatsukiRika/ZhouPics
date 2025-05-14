package com.tgwgroup.zhoupics.history

open class HistoryRecord

class InitRecord : HistoryRecord()

data class BeautifyRecord(
    val smoothProgress: Float = 0f,
    val whiteProgress: Float = 0f,
    val lipstickProgress: Float = 0f,
    val blusherProgress: Float = 0f,
    val eyeZoomProgress: Float = 0f,
    val faceSlimProgress: Float = 0f
) : HistoryRecord()

data class AdjustRecord(
    val contrastProgress: Float = 0f,
    val exposureProgress: Float = 0f,
    val saturationProgress: Float = 0f,
    val sharpnessProgress: Float = 0f,
    val brightnessProgress: Float = 0f
) : HistoryRecord()

data class UpdateImageRecord(
    val imageUri: String
) : HistoryRecord()