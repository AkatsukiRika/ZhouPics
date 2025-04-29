package com.tgwgroup.zhoupics.render

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixpark.gpupixel.FaceDetector
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.GPUPixelFilter
import com.pixpark.gpupixel.GPUPixelSinkRawData
import com.pixpark.gpupixel.GPUPixelSourceRawData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RenderHelper private constructor(private val activity: AppCompatActivity, private val surfaceView: GLSurfaceView) {
    companion object {
        const val PROPERTY_FACE_LANDMARK = "face_landmark"
        const val PROPERTY_BLEND_LEVEL = "blend_level"
        const val PROPERTY_SKIN_SMOOTHING = "skin_smoothing"
        const val PROPERTY_WHITENESS = "whiteness"
        const val PROPERTY_THIN_FACE = "thin_face"
        const val PROPERTY_BIG_EYE = "big_eye"

        fun createAndInit(activity: AppCompatActivity, surfaceView: GLSurfaceView): RenderHelper {
            return RenderHelper(activity, surfaceView)
        }
    }

    private lateinit var renderer: ZhouPicsRenderer

    private var sourceRgbaData: ByteArray? = null

    private var sourceRawData: GPUPixelSourceRawData? = null

    private var sinkRawData: GPUPixelSinkRawData? = null

    private var lipstickFilter: GPUPixelFilter? = null

    private var beautyFilter: GPUPixelFilter? = null

    private var faceReshapeFilter: GPUPixelFilter? = null

    private var blusherFilter: GPUPixelFilter? = null

    private var faceDetector: FaceDetector? = null

    private var outWidth = 0

    private var outHeight = 0

    init {
        GPUPixel.Init(activity)
        initSurfaceView()
    }

    private fun initSurfaceView() {
        surfaceView.setEGLContextClientVersion(2)
        renderer = ZhouPicsRenderer(activity)
        surfaceView.setRenderer(renderer)
    }

    fun startRender(bitmap: Bitmap) {
        sourceRawData = GPUPixelSourceRawData.Create()
        sinkRawData = GPUPixelSinkRawData.Create()
        faceDetector = FaceDetector.Create()

        beautyFilter = GPUPixelFilter.Create(GPUPixelFilter.BEAUTY_FACE_FILTER)
        faceReshapeFilter = GPUPixelFilter.Create(GPUPixelFilter.FACE_RESHAPE_FILTER)
        lipstickFilter = GPUPixelFilter.Create(GPUPixelFilter.LIPSTICK_FILTER)
        blusherFilter = GPUPixelFilter.Create(GPUPixelFilter.BLUSHER_FILTER)

        sourceRawData?.AddSink(lipstickFilter)
        lipstickFilter?.AddSink(blusherFilter)
        blusherFilter?.AddSink(beautyFilter)
        beautyFilter?.AddSink(faceReshapeFilter)
        faceReshapeFilter?.AddSink(sinkRawData)

        activity.lifecycleScope.launch(Dispatchers.IO) {
            val byteBuffer = ByteBuffer
                .allocateDirect(bitmap.byteCount)
                .order(ByteOrder.nativeOrder())
            bitmap.copyPixelsToBuffer(byteBuffer)
            sourceRgbaData = byteBuffer.array()
            outWidth = bitmap.width
            outHeight = bitmap.height
            doFaceDetect()
            doRender()
        }
    }

    fun updateSmoothProgress(progress: Float) {
        beautyFilter?.SetProperty(PROPERTY_SKIN_SMOOTHING, progress / 100f)
        doRender()
    }

    fun updateWhiteProgress(progress: Float) {
        beautyFilter?.SetProperty(PROPERTY_WHITENESS, progress / 100f)
        doRender()
    }

    fun updateLipstickProgress(progress: Float) {
        lipstickFilter?.SetProperty(PROPERTY_BLEND_LEVEL, progress / 100f)
        doRender()
    }

    fun updateBlusherProgress(progress: Float) {
        blusherFilter?.SetProperty(PROPERTY_BLEND_LEVEL, progress / 100f)
        doRender()
    }

    fun updateEyeZoomProgress(progress: Float) {
        faceReshapeFilter?.SetProperty(PROPERTY_BIG_EYE, progress / 400f)
        doRender()
    }

    fun updateFaceSlimProgress(progress: Float) {
        faceReshapeFilter?.SetProperty(PROPERTY_THIN_FACE, progress / 1600f)
        doRender()
    }

    private fun doFaceDetect() {
        sourceRgbaData?.let {
            val landmarks = faceDetector?.detect(
                it, outWidth, outHeight, outWidth * 4,
                FaceDetector.GPUPIXEL_MODE_FMT_VIDEO, FaceDetector.GPUPIXEL_FRAME_TYPE_RGBA
            )
            if (landmarks != null && landmarks.isNotEmpty()) {
                faceReshapeFilter?.SetProperty(PROPERTY_FACE_LANDMARK, landmarks)
                lipstickFilter?.SetProperty(PROPERTY_FACE_LANDMARK, landmarks)
                blusherFilter?.SetProperty(PROPERTY_FACE_LANDMARK, landmarks)
            }
        }
    }

    private fun doRender() {
        sourceRgbaData?.let {
            sourceRawData?.ProcessData(it, outWidth, outHeight, outWidth * 4, GPUPixelSourceRawData.FRAME_TYPE_RGBA)
            val processedRgba = sinkRawData?.GetRgbaBuffer()
            processedRgba?.let {
                val rgbaWidth = sinkRawData?.GetWidth() ?: 0
                val rgbaHeight = sinkRawData?.GetHeight() ?: 0
                renderer.updateTextureData(it, rgbaWidth, rgbaHeight, 0)
                surfaceView.requestRender()
            }
        }
    }

    fun destroy() {
        sourceRawData?.Destroy()
        sourceRawData = null
        sinkRawData?.Destroy()
        sinkRawData = null
        faceDetector?.destroy()
        faceDetector = null
        lipstickFilter?.Destroy()
        lipstickFilter = null
        beautyFilter?.Destroy()
        beautyFilter = null
        faceReshapeFilter?.Destroy()
        faceReshapeFilter = null
    }
}