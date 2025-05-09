package com.tgwgroup.zhoupics.utils

import android.graphics.Matrix

data class MatrixParams(
    val scaleX: Float,
    val scaleY: Float,
    val rotationDegrees: Float,
    val translateX: Float,
    val translateY: Float
)

fun Matrix.getParams(): MatrixParams {
    val values = FloatArray(9)
    this.getValues(values)

    val scaleX = Math.sqrt((values[Matrix.MSCALE_X] * values[Matrix.MSCALE_X] + values[Matrix.MSKEW_X] * values[Matrix.MSKEW_X]).toDouble()).toFloat()
    val scaleY = Math.sqrt((values[Matrix.MSCALE_Y] * values[Matrix.MSCALE_Y] + values[Matrix.MSKEW_Y] * values[Matrix.MSKEW_Y]).toDouble()).toFloat()

    val rotation = Math.toDegrees(Math.atan2(values[Matrix.MSKEW_X].toDouble(), values[Matrix.MSCALE_X].toDouble())).toFloat()

    val translateX = values[Matrix.MTRANS_X]
    val translateY = values[Matrix.MTRANS_Y]

    return MatrixParams(scaleX, scaleY, rotation, translateX, translateY)
}