package com.tgwgroup.zhoupics.ui.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityEditBinding
import com.tgwgroup.zhoupics.render.RenderHelper
import com.tgwgroup.zhoupics.ui.edit.adjust.AdjustFragment
import com.tgwgroup.zhoupics.ui.edit.beautify.BeautifyFragment
import com.tgwgroup.zhoupics.utils.LogUtil
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivity : BaseActivity<ActivityEditBinding>() {
    private var originalImageUri: Uri? = null

    private var originalBitmap: Bitmap? = null

    private val bottomTabAdapter = BottomTabAdapter()

    private val viewModel by viewModels<EditViewModel>()

    lateinit var renderHelper: RenderHelper

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
        renderHelper = RenderHelper.createAndInit(this, binding.surfaceView)

        loadOriginalImage(
            onLoad = {
                originalBitmap?.let {
                    renderHelper.startRender(it)
                }
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
        renderHelper.destroy()
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
        when (tabId) {
            TAB_ADJUST -> {
                val fragment = AdjustFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_tab_fragment, fragment, AdjustFragment.TAG)
                    .commitNowAllowingStateLoss()
            }

            TAB_BEAUTIFY -> {
                val fragment = BeautifyFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_tab_fragment, fragment, BeautifyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
        }
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
}