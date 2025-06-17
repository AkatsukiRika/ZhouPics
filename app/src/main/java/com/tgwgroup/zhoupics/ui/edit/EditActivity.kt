package com.tgwgroup.zhoupics.ui.edit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.DefaultOnStateChangedListener
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityEditBinding
import com.tgwgroup.zhoupics.render.RenderHelper
import com.tgwgroup.zhoupics.ui.edit.adjust.AdjustFragment
import com.tgwgroup.zhoupics.ui.edit.beautify.BeautifyFragment
import com.tgwgroup.zhoupics.ui.gallery.GalleryActivity
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.constants.EXTRA_URI
import com.tgwgroup.zhoupics.constants.MODE_FAST
import com.tgwgroup.zhoupics.constants.MODE_PRECISE
import com.tgwgroup.zhoupics.history.HistoryRecord
import com.tgwgroup.zhoupics.history.UpdateImageRecord
import com.tgwgroup.zhoupics.ui.edit.compare.CompareLoadingActivity
import com.tgwgroup.zhoupics.ui.edit.compare.CompareModeSelectBottomSheet
import com.tgwgroup.zhoupics.ui.edit.composition.CompositionFragment
import com.tgwgroup.zhoupics.ui.edit.elimination.EliminationFragment
import com.tgwgroup.zhoupics.ui.edit.filter.FilterFragment
import com.tgwgroup.zhoupics.ui.export.ExportActivity
import com.tgwgroup.zhoupics.ui.loading.LoadingDialogFragment
import com.tgwgroup.zhoupics.utils.appContext
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.dpToPx
import com.tgwgroup.zhoupics.utils.getBitmap
import com.tgwgroup.zhoupics.utils.getParcelableExtraCompat
import com.tgwgroup.zhoupics.utils.clearCache
import com.tgwgroup.zhoupics.utils.saveBitmapToGallery
import com.tgwgroup.zhoupics.utils.toastError
import com.tgwgroup.zhoupics.utils.toastSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivity : BaseActivity<ActivityEditBinding>() {
    private var originalImageUri: Uri? = null

    private var currentImageUri: Uri? = null

    var currentBitmap: Bitmap? = null

    private val bottomTabAdapter = BottomTabAdapter()

    private val viewModel by viewModels<EditViewModel>()

    // height of tab fragment (without slider)
    val tabFragmentBodyHeight = MutableStateFlow(0)

    // height of tab fragment slider (without bubble)
    val tabFragmentSliderHeight = MutableStateFlow(0)

    lateinit var renderHelper: RenderHelper

    private var compareFacesMode = MODE_FAST

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.getParcelableExtraCompat(EXTRA_URI, Uri::class.java)
        if (originalImageUri != null && uri != null) {
            CompareLoadingActivity.start(this, originalImageUri!!, uri, compareFacesMode)
        }
    }

    companion object {
        const val TAG = "EditActivity"

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
                currentBitmap?.let {
                    onOriginalBitmapLoaded(it)
                }
            },
            onLoadFailed = {
                LogUtil.e(TAG, "Failed to load image")
            }
        )

        initViewModel()
        initBottomTab()
        initTriggers()
        initCollectors()

        lifecycleScope.launch {
            clearCache()
        }
    }

    private fun onOriginalBitmapLoaded(bitmap: Bitmap) {
        binding.imageView.setImage(ImageSource.bitmap(bitmap))
        binding.surfaceView.updateLayoutParams<LayoutParams> {
            width = bitmap.width
            height = bitmap.height
        }
        binding.imageView.setOnStateChangedListener(object : DefaultOnStateChangedListener() {
            override fun onMatrixChanged(matrix: Matrix?) {
                super.onMatrixChanged(matrix)
                matrix?.let {
                    binding.transformLayout.setTransformMatrix(it)
                }
            }
        })
        renderHelper.startRender(bitmap)
    }

    fun updateImage(uri: Uri, bitmap: Bitmap) {
        currentImageUri = uri
        currentBitmap = bitmap
        binding.imageView.setImage(ImageSource.bitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true)))
        binding.surfaceView.updateLayoutParams<LayoutParams> {
            width = bitmap.width
            height = bitmap.height
        }
        renderHelper.updateImage(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        renderHelper.destroy()
    }

    private fun initViewModel() {
        viewModel.onCompareFacesClicked = {
            CompareModeSelectBottomSheet.show(supportFragmentManager) {
                it.onFastModeClick = {
                    compareFacesMode = MODE_FAST
                    galleryLauncher.launch(GalleryActivity.getIntent(this, GalleryActivity.FROM_TYPE_COMPARE_FACES))
                }

                it.onPreciseModeClick = {
                    compareFacesMode = MODE_PRECISE
                    galleryLauncher.launch(GalleryActivity.getIntent(this, GalleryActivity.FROM_TYPE_COMPARE_FACES))
                }
            }
        }
        viewModel.onCompositionClicked = {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_room_fragment, CompositionFragment(), CompositionFragment.TAG)
                .commitNowAllowingStateLoss()
        }
        viewModel.onEliminationClicked = {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_room_fragment, EliminationFragment(), EliminationFragment.TAG)
                .commitNowAllowingStateLoss()
        }
    }

    private fun initBottomTab() {
        binding.rvBottomTab.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBottomTab.adapter = bottomTabAdapter

        bottomTabAdapter.setItems(viewModel.bottomTabItemList.value)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTriggers() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivExport.setOnClickListener {
            export()
        }
        binding.ivUndo.setOnClickListener {
            viewModel.historyHelper.undo()
        }
        binding.ivRedo.setOnClickListener {
            viewModel.historyHelper.redo()
        }
        binding.ivCompare.setOnTouchListener { view, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    renderHelper.startCompare()
                }
                MotionEvent.ACTION_UP -> {
                    renderHelper.endCompare()
                }
            }
            true
        }
    }

    private fun initCollectors() {
        viewModel.bottomTabItemList.collectIn(lifecycleScope) {
            bottomTabAdapter.setItems(it)
        }

        viewModel.selectedBottomTabId.collectIn(lifecycleScope) {
            updateTabFragment(it)
        }

        combine(tabFragmentBodyHeight, tabFragmentSliderHeight) { p1, p2 ->
            Pair(p1, p2)
        }.collectIn(lifecycleScope) { pair ->
            val bodyHeight = pair.first
            val sliderHeight = pair.second
            // Slider height might be a bit too high for margins, so reduce it by 8dp here.
            val sliderDisplayHeight = (sliderHeight - dpToPx(8f)).coerceAtLeast(0)
            binding.ivUndo.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = bodyHeight + sliderDisplayHeight
            }
            binding.ivRedo.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = bodyHeight + sliderDisplayHeight
            }
            binding.ivCompare.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = bodyHeight + sliderDisplayHeight
            }
            binding.vTabFragmentBodyHeight.updateLayoutParams<LayoutParams> {
                height = bodyHeight
            }
        }

        viewModel.historyHelper.canUndo.collectIn(lifecycleScope) {
            binding.ivUndo.isEnabled = it
            binding.ivUndo.alpha = if (it) 1f else 0.5f
        }

        viewModel.historyHelper.canRedo.collectIn(lifecycleScope) {
            binding.ivRedo.isEnabled = it
            binding.ivRedo.alpha = if (it) 1f else 0.5f
        }

        viewModel.inRoom.collectIn(lifecycleScope) {
            binding.ivUndo.isVisible = !it
            binding.ivRedo.isVisible = !it
            binding.ivCompare.isVisible = !it
        }

        viewModel.historyHelper.undoEvent.collectIn(lifecycleScope) {
            handleUndoRedoEvent(it.receivedRecord)
        }

        viewModel.historyHelper.redoEvent.collectIn(lifecycleScope) {
            handleUndoRedoEvent(it.receivedRecord)
        }
    }

    private fun handleUndoRedoEvent(receivedRecord: HistoryRecord) {
        if (receivedRecord is UpdateImageRecord) {
            kotlin.runCatching {
                updateImageWithLoading(Uri.parse(receivedRecord.imageUri))
            }.onFailure {
                it.printStackTrace()
            }
        } else if (viewModel.historyHelper.isBeforeEarliestRecord(UpdateImageRecord::class.java)) {
            originalImageUri?.let {
                runCatching {
                    updateImageWithLoading(it)
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun updateImageWithLoading(uri: Uri) {
        if (uri == currentImageUri) {
            return
        }
        LoadingDialogFragment.show(supportFragmentManager)
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = getBitmap(uri)
            withContext(Dispatchers.Main) {
                bitmap?.let {
                    updateImage(uri, it)
                }
                delay(500)
                LoadingDialogFragment.dismiss(supportFragmentManager)
            }
        }
    }

    private fun updateTabFragment(tabId: Int) {
        when (tabId) {
            TAB_ADJUST -> {
                val existFragment = supportFragmentManager.findFragmentByTag(AdjustFragment.TAG)
                if (existFragment != null) {
                    (existFragment as? AdjustFragment)?.recycle()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_tab_fragment, AdjustFragment(), AdjustFragment.TAG)
                    .commitNowAllowingStateLoss()
            }

            TAB_BEAUTIFY -> {
                val existFragment = supportFragmentManager.findFragmentByTag(BeautifyFragment.TAG)
                if (existFragment != null) {
                    (existFragment as? BeautifyFragment)?.recycle()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_tab_fragment, BeautifyFragment(), BeautifyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }

            TAB_FILTER -> {
                val existFragment = supportFragmentManager.findFragmentByTag(FilterFragment.TAG)
                if (existFragment != null) {
                    (existFragment as? FilterFragment)?.recycle()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_tab_fragment, FilterFragment(), FilterFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
        }
    }

    private fun loadOriginalImage(onLoad: (() -> Unit)? = null, onLoadFailed: (() -> Unit)? = null) {
        originalImageUri = intent.getParcelableExtraCompat(EXTRA_URI, Uri::class.java)
        currentImageUri = originalImageUri
        lifecycleScope.launch(Dispatchers.IO) {
            originalImageUri?.let {
                currentBitmap = getBitmap(it)
                if (currentBitmap != null) {
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

    private fun export() {
        lifecycleScope.launch(Dispatchers.Main) {
            LoadingDialogFragment.show(supportFragmentManager)

            var resultUri: Uri? = null
            withContext(Dispatchers.IO) {
                renderHelper.getResultBitmap()?.let {
                    resultUri = saveBitmapToGallery(this@EditActivity, it, filename = getExportedFilename())
                }
            }

            delay(500)
            LoadingDialogFragment.dismiss(supportFragmentManager)
            resultUri?.let {
                toastSuccess(appContext.getString(R.string.saved_successfully))
                ExportActivity.start(this@EditActivity, uri = it)
            } ?: run {
                toastError(appContext.getString(R.string.save_failed))
            }
        }
    }

    private fun getExportedFilename() = "zpics_${System.currentTimeMillis()}.jpg"
}