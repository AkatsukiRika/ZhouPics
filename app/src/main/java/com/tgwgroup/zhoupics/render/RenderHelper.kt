package com.tgwgroup.zhoupics.render

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
        fun createAndInit(activity: AppCompatActivity, surfaceView: GLSurfaceView): RenderHelper {
            return RenderHelper(activity, surfaceView)
        }
    }

    private lateinit var renderer: ZhouPicsRenderer

    private var sourceRawData: GPUPixelSourceRawData? = null

    private var sinkRawData: GPUPixelSinkRawData? = null

    private var contrastFilter: GPUPixelFilter? = null

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

        contrastFilter = GPUPixelFilter.Create(GPUPixelFilter.CONTRAST_FILTER)

        sourceRawData?.AddSink(sinkRawData)

        activity.lifecycleScope.launch(Dispatchers.IO) {
            val byteBuffer = ByteBuffer
                .allocateDirect(bitmap.byteCount)
                .order(ByteOrder.nativeOrder())
            bitmap.copyPixelsToBuffer(byteBuffer)
            val rgbaBytes = byteBuffer.array()
            sourceRawData?.ProcessData(rgbaBytes, bitmap.width, bitmap.height, bitmap.width * 4, GPUPixelSourceRawData.FRAME_TYPE_RGBA)
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
        contrastFilter?.Destroy()
        contrastFilter = null
    }
}