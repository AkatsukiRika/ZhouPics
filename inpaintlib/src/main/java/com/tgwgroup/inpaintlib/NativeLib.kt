package com.tgwgroup.inpaintlib

class NativeLib {

    /**
     * A native method that is implemented by the 'inpaintlib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'inpaintlib' library on application startup.
        init {
            System.loadLibrary("inpaintlib")
        }
    }
}