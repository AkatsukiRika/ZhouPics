package com.tgwgroup.zhoupics.ui.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.GPUPixelSinkRawData
import com.pixpark.gpupixel.GPUPixelSourceImage
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityEditBinding
import com.tgwgroup.zhoupics.render.ZhouPicsRenderer
import com.tgwgroup.zhoupics.ui.edit.adjust.AdjustFragment
import com.tgwgroup.zhoupics.utils.LogUtil
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivity : BaseActivity<ActivityEditBinding>() {
    private var originalImageUri: Uri? = null

    private var originalBitmap: Bitmap? = null

    private lateinit var surfaceView: GLSurfaceView

    private lateinit var renderer: ZhouPicsRenderer

    private val bottomTabAdapter = BottomTabAdapter()

    private val viewModel by viewModels<EditViewModel>()

    companion object {
        const val TAG = "EditActivity"
        private const val EXTRA_URI = "uri"

        fun start(context: Context, uri: Uri) {
            val intent = Intent(context, EditActivity::class.java).apply {
                putExtra(EXTRA_URI, uri)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityEditBinding {
        return ActivityEditBinding.inflate(layoutInflater)
    }

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }

    override fun initView() {
        super.initView()
        GPUPixel.Init(this)
        initSurfaceView()

        loadOriginalImage(
            onLoad = {
                startRender()
            },
            onLoadFailed = {
                LogUtil.e(TAG, "Failed to load image")
            }
        )

        initBottomTab()
        initTriggers()
        initCollectors()
    }

    override fun onDestroy() {
        super.onDestroy()
        sourceImage?.Destroy()
        sourceImage = null
        sinkRawData?.Destroy()
        sinkRawData = null
    }

    private fun initBottomTab() {
        binding.rvBottomTab.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBottomTab.adapter = bottomTabAdapter

        bottomTabAdapter.setItems(viewModel.bottomTabItemList.value)
    }

    private fun initTriggers() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivExport.setOnClickListener {
        }
    }

    private fun initCollectors() {
        viewModel.bottomTabItemList.collectIn(lifecycleScope) {
            bottomTabAdapter.setItems(it)
        }

        viewModel.selectedBottomTabId.collectIn(lifecycleScope) {
            updateTabFragment(it)
        }
    }

    private fun updateTabFragment(tabId: Int) {
        val fragment = AdjustFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_tab_fragment, fragment, AdjustFragment.TAG)
            .commitNowAllowingStateLoss()
    }

    private fun loadOriginalImage(onLoad: (() -> Unit)? = null, onLoadFailed: (() -> Unit)? = null) {
        originalImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_URI, Uri::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_URI)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            originalImageUri?.let {
                originalBitmap = getBitmap(it)
                if (originalBitmap != null) {
                    withContext(Dispatchers.Main) {
                        onLoad?.invoke()
                    }
                } else {
                    onLoadFailed?.invoke()
                }
            } ?: run {
                onLoadFailed?.invoke()
            }
        }
    }

    private fun initSurfaceView() {
        surfaceView = binding.surfaceView
        surfaceView.setEGLContextClientVersion(2)
        renderer = ZhouPicsRenderer(this)
        surfaceView.setRenderer(renderer)
    }

    private var sourceImage: GPUPixelSourceImage? = null

    private var sinkRawData: GPUPixelSinkRawData? = null

    private fun startRender() {
        sourceImage = GPUPixelSourceImage.CreateFromBitmap(originalBitmap)
        sinkRawData = GPUPixelSinkRawData.Create()
        sourceImage?.AddSink(sinkRawData)
        sourceImage?.Render()
        val processedRgba = sinkRawData?.GetRgbaBuffer()
        processedRgba?.let {
            val rgbaWidth = sinkRawData?.GetWidth() ?: 0
            val rgbaHeight = sinkRawData?.GetHeight() ?: 0
            renderer.updateTextureData(it, rgbaWidth, rgbaHeight, 0)
            surfaceView.requestRender()
        }
    }
}