package com.tgwgroup.inpaintlib

import android.graphics.Bitmap

class NativeLib {
    companion object {
        // Used to load the 'inpaintlib' library on application startup.
        init {
            System.loadLibrary("inpaintlib")
        }
    }

    external fun runInpaint(imageBitmap: Bitmap, maskBitmap: Bitmap, modelFile: String): Bitmap?
}