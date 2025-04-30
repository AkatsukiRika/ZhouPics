package com.tgwgroup.zhoupics.ui.edit.adjust

import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentAdjustBinding
import com.tgwgroup.zhoupics.history.AdjustRecord
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

class AdjustFragment : BaseFragment<FragmentAdjustBinding>() {
    private val adjustAdapter = AdjustAdapter()

    private val viewModel by viewModels<AdjustViewModel>()

    private val editViewModel by activityViewModels<EditViewModel>()

    private val renderScope = MainScope()

    companion object {
        const val TAG = "AdjustFragment"
    }

    override fun onBindingCreate(): FragmentAdjustBinding {
        return FragmentAdjustBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val latestRecord = editViewModel.historyHelper.getLatestRecord(AdjustRecord::class.java) as? AdjustRecord
        viewModel.init(latestRecord)
        initRecyclerView()
        initListeners()
        initCollectors()

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
        binding.rvAdjust.post {
            getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvAdjust.height + dpToPx(8f)
        }
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvAdjust.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAdjust.adapter = adjustAdapter
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
                    ADJUST_CONTRAST -> {
                        updateProgress(contrast = progress)
                    }
                    ADJUST_EXPOSURE -> {
                        updateProgress(exposure = progress)
                    }
                    ADJUST_SATURATION -> {
                        updateProgress(saturation = progress)
                    }
                    ADJUST_SHARPNESS -> {
                        updateProgress(sharpness = progress)
                    }
                    ADJUST_BRIGHTNESS -> {
                        updateProgress(brightness = progress)
                    }
                }
            }
        })
    }

    private fun updateProgress(
        contrast: Float? = null,
        exposure: Float? = null,
        saturation: Float? = null,
        sharpness: Float? = null,
        brightness: Float? = null,
        renderHelper: RenderHelper? = null
    ) {
        val render = renderHelper ?: getEditActivity()?.renderHelper

        contrast?.let { progress ->
            viewModel.updateProgress(ADJUST_CONTRAST, progress)
            render?.updateContrastProgress(progress)
        }
        exposure?.let { progress ->
            viewModel.updateProgress(ADJUST_EXPOSURE, progress)
            render?.updateExposureProgress(progress)
        }
        saturation?.let { progress ->
            viewModel.updateProgress(ADJUST_SATURATION, progress)
            render?.updateSaturationProgress(progress)
        }
        sharpness?.let { progress ->
            viewModel.updateProgress(ADJUST_SHARPNESS, progress)
            render?.updateSharpnessProgress(progress)
        }
        brightness?.let { progress ->
            viewModel.updateProgress(ADJUST_BRIGHTNESS, progress)
            render?.updateBrightnessProgress(progress)
        }

        render?.doRender()
    }

    private fun updateProgress(record: HistoryRecord, renderHelper: RenderHelper?) {
        if (record is AdjustRecord) {
            updateProgress(
                contrast = record.contrastProgress,
                exposure = record.exposureProgress,
                saturation = record.saturationProgress,
                sharpness = record.sharpnessProgress,
                brightness = record.brightnessProgress,
                renderHelper
            )
        } else if (editViewModel.historyHelper.isBeforeEarliestRecord(AdjustRecord::class.java)) {
            updateProgress(0f, 0f, 0f, 0f, 0f, renderHelper)
        }
        onSelectedItemChanged(viewModel.selectedItemId.value)
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            adjustAdapter.setItems(it)
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
            if (item.twoWaySlider) {
                binding.slider.setValueRange(-100f, 100f)
                binding.slider.setBidirectional(true)
            } else {
                binding.slider.setValueRange(0f, 100f)
                binding.slider.setBidirectional(false)
            }
            binding.slider.post {
                if (binding.slider.isInvisible) {
                    binding.vSliderGradient.isInvisible = false
                    binding.slider.isInvisible = false
                    getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvAdjust.height + binding.slider.height
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