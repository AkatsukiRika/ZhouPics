package com.tgwgroup.inpaintlib

import android.graphics.Bitmap

object InpaintLib {
    init {
        System.loadLibrary("inpaintlib")
    }

    external fun runInpaint(imageBitmap: Bitmap, maskBitmap: Bitmap, modelFile: String): Bitmap?
}