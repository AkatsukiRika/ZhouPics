package com.tgwgroup.zhoupics.ui.edit.beautify

import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentBeautifyBinding
import com.tgwgroup.zhoupics.history.BeautifyRecord
import com.tgwgroup.zhoupics.history.HistoryRecord
import com.tgwgroup.zhoupics.render.RenderHelper
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.edit.EditViewModel
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.dpToPx
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BeautifyFragment : BaseFragment<FragmentBeautifyBinding>() {
    private val beautifyAdapter = BeautifyAdapter()

    private val viewModel by viewModels<BeautifyViewModel>()

    private val editViewModel by activityViewModels<EditViewModel>()

    private val renderScope = MainScope()

    companion object {
        const val TAG = "BeautifyFragment"
    }

    override fun onBindingCreate(): FragmentBeautifyBinding {
        return FragmentBeautifyBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val latestRecord = editViewModel.historyHelper.getLatestRecord(BeautifyRecord::class.java) as? BeautifyRecord
        viewModel.init(latestRecord)
        initRecyclerView()
        initListeners()
        initCollectors()

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
        binding.rvBeautify.post {
            getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvBeautify.height + dpToPx(8f)
        }
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvBeautify.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBeautify.adapter = beautifyAdapter
    }

    private fun initListeners() {
        val binding = binding ?: return
        binding.slider.setOnProgressChangeListener(object : BidirectionalSlider.OnProgressChangeListener {
            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch() {
                editViewModel.historyHelper.addRecord(viewModel.getHistoryRecord())
            }

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                when (viewModel.selectedItemId.value) {
                    BEAUTIFY_SMOOTH -> {
                        updateProgress(smooth = progress)
                    }
                    BEAUTIFY_WHITE -> {
                        updateProgress(white = progress)
                    }
                    BEAUTIFY_LIPSTICK -> {
                        updateProgress(lipstick = progress)
                    }
                    BEAUTIFY_BLUSHER -> {
                        updateProgress(blusher = progress)
                    }
                    BEAUTIFY_EYE_ZOOM -> {
                        updateProgress(eyeZoom = progress)
                    }
                    BEAUTIFY_FACE_SLIM -> {
                        updateProgress(faceSlim = progress)
                    }
                }
            }
        })
    }

    private fun updateProgress(
        smooth: Float? = null,
        white: Float? = null,
        lipstick: Float? = null,
        blusher: Float? = null,
        eyeZoom: Float? = null,
        faceSlim: Float? = null,
        renderHelper: RenderHelper? = null
    ) {
        val render = renderHelper ?: getEditActivity()?.renderHelper

        smooth?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_SMOOTH, progress)
            render?.updateSmoothProgress(progress)
        }
        white?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_WHITE, progress)
            render?.updateWhiteProgress(progress)
        }
        lipstick?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_LIPSTICK, progress)
            render?.updateLipstickProgress(progress)
        }
        blusher?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_BLUSHER, progress)
            render?.updateBlusherProgress(progress)
        }
        eyeZoom?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_EYE_ZOOM, progress)
            render?.updateEyeZoomProgress(progress)
        }
        faceSlim?.let { progress ->
            viewModel.updateProgress(BEAUTIFY_FACE_SLIM, progress)
            render?.updateFaceSlimProgress(progress)
        }
    }

    private fun updateProgress(record: HistoryRecord, renderHelper: RenderHelper?) {
        if (record is BeautifyRecord) {
            updateProgress(
                smooth = record.smoothProgress,
                white = record.whiteProgress,
                lipstick = record.lipstickProgress,
                blusher = record.blusherProgress,
                eyeZoom = record.eyeZoomProgress,
                faceSlim = record.faceSlimProgress,
                renderHelper = renderHelper
            )
        } else if (editViewModel.historyHelper.isBeforeEarliestRecord(BeautifyRecord::class.java)) {
            updateProgress(
                smooth = 0f,
                white = 0f,
                lipstick = 0f,
                blusher = 0f,
                eyeZoom = 0f,
                faceSlim = 0f,
                renderHelper = renderHelper
            )
        }
        onSelectedItemChanged(viewModel.selectedItemId.value)
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            beautifyAdapter.setItems(it)
        }

        viewModel.selectedItemId.collectIn(lifecycleScope) {
            onSelectedItemChanged(it)
        }

        renderScope.launch {
            // Capturing renderHelper in the closure to ensure rendering is still available after fragment being detached.
            val renderHelper = getEditActivity()?.renderHelper

            launch {
                editViewModel.historyHelper.undoEvent.collect {
                    updateProgress(it.receivedRecord, renderHelper)
                }
            }

            launch {
                editViewModel.historyHelper.redoEvent.collect {
                    updateProgress(it.receivedRecord, renderHelper)
                }
            }
        }
    }

    private fun onSelectedItemChanged(itemId: Int?) {
        val binding = binding ?: return
        val item = viewModel.itemList.value.find { it.id == itemId }
        item?.let {
            binding.slider.post {
                if (binding.slider.isInvisible) {
                    binding.vSliderGradient.isInvisible = false
                    binding.slider.isInvisible = false
                    getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvBeautify.height + binding.slider.height
                }
            }
        }
        binding.slider.setValue(item?.progress ?: 0f)
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }

    fun recycle() {
        renderScope.cancel()
    }
}